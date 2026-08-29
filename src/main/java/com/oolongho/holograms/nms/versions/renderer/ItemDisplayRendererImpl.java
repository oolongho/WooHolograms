package com.oolongho.holograms.nms.versions.renderer;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.nms.renderer.NmsItemDisplayRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import com.oolongho.holograms.util.ItemLineParser;
import com.oolongho.holograms.util.PlaceholderUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ItemDisplay 物品全息图渲染器实现
 * 使用 ItemDisplay 实体替代 ArmorStand+Item 骑乘方案
 * 支持附魔光效、Billboard、Display 属性
 *
 * <p>物品解析委托 {@link ItemLineParser}：支持原版 Material、特殊头颅形式、
 * CraftEngine 自定义物品（namespace:path）与尾参数（cmd:/color:/name:/lore:/glow/unbreakable）。</p>
 */
public class ItemDisplayRendererImpl implements NmsItemDisplayRenderer {

    private final int entityId;
    private volatile boolean destroyed = false;
    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private final Map<UUID, String> lastContentPerPlayer = new ConcurrentHashMap<>();
    /** 未识别 ID 兜底警告只记录一次（每行一个渲染器实例） */
    private final AtomicBoolean warnedUnresolved = new AtomicBoolean(false);

    public ItemDisplayRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.singletonList(entityId);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (destroyed || location == null || line == null) {
            return;
        }

        ItemLineParser.Result result = parseItem(line.getContent(), player);
        String rawContent = line.getContent();
        if (rawContent != null && player != null) {
            lastContentPerPlayer.put(player.getUniqueId(), PlaceholderUtil.replace(rawContent, player));
        }

        Hologram hologram = line.getHologram();
        Billboard billboard = resolveBillboard(line, hologram);
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        // 计算朝向
        float yaw;
        float pitch;
        Float customYaw = line.getCustomYaw();
        Float customPitch = line.getCustomPitch();

        if (billboard == Billboard.FIXED_ANGLE) {
            Float holoPitch = hologram != null ? hologram.getPitch() : null;
            yaw = customYaw != null ? customYaw : hologramFacing;
            pitch = customPitch != null ? customPitch : (holoPitch != null ? holoPitch : 0f);
        } else {
            yaw = customYaw != null ? customYaw : location.getYaw();
            pitch = customPitch != null ? customPitch : location.getPitch();
        }

        this.currentYaw = yaw;
        this.currentPitch = pitch;

        // 构建元数据：ItemDisplay 需要可见（不设置 invisible），无重力，Billboard，物品，Display 属性
        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withBillboard(billboard)
                .withItemDisplayItem(result.item())
                .withDisplayProperties(line, hologram);

        // 附魔光效：glow 参数触发实体发光（兼容旧行为：":glow" 后缀与 " glow" 后缀）
        if (result.glow()) {
            metadataBuilder.withGlow();
        }

        HologramPosition position = HologramPosition.fromLocation(location);

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, org.bukkit.entity.EntityType.ITEM_DISPLAY,
                        position, yaw, pitch)
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                .sendTo(player);
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        if (destroyed) return;
        String rawContent = line.getContent();
        String resolvedContent = rawContent;
        if (rawContent != null && player != null) {
            resolvedContent = PlaceholderUtil.replace(rawContent, player);
        }
        String lastContent = lastContentPerPlayer.get(player.getUniqueId());
        if (resolvedContent != null && resolvedContent.equals(lastContent)) return;
        lastContentPerPlayer.put(player.getUniqueId(), resolvedContent);

        ItemLineParser.Result result = parseItem(line.getContent(), player);

        Hologram hologram = line.getHologram();
        Billboard billboard = resolveBillboard(line, hologram);

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withBillboard(billboard)
                .withItemDisplayItem(result.item())
                .withDisplayProperties(line, hologram);

        if (result.glow()) {
            metadataBuilder.withGlow();
        }

        EntityPacketsBuilder.create()
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                .sendTo(player);
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
    public void reset() {
        destroyed = false;
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        lastContentPerPlayer.clear();
    }

    /**
     * 解析物品内容，委托共享解析器
     * render 与 updateText 两条路径行为一致；未识别 ID 记录一次 warning 并兜底 STONE
     */
    private ItemLineParser.Result parseItem(String content, Player player) {
        ItemLineParser.Result result = ItemLineParser.parse(content, player);
        if (!result.resolved() && warnedUnresolved.compareAndSet(false, true)) {
            String rawContent = content != null ? content : "";
            WooHolograms.getInstance().getLogger().warning(() ->
                    "无法解析 #ICON 行物品，已使用 STONE 兜底: " + rawContent);
        }
        return result;
    }

    /**
     * 解析物品行的 Billboard：
     * 行级显式设置优先（含 CENTER——用户明确选择旋转）；
     * 未设置时继承全息图级，若为默认 CENTER 则降级为 FIXED_ANGLE 静态朝向——
     * 道具随玩家走动打转并随俯仰翻转不符合道具语义，朝向由 /wh setfacing 或行级 custom-yaw 控制。
     */
    private Billboard resolveBillboard(HologramLine line, Hologram hologram) {
        Billboard billboard = line.getBillboard();
        if (billboard == null) {
            billboard = hologram != null ? hologram.getBillboard() : Billboard.CENTER;
            if (billboard == Billboard.CENTER) {
                billboard = Billboard.FIXED_ANGLE;
            }
        }
        return billboard;
    }
}
