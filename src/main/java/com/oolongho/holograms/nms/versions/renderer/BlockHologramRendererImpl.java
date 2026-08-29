package com.oolongho.holograms.nms.versions.renderer;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.nms.renderer.NmsBlockHologramRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方块全息图渲染器实现
 * 使用 BlockDisplay 实体展示方块
 *
 * <p>支持原版 Material 与 CraftEngine 自定义方块（BlockData），
 * CE 解析结果优先于 Material 渲染（资源包模型生效）。</p>
 */
public class BlockHologramRendererImpl implements NmsBlockHologramRenderer {

    private final int entityId;
    private volatile Material blockMaterial = Material.STONE;
    /** CraftEngine 自定义方块 BlockData（null 表示原版 Material 模式） */
    private volatile BlockData blockData;
    private volatile boolean destroyed = false;
    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private final Map<UUID, String> lastContentPerPlayer = new ConcurrentHashMap<>();

    public BlockHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.singletonList(entityId);
    }

    @Override
    public Material getBlockMaterial() {
        return blockMaterial;
    }

    @Override
    public void setBlockMaterial(Material material) {
        this.blockMaterial = material;
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (destroyed || location == null || location.getWorld() == null) {
            return;
        }

        // 从行数据获取方块状态：CE BlockData 优先，其次 Material
        if (line != null) {
            BlockData lineData = line.getBlockData();
            if (lineData != null) {
                this.blockData = lineData;
                this.blockMaterial = lineData.getMaterial();
            } else if (line.getBlockMaterial() != null) {
                this.blockData = null;
                this.blockMaterial = line.getBlockMaterial();
            }
        }

        Hologram hologram = line != null ? line.getHologram() : null;
        Billboard billboard = resolveBillboard(line, hologram);
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withBillboard(billboard);
        applyBlockState(metadataBuilder).withDisplayProperties(line, hologram);

        float yaw;
        float pitch;

        if (billboard == Billboard.FIXED_ANGLE) {
            Float customYaw = line != null ? line.getCustomYaw() : null;
            Float customPitch = line != null ? line.getCustomPitch() : null;
            Float holoPitch = hologram != null ? hologram.getPitch() : null;
            yaw = customYaw != null ? customYaw : hologramFacing;
            pitch = customPitch != null ? customPitch : (holoPitch != null ? holoPitch : 0f);
        } else {
            Float customYaw = line != null ? line.getCustomYaw() : null;
            Float customPitch = line != null ? line.getCustomPitch() : null;
            yaw = customYaw != null ? customYaw : location.getYaw();
            pitch = customPitch != null ? customPitch : location.getPitch();
        }

        this.currentYaw = yaw;
        this.currentPitch = pitch;

        // BlockDisplay 以实体位置为方块西北下角（方块占 [x,x+1]×[y,y+1]×[z,z+1]），
        // 偏移 -0.5 使方块以行位置为中心
        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, org.bukkit.entity.EntityType.BLOCK_DISPLAY,
                        centeredBlockPosition(location),
                        yaw, pitch)
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                .sendTo(player);

        if (line != null && line.getContent() != null) {
            lastContentPerPlayer.put(player.getUniqueId(), line.getContent());
        }
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        if (destroyed || line == null) return;

        // 行级解析结果变化（CE BlockData 或 Material）→ 销毁重建
        BlockData lineData = line.getBlockData();
        if (lineData != null) {
            if (!lineData.equals(this.blockData)) {
                destroy(player);
                render(player, line.getLocation(), line);
                return;
            }
        } else {
            Material newMaterial = line.getBlockMaterial();
            if (newMaterial != null && (newMaterial != this.blockMaterial || this.blockData != null)) {
                destroy(player);
                render(player, line.getLocation(), line);
                return;
            }
        }

        // 检查内容是否变化（占位符支持）
        String rawContent = line.getContent();
        String resolvedContent = rawContent;
        if (rawContent != null && player != null) {
            resolvedContent = com.oolongho.holograms.util.PlaceholderUtil.replace(rawContent, player);
        }
        String lastContent = lastContentPerPlayer.get(player.getUniqueId());
        if (resolvedContent != null && resolvedContent.equals(lastContent)) return;
        lastContentPerPlayer.put(player.getUniqueId(), resolvedContent);

        // 解析可能变化的方块状态（占位符替换后，含 CE 重解析）
        if (resolvedContent == null) return;
        String upperContent = resolvedContent.toUpperCase(Locale.ROOT);
        if (!upperContent.startsWith("#BLOCK:")) return;

        String materialName = resolvedContent.substring(7).trim();

        // CraftEngine 重解析
        if (materialName.contains(":")) {
            var hook = WooHolograms.getInstance().getCraftEngineHook();
            if (hook != null && hook.isReady()) {
                BlockData data = hook.resolveBlockData(materialName);
                if (data != null) {
                    if (!data.equals(this.blockData)) {
                        this.blockData = data;
                        this.blockMaterial = data.getMaterial();
                        sendBlockMetadata(player, line);
                    }
                    return;
                }
            }
        }

        // 原版 Material 重解析
        Material material = Material.matchMaterial(materialName);
        if (material != null && material.isBlock()
                && (material != this.blockMaterial || this.blockData != null)) {
            this.blockMaterial = material;
            this.blockData = null;
            sendBlockMetadata(player, line);
        }
    }

    /**
     * 重新发送方块状态元数据（不重建实体）
     */
    private void sendBlockMetadata(Player player, HologramLine line) {
        Hologram hologram = line.getHologram();
        Billboard billboard = resolveBillboard(line, hologram);

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withBillboard(billboard);
        applyBlockState(metadataBuilder).withDisplayProperties(line, hologram);

        EntityPacketsBuilder.create()
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                .sendTo(player);
    }

    /**
     * 应用当前生效的方块状态（CE BlockData 优先，否则 Material）
     */
    private EntityMetadataBuilder applyBlockState(EntityMetadataBuilder builder) {
        return blockData != null ? builder.withBlockState(blockData)
                : builder.withBlockState(blockMaterial);
    }

    @Override
    public void updateText(Collection<Player> players, HologramLine line) {
        for (Player player : players) {
            updateText(player, line);
        }
    }

    @Override
    public void destroy(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
        lastContentPerPlayer.remove(player.getUniqueId());
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            EntityPacketsBuilder.create()
                    .withRemoveEntity(entityId)
                    .sendTo(player);
        }
        lastContentPerPlayer.clear();
    }

    @Override
    public void teleport(Player player, Location location) {
        if (destroyed || location == null) {
            return;
        }

        HologramPosition centered = centeredBlockPosition(location);
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, new HologramPosition(
                        centered.getX(), centered.getY(), centered.getZ(),
                        currentYaw, currentPitch))
                .sendTo(player);
    }

    @Override
    public void teleport(Collection<Player> players, Location location) {
        for (Player player : players) {
            teleport(player, location);
        }
    }

    /**
     * 计算方块居中的实体位置：
     * BlockDisplay 以实体位置为方块西北下角（方块占 [x,x+1]×[y,y+1]×[z,z+1]），
     * 偏移 -0.5 使方块以行位置为中心，与文本行对齐
     */
    private HologramPosition centeredBlockPosition(Location location) {
        return new HologramPosition(
                location.getX() - 0.5,
                location.getY() - 0.5,
                location.getZ() - 0.5);
    }

    /**
     * 解析方块行的 Billboard：
     * 行级显式设置优先（含 CENTER——用户明确选择旋转）；
     * 未设置时继承全息图级，若为默认 CENTER 则降级为 FIXED_ANGLE 静态朝向——
     * 方块随玩家走动打转并随俯仰翻转不符合方块语义，朝向由 /wh setfacing 或行级 custom-yaw 控制。
     */
    private Billboard resolveBillboard(HologramLine line, Hologram hologram) {
        Billboard billboard = line != null ? line.getBillboard() : null;
        if (billboard == null) {
            billboard = hologram != null ? hologram.getBillboard() : Billboard.CENTER;
            if (billboard == Billboard.CENTER) {
                billboard = Billboard.FIXED_ANGLE;
            }
        }
        return billboard;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void reset() {
        destroyed = false;
        blockMaterial = Material.STONE;
        blockData = null;
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        lastContentPerPlayer.clear();
    }
}
