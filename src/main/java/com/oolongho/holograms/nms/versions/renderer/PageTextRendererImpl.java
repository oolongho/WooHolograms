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
        final List<HologramLine> lines;

        TextGroup(int frontEntityId, int backEntityId,
                  NmsClickableHologramRenderer interactionRenderer,
                  List<HologramLine> lines) {
            this.frontEntityId = frontEntityId;
            this.backEntityId = backEntityId;
            this.interactionRenderer = interactionRenderer;
            this.lines = lines;
        }

        /** 返回 Interaction 实体 ID，若无 renderer 返回 -1 */
        int interactionEntityId() {
            return interactionRenderer == null ? -1 : interactionRenderer.getEntityId();
        }
    }

    /** Interaction 实体的位置和尺寸参数 */
    private record InteractionBounds(double x, double y, double z, float width, float height) {}

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
     * 位置 = 首行位置 - (0, height, 0)（文本底部，与 TextDisplay 渲染底部对齐）
     * 宽度 = 2.0 × scaleX（覆盖常见文本宽度，随缩放）
     * 高度 = N × lineHeight × scaleY（与文本显示高度一致）
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
        float width = 2.0f * scaleX;
        float height = (float) (group.lines.size() * lineHeight * scaleY);
        double interactionY = firstLine.getY() - height;
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
    }

    /**
     * 创建一个 TextGroup，根据 clickable 参数决定是否创建 Interaction 渲染器
     */
    private TextGroup createTextGroup(List<HologramLine> lines, boolean clickable) {
        int frontId = entityIdGenerator.getFreeEntityId();
        int backId = entityIdGenerator.getFreeEntityId();
        NmsClickableHologramRenderer interactionRenderer = clickable
                ? WooHolograms.getInstance().getRendererFactory().createClickableRenderer()
                : null;
        return new TextGroup(frontId, backId, interactionRenderer, lines);
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
     * 1. TextDisplay（frontEntityId/backEntityId）：位置在组顶部，
     *    hitY 从顶部(0)到底部(负值)，lineIndex = (int)(-hitY / lineHeight)
     * 2. Interaction（interactionEntityId）：位置在组底部（最后一行位置），
     *    AABB 向上延伸 height，hitY 从底部(0)到顶部(height)，
     *    lineIndex = size - 1 - (int)(hitY / lineHeight)
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
                double lineHeight = page.getParent() != null ? page.getParent().getLineHeight() : 0.25;
                int lineIndex;
                if (isInteraction) {
                    // Interaction 位置在组底部（最后一行），AABB 向上延伸
                    // hitY=0 在底部（最后一行），hitY=height 在顶部（第一行）
                    // 从底部往上数：lineIndex = size - 1 - (int)(hitY / lineHeight)
                    lineIndex = group.lines.size() - 1 - (int) (hitY / lineHeight);
                } else {
                    // TextDisplay：hitY 从顶部(0)到底部(负值)
                    lineIndex = (int) (-hitY / lineHeight);
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
     * Interaction 位置在组底部（最后一行位置），AABB 向上延伸 height，
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
            if (group.lines.isEmpty()) return null;
            Location groupLocation = group.lines.get(group.lines.size() - 1).getLocation();
            if (groupLocation == null) return null;
            double lineHeight = page.getParent() != null ? page.getParent().getLineHeight() : 0.25;
            double height = group.lines.size() * lineHeight;
            return rayTraceHitY(player, groupLocation, height);
        }
        return null;
    }

    /**
     * 射线-AABB 相交（slab 法），计算击中点相对于 AABB 底部的 Y 偏移
     * Interaction 实体 width=1.0，AABB 从 entityLoc 向上延伸 height
     */
    private Float rayTraceHitY(Player player, Location entityLoc, double height) {
        org.bukkit.Location eye = player.getEyeLocation();
        org.bukkit.util.Vector dir = eye.getDirection();
        org.bukkit.util.Vector origin = eye.toVector();
        // Interaction width=1.0，AABB 中心在 entityLoc.x/z，范围 ±0.5
        org.bukkit.util.Vector min = new org.bukkit.util.Vector(
                entityLoc.getX() - 0.5, entityLoc.getY(), entityLoc.getZ() - 0.5);
        org.bukkit.util.Vector max = new org.bukkit.util.Vector(
                entityLoc.getX() + 0.5, entityLoc.getY() + height, entityLoc.getZ() + 0.5);

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
     * Interaction 位置使用组内最后一行位置（底部），AABB 向上延伸覆盖所有行：
     *   - hitY=0 对应最后一行（底部）
     *   - hitY=height 对应第一行（顶部）
     */
    public void showClickableEntities(Player player) {
        if (destroyed) return;
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            if (group.lines.isEmpty()) continue;
            // 使用最后一行位置作为 Interaction 位置（AABB 底部）
            Location groupLocation = group.lines.get(group.lines.size() - 1).getLocation();
            if (groupLocation == null) continue;
            double lineHeight = page.getParent() != null ? page.getParent().getLineHeight() : 0.25;
            float height = (float) (group.lines.size() * lineHeight);
            group.interactionRenderer.display(player,
                    new HologramPosition(groupLocation.getX(), groupLocation.getY(), groupLocation.getZ()),
                    1.0f, height);
        }
    }

    /**
     * 传送所有 TextGroup 的 Interaction 实体
     * 位置使用组内最后一行位置（与 showClickableEntities 保持一致）
     */
    public void teleportClickableEntities(Player player) {
        if (destroyed) return;
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            if (group.lines.isEmpty()) continue;
            Location groupLocation = group.lines.get(group.lines.size() - 1).getLocation();
            if (groupLocation == null) continue;
            group.interactionRenderer.move(player,
                    new HologramPosition(groupLocation.getX(), groupLocation.getY(), groupLocation.getZ()));
        }
    }

    /**
     * 隐藏所有 TextGroup 的 Interaction 实体（指定玩家）
     */
    public void hideClickableEntities(Player player) {
        for (TextGroup group : textGroups) {
            if (group.interactionRenderer == null) continue;
            group.interactionRenderer.destroy(player);
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
