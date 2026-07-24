package com.oolongho.holograms.nms.versions.renderer;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.*;
import com.oolongho.holograms.nms.renderer.NmsClickableHologramRenderer;
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
 *
 * 每个 TextGroup 自带一个 Interaction 实体（NmsClickableHologramRenderer），
 * 用于精确的行级点击检测。Interaction 的位置和尺寸与 TextGroup 匹配。
 * 仅当 page.isClickable() 为 true 时，Interaction 才会被显示给玩家。
 */
public class PageTextRendererImpl {

    /** 一个文本行组：连续的、X/Z 偏移相同的 TEXT 行 */
    private static class TextGroup {
        final int frontEntityId;
        final int backEntityId;
        final NmsClickableHologramRenderer interactionRenderer;
        final int debugBoxEntityId;
        final List<HologramLine> lines;

        TextGroup(int frontEntityId, int backEntityId,
                  NmsClickableHologramRenderer interactionRenderer,
                  int debugBoxEntityId,
                  List<HologramLine> lines) {
            this.frontEntityId = frontEntityId;
            this.backEntityId = backEntityId;
            this.interactionRenderer = interactionRenderer;
            this.debugBoxEntityId = debugBoxEntityId;
            this.lines = lines;
        }

        /** 返回 Interaction 实体 ID，若无 renderer 返回 -1 */
        int interactionEntityId() {
            return interactionRenderer == null ? -1 : interactionRenderer.getEntityId();
        }
    }

    /** Interaction 实体的位置和尺寸参数 */
    private record InteractionBounds(double x, double y, double z, float width, float height) {}

    /**
     * Interaction 高度系数（每行判定高度 = lineHeight × scaleY × 此系数）
     * 1.0 = 覆盖完整行高（推荐，与文字渲染范围一致）
     * 0.5 = 仅覆盖字体可见高度（行间存在死区，多行组会出现部分文字无法点击）
     * 调小：判定区域变窄，点击更精准但更难点中
     * 调大：判定区域变宽，更容易点中但容易误触相邻行
     */
    private static final double INTERACTION_HEIGHT_RATIO = 1.0;

    /**
     * Interaction 垂直偏移（格）
     * 正值：判定区域上移（远离地面）
     * 负值：判定区域下移（靠近地面）
     * 用于微调判定区域与可见文字的垂直对齐
     * 默认 0.0
     */
    private static final double INTERACTION_VERTICAL_SHIFT = 0.0;

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
     * 计算 TextGroup 的 Interaction 实体边界参数
     * 位置 = 首行位置 + 垂直偏移（文本从首行位置向上渲染）
     * 宽度 = 1.0 × scaleX（避免相邻 TextGroup 的 Interaction 水平重叠，随缩放）
     * 高度 = N × lineHeight × scaleY × INTERACTION_HEIGHT_RATIO
     *
     * @param group 文本组
     * @return 边界参数；若 scaleX/scaleY ≤ 0、位置无效或 parent 为 null 返回 null
     */
    private InteractionBounds computeInteractionBounds(TextGroup group) {
        if (group.lines.isEmpty()) return null;
        Hologram hologram = page.getParent();
        if (hologram == null) return null;
        float scaleX = hologram.getScaleX();
        float scaleY = hologram.getScaleY();
        if (scaleX <= 0 || scaleY <= 0) return null;
        Location firstLine = group.lines.get(0).getLocation();
        if (firstLine == null) return null;
        double lineHeight = hologram.getLineHeight();
        float width = 1.0f * scaleX;
        float height = (float) (group.lines.size() * lineHeight * scaleY * INTERACTION_HEIGHT_RATIO);
        double interactionY = firstLine.getY() + INTERACTION_VERTICAL_SHIFT;
        return new InteractionBounds(firstLine.getX(), interactionY, firstLine.getZ(), width, height);
    }

    /**
     * 重建文本行组
     * 遍历页面所有行，将连续的、X/Z 偏移相同的 TEXT 行分为一组。
     * 偏移变化（offsetX 或 offsetZ 不同）触发分裂；Y 偏移不触发分裂。
     * 仅当 page.isClickable() 为 true 时，为每个组创建 Interaction 渲染器。
     */
    public void rebuildGroups() {
        // 先移除旧实体，避免实体ID泄漏
        if (!textGroups.isEmpty()) {
            EntityPacketsBuilder removePackets = EntityPacketsBuilder.create();
            for (TextGroup group : textGroups) {
                removePackets.withRemoveEntity(group.frontEntityId);
                removePackets.withRemoveEntity(group.backEntityId);
                if (group.interactionEntityId() != -1) {
                    removePackets.withRemoveEntity(group.interactionEntityId());
                }
                if (group.debugBoxEntityId != -1) {
                    removePackets.withRemoveEntity(group.debugBoxEntityId);
                }
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

        boolean clickable = page.isClickable();
        // 主组：所有 X/Z 偏移为零的行合并到此（偏移行分裂后不重置主组）
        TextGroup mainGroup = null;
        // 当前偏移组：连续相同 X/Z 偏移的行
        List<HologramLine> offsetGroupLines = new ArrayList<>();
        double currentOffsetX = 0;
        double currentOffsetZ = 0;
        boolean offsetGroupStarted = false;

        for (HologramLine line : page.getLines()) {
            if (line.getType() == HologramType.TEXT) {
                boolean hasXZOffset = line.getOffsetX() != 0.0 || line.getOffsetZ() != 0.0;

                if (!hasXZOffset) {
                    // X/Z 偏移为零：提交当前偏移组，合并到主组
                    if (offsetGroupStarted) {
                        textGroups.add(createTextGroup(new ArrayList<>(offsetGroupLines), clickable));
                        offsetGroupLines.clear();
                        offsetGroupStarted = false;
                    }
                    if (mainGroup == null) {
                        mainGroup = createTextGroup(new ArrayList<>(), clickable);
                        textGroups.add(mainGroup);
                    }
                    mainGroup.lines.add(line);
                } else {
                    // X/Z 偏移非零：开始/继续偏移组
                    if (!offsetGroupStarted) {
                        offsetGroupLines.add(line);
                        currentOffsetX = line.getOffsetX();
                        currentOffsetZ = line.getOffsetZ();
                        offsetGroupStarted = true;
                    } else if (line.getOffsetX() != currentOffsetX || line.getOffsetZ() != currentOffsetZ) {
                        // X/Z 偏移变化，提交当前偏移组并开启新偏移组
                        textGroups.add(createTextGroup(new ArrayList<>(offsetGroupLines), clickable));
                        offsetGroupLines.clear();
                        offsetGroupLines.add(line);
                        currentOffsetX = line.getOffsetX();
                        currentOffsetZ = line.getOffsetZ();
                    } else {
                        // 偏移相同，加入当前偏移组
                        offsetGroupLines.add(line);
                    }
                }
            } else {
                // 非 TEXT 行：提交当前偏移组，重置主组（非 TEXT 行打断主组连续性）
                if (offsetGroupStarted) {
                    textGroups.add(createTextGroup(new ArrayList<>(offsetGroupLines), clickable));
                    offsetGroupLines.clear();
                    offsetGroupStarted = false;
                }
                mainGroup = null;
            }
        }
        // 处理末尾的偏移组
        if (offsetGroupStarted) {
            textGroups.add(createTextGroup(new ArrayList<>(offsetGroupLines), clickable));
        }

        // 分组重建：输出最终分组结果
        WooHolograms.getInstance().debug(() -> formatGroupsLog());
    }

    /**
     * 格式化分组 debug 日志
     */
    private String formatGroupsLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[Debug.group] rebuild, hologram=%s, page=%d, groupCount=%d, clickable=%s",
                page.getParent() == null ? "null" : page.getParent().getName(),
                page.getIndex(), textGroups.size(), page.isClickable()));
        for (int i = 0; i < textGroups.size(); i++) {
            TextGroup g = textGroups.get(i);
            sb.append(String.format("%n  group[%d] lines=%d, frontId=%d, interactionId=%d",
                    i, g.lines.size(), g.frontEntityId, g.interactionEntityId()));
            for (int j = 0; j < g.lines.size(); j++) {
                HologramLine line = g.lines.get(j);
                Location loc = line.getLocation();
                sb.append(String.format("%n    line[%d] content=%.30s, offset=(%.2f,%.2f,%.2f), loc=%.2f,%.2f,%.2f",
                        j,
                        line.getContent() == null ? "" : line.getContent().replace('\n', ' '),
                        line.getOffsetX(), line.getOffsetY(), line.getOffsetZ(),
                        loc == null ? 0 : loc.getX(), loc == null ? 0 : loc.getY(), loc == null ? 0 : loc.getZ()));
            }
        }
        return sb.toString();
    }

    /**
     * 创建一个 TextGroup，根据 clickable 参数决定是否创建 Interaction 渲染器
     * 调试框（半透明玻璃标记判定区域）仅在 config.yml 的 debug=true 时创建，
     * /wh reload 后通过 rebuildGroups 重建生效。
     */
    private TextGroup createTextGroup(List<HologramLine> lines, boolean clickable) {
        int frontId = entityIdGenerator.getFreeEntityId();
        int backId = entityIdGenerator.getFreeEntityId();
        NmsClickableHologramRenderer interactionRenderer = clickable
                ? WooHolograms.getInstance().getRendererFactory().createClickableRenderer()
                : null;
        int debugBoxId = (clickable && WooHolograms.getInstance().getConfigManager().isDebug())
                ? entityIdGenerator.getFreeEntityId()
                : -1;
        return new TextGroup(frontId, backId, interactionRenderer, debugBoxId, lines);
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
     * 销毁所有文本行组实体（指定玩家），包含 TextDisplay 和 Interaction
     */
    public void destroy(Player player) {
        EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create();
        for (TextGroup group : textGroups) {
            packetsBuilder.withRemoveEntity(group.frontEntityId);
            packetsBuilder.withRemoveEntity(group.backEntityId);
            if (group.interactionEntityId() != -1) {
                packetsBuilder.withRemoveEntity(group.interactionEntityId());
            }
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
     * 获取所有实体ID（用于注册和点击检测），包含 TextDisplay 和 Interaction
     */
    public List<Integer> getEntityIds() {
        List<Integer> ids = new ArrayList<>();
        for (TextGroup group : textGroups) {
            ids.add(group.frontEntityId);
            ids.add(group.backEntityId);
            int interactionId = group.interactionEntityId();
            if (interactionId != -1) {
                ids.add(interactionId);
            }
        }
        return ids;
    }

    /**
     * 根据实体ID和点击Y坐标查找对应的行（用于点击检测）
     *
     * 支持两种实体：
     * 1. TextDisplay（frontEntityId/backEntityId）：位置在组顶部（首行），
     *    hitY 从顶部(0)到底部(负值)，effectiveLineHeight = lineHeight × scaleY，
     *    lineIndex = (int)(-hitY / effectiveLineHeight)
     * 2. Interaction（interactionEntityId）：位置 = 首行位置 + 垂直偏移，
     *    AABB 向上延伸 height，hitY 从底部(0)到顶部(height)，
     *    effectiveLineHeight = lineHeight × scaleY × INTERACTION_HEIGHT_RATIO（与 height 计算一致），
     *    lineIndex = size - 1 - (int)(hitY / effectiveLineHeight)
     *
     * @param entityId 实体 ID
     * @param hitY     点击位置相对于实体位置的 Y 偏移，null 表示无法确定（退回到第一行）
     * @return 对应的行，如果不存在返回 null
     */
    public HologramLine getLineByEntityId(int entityId, Float hitY) {
        for (TextGroup group : textGroups) {
            boolean isTextDisplay = group.frontEntityId == entityId || group.backEntityId == entityId;
            int interactionId = group.interactionEntityId();
            boolean isInteraction = interactionId != -1 && interactionId == entityId;

            if (isTextDisplay || isInteraction) {
                if (group.lines.isEmpty()) {
                    return null;
                }
                // 单行组或无 Y 坐标时无需路由
                if (group.lines.size() == 1 || hitY == null) {
                    return group.lines.get(0);
                }
                Hologram hologram = page.getParent();
                double lineHeight = hologram != null ? hologram.getLineHeight() : 0.25;
                float scaleY = hologram != null ? hologram.getScaleY() : 1.0f;
                int lineIndex;
                if (isInteraction) {
                    // Interaction: 每行判定高度 = lineHeight × scaleY × INTERACTION_HEIGHT_RATIO（与 height 计算一致）
                    double effectiveLineHeight = lineHeight * scaleY * INTERACTION_HEIGHT_RATIO;
                    if (effectiveLineHeight <= 0) {
                        return group.lines.get(0); // 退化保护
                    }
                    // hitY=0 在底部（最后一行），hitY=height 在顶部（第一行）
                    lineIndex = group.lines.size() - 1 - (int) (hitY / effectiveLineHeight);
                } else {
                    // TextDisplay: 每行占用高度 = lineHeight × scaleY，hitY 从顶部(0)到底部(负值)
                    double effectiveLineHeight = lineHeight * scaleY;
                    if (effectiveLineHeight <= 0) {
                        return group.lines.get(0); // 退化保护
                    }
                    lineIndex = (int) (-hitY / effectiveLineHeight);
                }
                // 边界保护
                lineIndex = Math.max(0, Math.min(lineIndex, group.lines.size() - 1));
                return group.lines.get(lineIndex);
            }
        }
        return null;
    }

    /*
     * Interaction 实体管理方法
     */

    /**
     * 用玩家视线计算点击 Interaction 实体的 hitY
     * 用于左键（ATTACK）时 Minecraft 不发送 INTERACT_AT 包、hitY=null 的情况
     *
     * Interaction 位置 = 首行位置（文本底部），AABB 向上延伸 height，
     * 返回值与 INTERACT_AT 的 hitY 语义一致：相对于实体底部的 Y 偏移
     *
     * @param player   玩家
     * @param entityId Interaction 实体 ID
     * @return 相对于实体底部的 Y 偏移，null 表示无法计算（射线未击中或组不存在）
     */
    public Float calculateHitYFromRay(Player player, int entityId) {
        for (TextGroup group : textGroups) {
            int interactionId = group.interactionEntityId();
            if (interactionId == -1 || interactionId != entityId) continue;
            InteractionBounds bounds = computeInteractionBounds(group);
            if (bounds == null) return null;
            Location entityLoc = new Location(
                    player.getWorld(),
                    bounds.x(),
                    bounds.y(),
                    bounds.z());
            return rayTraceHitY(player, entityLoc, bounds.width(), bounds.height());
        }
        return null;
    }

    /**
     * 射线-AABB 相交（slab 法），计算击中点相对于 AABB 底部的 Y 偏移
     * AABB 中心在 (entityLoc.x, entityLoc.z)，X/Z 范围 ±(width/2)，Y 从 entityLoc.y 向上延伸 height
     *
     * @param player    玩家
     * @param entityLoc AABB 底部中心位置
     * @param width     AABB 宽度（X/Z 方向）
     * @param height    AABB 高度（Y 方向）
     * @return 相对于 entityLoc.y 的 Y 偏移，null 表示射线未击中 AABB
     */
    private Float rayTraceHitY(Player player, Location entityLoc, float width, double height) {
        org.bukkit.Location eye = player.getEyeLocation();
        org.bukkit.util.Vector dir = eye.getDirection();
        org.bukkit.util.Vector origin = eye.toVector();
        double halfWidth = width / 2.0;
        org.bukkit.util.Vector min = new org.bukkit.util.Vector(
                entityLoc.getX() - halfWidth, entityLoc.getY(), entityLoc.getZ() - halfWidth);
        org.bukkit.util.Vector max = new org.bukkit.util.Vector(
                entityLoc.getX() + halfWidth, entityLoc.getY() + height, entityLoc.getZ() + halfWidth);

        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        // X 轴
        if (Math.abs(dir.getX()) < 1e-8) {
            if (origin.getX() < min.getX() || origin.getX() > max.getX()) return null;
        } else {
            double t1 = (min.getX() - origin.getX()) / dir.getX();
            double t2 = (max.getX() - origin.getX()) / dir.getX();
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        // Y 轴
        if (Math.abs(dir.getY()) < 1e-8) {
            if (origin.getY() < min.getY() || origin.getY() > max.getY()) return null;
        } else {
            double t1 = (min.getY() - origin.getY()) / dir.getY();
            double t2 = (max.getY() - origin.getY()) / dir.getY();
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        // Z 轴
        if (Math.abs(dir.getZ()) < 1e-8) {
            if (origin.getZ() < min.getZ() || origin.getZ() > max.getZ()) return null;
        } else {
            double t1 = (min.getZ() - origin.getZ()) / dir.getZ();
            double t2 = (max.getZ() - origin.getZ()) / dir.getZ();
            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }
        if (tmax < tmin || tmax < 0) return null; // 无交点
        double t = tmin >= 0 ? tmin : tmax;
        double hitWorldY = origin.getY() + t * dir.getY();
        return (float) (hitWorldY - entityLoc.getY());
    }

    /**
     * 显示所有 TextGroup 的 Interaction 实体给指定玩家
     * 仅对有 interactionRenderer 的组生效（即 page.isClickable() 为 true 时创建的组）
     *
     * Interaction 位置 = 首行位置 + 垂直偏移，AABB 向上延伸 height
     *   - hitY=0 对应底部（最后一行）
     *   - hitY=height 对应顶部（第一行）
     * width = 1.0 × scaleX，height = N × lineHeight × scaleY × INTERACTION_HEIGHT_RATIO
     *
     * 当 config.yml 的 debug 为 true 时，同时显示半透明玻璃框标记判定区域
     */
    public void showClickableEntities(Player player) {
        if (destroyed) return;
        final String playerName = player.getName();
        for (int gi = 0; gi < textGroups.size(); gi++) {
            TextGroup group = textGroups.get(gi);
            if (group.interactionRenderer == null) continue;
            InteractionBounds bounds = computeInteractionBounds(group);
            if (bounds == null) continue;
            group.interactionRenderer.display(player,
                    new HologramPosition(bounds.x(), bounds.y(), bounds.z()),
                    bounds.width(), bounds.height());

            // Interaction 显示：输出位置和尺寸
            final int gIdx = gi;
            final int lineCount = group.lines.size();
            WooHolograms.getInstance().debug(() -> String.format(
                    "[Debug.interaction] spawn, player=%s, group=%d, lines=%d, entityId=%d, pos=(%.2f,%.2f,%.2f), w=%.2f, h=%.2f",
                    playerName, gIdx, lineCount, group.interactionEntityId(),
                    bounds.x(), bounds.y(), bounds.z(), bounds.width(), bounds.height()));

            if (group.debugBoxEntityId != -1) {
                spawnDebugBox(player, group, bounds);
            }
        }
    }

    /**
     * 生成调试用半透明玻璃框，标记 Interaction 判定区域
     * 使用 BlockDisplay 实体，白色彩色玻璃，缩放至 Interaction 尺寸
     *
     * BlockDisplay 方块以实体位置为角点（不是中心），方块占据 [x, x+scale] 范围
     * Interaction AABB 以实体位置为中心，占据 [x-width/2, x+width/2] 范围
     * 因此需要 translation：
     *   - X/Z 方向平移 -width/2，让方块从 [x, x+width] 变为 [x-width/2, x+width/2]
     *   - Y 方向不平移（方块默认从 y 向上渲染到 y+height，与 Interaction AABB [y, y+height] 一致）
     */
    private void spawnDebugBox(Player player, TextGroup group, InteractionBounds bounds) {
        float w = bounds.width();
        float h = bounds.height();
        EntityPacketsBuilder.create()
                .withSpawnEntity(group.debugBoxEntityId, org.bukkit.entity.EntityType.BLOCK_DISPLAY,
                        new HologramPosition(bounds.x(), bounds.y(), bounds.z()),
                        0f, 0f)
                .withEntityMetadata(group.debugBoxEntityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withBlockState(org.bukkit.Material.WHITE_STAINED_GLASS)
                        .withScale(w, h, w)
                        .withTranslation(-w / 2.0, 0, -w / 2.0)
                        .withBillboard(Billboard.FIXED_ANGLE)
                        .toWatchableObjects())
                .sendTo(player);
    }

    /**
     * 传送所有 TextGroup 的 Interaction 实体（及调试框）
     * 位置与 showClickableEntities 保持一致
     */
    public void teleportClickableEntities(Player player) {
        if (destroyed) return;
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            InteractionBounds bounds = computeInteractionBounds(group);
            if (bounds == null) continue;
            group.interactionRenderer.move(player,
                    new HologramPosition(bounds.x(), bounds.y(), bounds.z()));

            if (group.debugBoxEntityId != -1) {
                EntityPacketsBuilder.create()
                        .withTeleportEntity(group.debugBoxEntityId,
                                new HologramPosition(bounds.x(), bounds.y(), bounds.z()))
                        .sendTo(player);
            }
        }
    }

    /**
     * 隐藏所有 TextGroup 的 Interaction 实体及调试框（指定玩家）
     */
    public void hideClickableEntities(Player player) {
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            group.interactionRenderer.destroy(player);
            if (group.debugBoxEntityId != -1) {
                EntityPacketsBuilder.create()
                        .withRemoveEntity(group.debugBoxEntityId)
                        .sendTo(player);
            }
        }
    }

    /**
     * 全局销毁所有 Interaction 渲染器（不发送包，调用方需先对所有 viewer 发送 hideClickableEntities）
     * 注销所有 Interaction entityId
     */
    public void destroyClickableEntities() {
        HologramManager manager = WooHolograms.getInstance().getHologramManager();
        List<Player> viewers = new ArrayList<>();
        for (UUID uuid : lastTextPerPlayerGroup.keySet()) {
            Player p = org.bukkit.Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) viewers.add(p);
        }
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            int interactionId = group.interactionEntityId();
            manager.unregisterEntityId(interactionId);
            for (Player viewer : viewers) {
                group.interactionRenderer.destroy(viewer);
            }
        }
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
