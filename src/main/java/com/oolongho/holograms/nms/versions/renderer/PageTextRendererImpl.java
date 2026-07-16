package com.oolongho.holograms.nms.versions.renderer;

import com.oolongho.holograms.hologram.*;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import com.oolongho.holograms.util.ColorUtil;
import com.oolongho.holograms.util.Profiler;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 页面级文本渲染器
 * 将连续的 TEXT 行合并为单个 TextDisplay 实体渲染
 * 解决背景宽度不一致、文本换行重叠和对齐问题
 */
public class PageTextRendererImpl {

    /** 一个文本行组：连续的TEXT行 */
    private static class TextGroup {
        final int frontEntityId;
        final int backEntityId;
        final List<HologramLine> lines;

        TextGroup(int frontEntityId, int backEntityId, List<HologramLine> lines) {
            this.frontEntityId = frontEntityId;
            this.backEntityId = backEntityId;
            this.lines = lines;
        }
    }

    private final HologramPage page;
    private final EntityIdGenerator entityIdGenerator;
    private List<TextGroup> textGroups = new ArrayList<>();
    private volatile boolean destroyed = false;

    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private boolean currentDoubleSided = false;

    /** 每个玩家每组的文本缓存，用于增量更新 */
    private final Map<UUID, Map<Integer, String>> lastTextPerPlayerGroup = new ConcurrentHashMap<>();

    public PageTextRendererImpl(HologramPage page, EntityIdGenerator entityIdGenerator) {
        this.page = page;
        this.entityIdGenerator = entityIdGenerator;
        rebuildGroups();
    }

    /**
     * 重建文本行组
     * 遍历页面所有行，将连续的TEXT行分为一组
     */
    public void rebuildGroups() {
        // 先移除旧实体，避免实体ID泄漏
        if (!textGroups.isEmpty()) {
            EntityPacketsBuilder removePackets = EntityPacketsBuilder.create();
            for (TextGroup group : textGroups) {
                removePackets.withRemoveEntity(group.frontEntityId);
                removePackets.withRemoveEntity(group.backEntityId);
            }
            for (UUID uuid : lastTextPerPlayerGroup.keySet()) {
                org.bukkit.entity.Player viewer = org.bukkit.Bukkit.getPlayer(uuid);
                if (viewer != null && viewer.isOnline()) {
                    removePackets.sendTo(viewer);
                }
            }
        }

        textGroups.clear();
        lastTextPerPlayerGroup.clear();

        List<HologramLine> currentGroupLines = new ArrayList<>();
        for (HologramLine line : page.getLines()) {
            if (line.getType() == HologramType.TEXT) {
                currentGroupLines.add(line);
            } else {
                if (!currentGroupLines.isEmpty()) {
                    textGroups.add(new TextGroup(
                            entityIdGenerator.getFreeEntityId(),
                            entityIdGenerator.getFreeEntityId(),
                            new ArrayList<>(currentGroupLines)
                    ));
                    currentGroupLines.clear();
                }
            }
        }
        // 处理末尾的TEXT行组
        if (!currentGroupLines.isEmpty()) {
            textGroups.add(new TextGroup(
                    entityIdGenerator.getFreeEntityId(),
                    entityIdGenerator.getFreeEntityId(),
                    new ArrayList<>(currentGroupLines)
            ));
        }
    }

    /**
     * 渲染所有文本行组给指定玩家
     */
    public void render(Player player, Location baseLocation) {
        if (destroyed) return;

        Profiler profiler = Profiler.getInstance();
        if (profiler.isEnabled()) profiler.start("渲染");
        try {

        Hologram hologram = page.getParent();
        if (hologram == null) return;

        Billboard billboard = hologram.getBillboard();
        boolean doubleSided = hologram.isDoubleSided();
        float hologramFacing = hologram.getFacing();
        TextAlignment alignment = hologram.getAlignment();
        int backgroundColor = (hologram.getBackgroundAlpha() << 24) | hologram.getBackgroundColor();
        int lineWidth = hologram.getLineWidth();

        float yaw, pitch;
        if (billboard == Billboard.FIXED_ANGLE) {
            yaw = hologramFacing;
            pitch = 0;
        } else {
            yaw = baseLocation.getYaw();
            pitch = baseLocation.getPitch();
        }
        this.currentYaw = yaw;
        this.currentPitch = pitch;
        this.currentDoubleSided = doubleSided;

        Map<Integer, String> playerGroupTexts = new HashMap<>();

        for (int gi = 0; gi < textGroups.size(); gi++) {
            TextGroup group = textGroups.get(gi);

            // 收集该组所有TEXT行的文本
            List<String> textLines = new ArrayList<>();
            for (HologramLine line : group.lines) {
                textLines.add(line.getDisplayText(player));
            }
            String textKey = String.join("\n", textLines);
            playerGroupTexts.put(gi, textKey);

            // 使用组内第一行的位置
            Location groupLocation = group.lines.get(0).getLocation();
            if (groupLocation == null) continue;

            EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                    .withInvisible()
                    .withNoGravity()
                    .withTextDisplayText(textLines)
                    .withBillboard(billboard)
                    .withTextAlignment(alignment)
                    .withTextBackgroundColor(backgroundColor)
                    .withTextLineWidth(lineWidth)
                    .withDisplayProperties(group.lines, hologram);

            List<SynchedEntityData.DataItem<?>> metadata = metadataBuilder.toWatchableObjects();

            EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                    .withSpawnEntity(group.frontEntityId, org.bukkit.entity.EntityType.TEXT_DISPLAY,
                            new HologramPosition(groupLocation.getX(), groupLocation.getY(), groupLocation.getZ()),
                            yaw, pitch)
                    .withEntityMetadata(group.frontEntityId, metadata);

            if (doubleSided) {
                packetsBuilder.withSpawnEntity(group.backEntityId, org.bukkit.entity.EntityType.TEXT_DISPLAY,
                                new HologramPosition(groupLocation.getX(), groupLocation.getY(), groupLocation.getZ()),
                                yaw + 180.0f, pitch)
                        .withEntityMetadata(group.backEntityId, metadata);
            }

            packetsBuilder.sendTo(player);
        }

        lastTextPerPlayerGroup.put(player.getUniqueId(), playerGroupTexts);

        } finally {
            if (profiler.isEnabled()) profiler.stop("渲染");
        }
    }

    /**
     * 更新所有文本行组的文本（增量更新，仅发送变化的组）
     * Chroma 启用时，即使文本未变化也会更新元数据以实现动态颜色
     */
    public void updateText(Player player) {
        if (destroyed) return;

        Hologram hologram = page.getParent();
        if (hologram == null) return;

        Billboard billboard = hologram.getBillboard();
        boolean doubleSided = hologram.isDoubleSided();
        TextAlignment alignment = hologram.getAlignment();
        int backgroundColor = (hologram.getBackgroundAlpha() << 24) | hologram.getBackgroundColor();
        int lineWidth = hologram.getLineWidth();

        // 计算 Chroma 颜色（基于系统时间）
        long chromaStep = System.currentTimeMillis() / 50; // 每50ms一步

        Map<Integer, String> playerGroupTexts = lastTextPerPlayerGroup.computeIfAbsent(
                player.getUniqueId(), k -> new HashMap<>());

        for (int gi = 0; gi < textGroups.size(); gi++) {
            TextGroup group = textGroups.get(gi);

            // 检查该组是否有 Chroma 效果
            boolean groupChromaBg = false;
            boolean groupChromaGlow = false;
            for (HologramLine line : group.lines) {
                if (line.isChromaBackground()) groupChromaBg = true;
                if (line.isChromaGlow()) groupChromaGlow = true;
            }
            boolean hasChroma = groupChromaBg || groupChromaGlow;

            // 收集文本行
            List<String> textLines = new ArrayList<>();
            for (HologramLine line : group.lines) {
                textLines.add(line.getDisplayText(player));
            }
            String textKey = String.join("\n", textLines);

            // 检查文本是否变化，Chroma 启用时始终更新
            String lastText = playerGroupTexts.get(gi);
            boolean textChanged = !textKey.equals(lastText);
            if (!textChanged && !hasChroma) continue;
            playerGroupTexts.put(gi, textKey);

            // 计算 Chroma 颜色
            int effectiveBgColor = backgroundColor;
            if (groupChromaBg) {
                int chromaRgb = ColorUtil.chromaColor(chromaStep) & 0x00FFFFFF; // 去掉 alpha
                effectiveBgColor = (backgroundColor & 0xFF000000) | chromaRgb; // 保留原 alpha
            }

            EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                    .withInvisible()
                    .withNoGravity()
                    .withTextDisplayText(textLines)
                    .withBillboard(billboard)
                    .withTextAlignment(alignment)
                    .withTextBackgroundColor(effectiveBgColor)
                    .withTextLineWidth(lineWidth)
                    .withDisplayProperties(group.lines, hologram, groupChromaGlow);

            // Chroma 发光色：覆盖 glowColor（withGlowColor 内部自动启用发光标志）
            if (groupChromaGlow) {
                metadataBuilder.withGlowColor(ColorUtil.chromaColor(chromaStep));
            }

            List<SynchedEntityData.DataItem<?>> metadata = metadataBuilder.toWatchableObjects();

            EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                    .withEntityMetadata(group.frontEntityId, metadata);

            if (doubleSided) {
                packetsBuilder.withEntityMetadata(group.backEntityId, metadata);
            }

            packetsBuilder.sendTo(player);
        }
    }

    /**
     * 强制更新所有文本行组的 Display Entity 属性（billboard/alignment/背景色/线宽/scale 等）
     * 用于属性编辑后即时刷新，避免 hide+show 的闪烁和性能开销
     */
    public void updateMetadata(Player player) {
        if (destroyed) return;

        Hologram hologram = page.getParent();
        if (hologram == null) return;

        Billboard billboard = hologram.getBillboard();
        boolean doubleSided = hologram.isDoubleSided();
        TextAlignment alignment = hologram.getAlignment();
        int backgroundColor = (hologram.getBackgroundAlpha() << 24) | hologram.getBackgroundColor();
        int lineWidth = hologram.getLineWidth();

        for (int gi = 0; gi < textGroups.size(); gi++) {
            TextGroup group = textGroups.get(gi);

            List<String> textLines = new ArrayList<>();
            for (HologramLine line : group.lines) {
                textLines.add(line.getDisplayText(player));
            }

            EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                    .withInvisible()
                    .withNoGravity()
                    .withTextDisplayText(textLines)
                    .withBillboard(billboard)
                    .withTextAlignment(alignment)
                    .withTextBackgroundColor(backgroundColor)
                    .withTextLineWidth(lineWidth)
                    .withDisplayProperties(group.lines, hologram);

            List<SynchedEntityData.DataItem<?>> metadata = metadataBuilder.toWatchableObjects();

            EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                    .withEntityMetadata(group.frontEntityId, metadata);

            if (doubleSided) {
                packetsBuilder.withEntityMetadata(group.backEntityId, metadata);
            }

            packetsBuilder.sendTo(player);
        }
    }

    /**
     * 销毁所有文本行组实体（指定玩家）
     */
    public void destroy(Player player) {
        EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create();
        for (TextGroup group : textGroups) {
            packetsBuilder.withRemoveEntity(group.frontEntityId);
            packetsBuilder.withRemoveEntity(group.backEntityId);
        }
        packetsBuilder.sendTo(player);
        lastTextPerPlayerGroup.remove(player.getUniqueId());
    }

    /**
     * 销毁所有状态（不发送包，需在调用前先对所有viewer发送destroy）
     */
    public void destroyAll() {
        destroyed = true;
        lastTextPerPlayerGroup.clear();
    }

    /**
     * 传送所有文本行组实体
     */
    public void teleport(Player player) {
        if (destroyed) return;

        for (TextGroup group : textGroups) {
            Location groupLocation = group.lines.get(0).getLocation();
            if (groupLocation == null) continue;

            EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                    .withTeleportEntity(group.frontEntityId, new HologramPosition(
                            groupLocation.getX(), groupLocation.getY(), groupLocation.getZ(),
                            currentYaw, currentPitch));

            if (currentDoubleSided) {
                packetsBuilder.withTeleportEntity(group.backEntityId, new HologramPosition(
                        groupLocation.getX(), groupLocation.getY(), groupLocation.getZ(),
                        currentYaw + 180.0f, currentPitch));
            }

            packetsBuilder.sendTo(player);
        }
    }

    /**
     * 获取所有实体ID（用于注册和点击检测）
     */
    public List<Integer> getEntityIds() {
        List<Integer> ids = new ArrayList<>();
        for (TextGroup group : textGroups) {
            ids.add(group.frontEntityId);
            ids.add(group.backEntityId);
        }
        return ids;
    }

    /**
     * 根据实体ID查找对应的行（用于点击检测）
     */
    public HologramLine getLineByEntityId(int entityId) {
        for (TextGroup group : textGroups) {
            if (group.frontEntityId == entityId || group.backEntityId == entityId) {
                return group.lines.isEmpty() ? null : group.lines.get(0);
            }
        }
        return null;
    }

    /**
     * 根据实体ID和点击Y坐标查找对应的行（用于合并 TextDisplay 的行路由）
     *
     * TextDisplay 实体位置在文本顶部，hitY 是相对于实体位置的 Y 偏移（Y 向上为正）。
     * 文本从实体位置向下渲染：第一行在顶部（hitY ≈ 0），最后一行在底部（hitY 为负值）。
     * 因此：lineIndex = (int)(-hitY / lineHeight)
     *
     * @param entityId 实体 ID
     * @param hitY     点击位置相对于实体位置的 Y 偏移，null 表示无法确定（退回到第一行）
     * @return 对应的行，如果不存在返回 null
     */
    public HologramLine getLineByEntityId(int entityId, Float hitY) {
        for (TextGroup group : textGroups) {
            if (group.frontEntityId == entityId || group.backEntityId == entityId) {
                if (group.lines.isEmpty()) {
                    return null;
                }
                // 单行组或无 Y 坐标时无需路由
                if (group.lines.size() == 1 || hitY == null) {
                    return group.lines.get(0);
                }
                double lineHeight = page.getParent() != null ? page.getParent().getLineHeight() : 0.25;
                // hitY 从顶部(0)到底部(负值)，取反后除以行高得到行索引
                int lineIndex = (int) (-hitY / lineHeight);
                // 边界保护
                lineIndex = Math.max(0, Math.min(lineIndex, group.lines.size() - 1));
                return group.lines.get(lineIndex);
            }
        }
        return null;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void reset() {
        destroyed = false;
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        currentDoubleSided = false;
        lastTextPerPlayerGroup.clear();
    }
}
