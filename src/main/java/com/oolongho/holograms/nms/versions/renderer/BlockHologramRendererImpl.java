package com.oolongho.holograms.nms.versions.renderer;

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
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方块全息图渲染器实现
 * 使用 BlockDisplay 实体展示方块
 */
public class BlockHologramRendererImpl implements NmsBlockHologramRenderer {

    private final int entityId;
    private volatile Material blockMaterial = Material.STONE;
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

        // 从行数据获取方块材质
        if (line != null && line.getBlockMaterial() != null) {
            this.blockMaterial = line.getBlockMaterial();
        }

        Hologram hologram = line != null ? line.getHologram() : null;
        Billboard billboard = line.getBillboard() != null ? line.getBillboard() : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withBillboard(billboard)
                .withBlockState(blockMaterial)
                .withDisplayProperties(line, hologram);

        float yaw;
        float pitch;

        if (billboard == Billboard.FIXED_ANGLE) {
            Float customYaw = line != null ? line.getCustomYaw() : null;
            Float customPitch = line != null ? line.getCustomPitch() : null;
            yaw = customYaw != null ? customYaw : hologramFacing;
            pitch = customPitch != null ? customPitch : 0;
        } else {
            Float customYaw = line != null ? line.getCustomYaw() : null;
            Float customPitch = line != null ? line.getCustomPitch() : null;
            yaw = customYaw != null ? customYaw : location.getYaw();
            pitch = customPitch != null ? customPitch : location.getPitch();
        }

        this.currentYaw = yaw;
        this.currentPitch = pitch;

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, org.bukkit.entity.EntityType.BLOCK_DISPLAY,
                        new HologramPosition(location.getX(), location.getY(), location.getZ()),
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

        // 检查方块材质是否变化
        Material newMaterial = line.getBlockMaterial();
        if (newMaterial != null && newMaterial != this.blockMaterial) {
            // 方块类型变了，需要销毁重建
            destroy(player);
            this.blockMaterial = newMaterial;
            render(player, line.getLocation(), line);
            return;
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

        // 解析可能变化的方块材质（占位符替换后）
        if (resolvedContent != null) {
            String upperContent = resolvedContent.toUpperCase(Locale.ROOT);
            if (upperContent.startsWith("#BLOCK:")) {
                String materialName = resolvedContent.substring(7).trim();
                Material material = Material.matchMaterial(materialName);
                if (material != null && material.isBlock() && material != this.blockMaterial) {
                    this.blockMaterial = material;
                    // 重新发送元数据更新方块状态
                    Hologram hologram = line.getHologram();
                    Billboard billboard = line.getBillboard() != null ? line.getBillboard() : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);

                    EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                            .withBlockState(blockMaterial)
                            .withBillboard(billboard)
                            .withDisplayProperties(line, hologram);

                    EntityPacketsBuilder.create()
                            .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                            .sendTo(player);
                }
            }
        }
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

        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, new HologramPosition(
                        location.getX(), location.getY(), location.getZ(),
                        currentYaw, currentPitch))
                .sendTo(player);
    }

    @Override
    public void teleport(Collection<Player> players, Location location) {
        for (Player player : players) {
            teleport(player, location);
        }
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void reset() {
        destroyed = false;
        blockMaterial = Material.STONE;
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        lastContentPerPlayer.clear();
    }
}
