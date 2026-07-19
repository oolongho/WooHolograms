package com.oolongho.holograms.hologram;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.HologramManager;
import com.oolongho.holograms.nms.versions.renderer.PageTextRendererImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 全息图页
 * 表示全息图的一页，包含多行内容
 * 参考 DecentHolograms 的 HologramPage 实现
 * 
 */
public class HologramPage {

    /*
     * 字段
     */

    private int index;
    private final Hologram parent;

    // 行列表
    private final List<HologramLine> lines;

    // 点击动作
    private final Map<ClickType, List<Action>> actions;

    // 标志
    private final Set<EnumFlag> flags;

    // 页面级文本渲染器（合并连续TEXT行为单个TextDisplay实体）
    private volatile PageTextRendererImpl pageTextRenderer;

    // pageTextRenderer 惰性创建的锁对象
    private final Object textRendererLock = new Object();

    /*
     * 构造函数
     */

    /**
     * 创建全息图页
     * 
     * @param parent 父全息图
     * @param index 页索引
     */
    public HologramPage(Hologram parent, int index) {
        this.parent = parent;
        this.index = index;
        this.lines = new ArrayList<>();
        this.actions = new EnumMap<>(ClickType.class);
        this.flags = ConcurrentHashMap.newKeySet();
    }

    /*
     * 常规方法
     */

    /**
     * 获取父全息图
     * 
     * @return 父全息图
     */
    public Hologram getParent() {
        return parent;
    }

    /**
     * 获取页索引
     * 
     * @return 页索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 设置页索引
     * 
     * @param index 页索引
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 获取此页的高度
     * 
     * @return 高度
     */
    public double getHeight() {
        double height = 0.0;
        for (HologramLine line : lines) {
            if (line.getType() == HologramType.TEXT) {
                // TEXT 行使用 hologram 的 lineHeight
                if (parent != null) {
                    height += parent.getLineHeight();
                } else {
                    height += line.getHeight();
                }
            } else {
                height += line.getHeight();
            }
        }
        return height;
    }

    /**
     * 获取此页的中心位置
     * 
     * @return 中心位置
     */
    public Location getCenter() {
        if (parent == null) {
            return null;
        }

        Location center = parent.getLocation().clone();
        center.subtract(0, getHeight() / 2, 0);
        return center;
    }

    /**
     * 获取行数
     * 
     * @return 行数
     */
    public int size() {
        return lines.size();
    }
    
    /**
     * 检查是否为空
     * 
     * @return 是否为空
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /*
     * 行管理方法
     */

    /**
     * 获取所有行
     * 
     * @return 行列表（不可变）
     */
    public List<HologramLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * 获取指定行
     * 
     * @param index 行索引
     * @return 行，如果不存在返回 null
     */
    public HologramLine getLine(int index) {
        if (index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }

    /**
     * 添加行到末尾
     * 
     * @param line 行
     * @return 是否成功
     */
    public boolean addLine(HologramLine line) {
        return insertLine(size(), line);
    }

    /**
     * 添加新行
     * 
     * @param content 内容
     * @return 新添加的行
     */
    public HologramLine addLine(String content) {
        HologramLine line = new HologramLine(this, getNextLineLocation(), content);
        if (addLine(line)) {
            return line;
        }
        return null;
    }

    /**
     * 在指定位置插入行
     * 
     * @param index 索引
     * @param line 行
     * @return 是否成功
     */
    public boolean insertLine(int index, HologramLine line) {
        if (index < 0 || index > size()) {
            return false;
        }

        lines.add(index, line);

        // 重建 PageTextRendererImpl（行结构变化，内部已销毁旧实体）
        rebuildPageTextRenderer();

        // 显示给当前观看者
        if (parent != null) {
            Set<Player> viewers = parent.getViewerPlayers(this.index);
            for (Player player : viewers) {
                if (player != null && player.isOnline()) {
                    // 非 TEXT 行直接显示
                    if (line.getType() != HologramType.TEXT) {
                        line.show(player);
                    }
                }
            }
            // 重新渲染所有文本（rebuildPageTextRenderer 已销毁旧实体，只需 render）
            if (pageTextRenderer != null) {
                for (Player player : parent.getViewerPlayers(this.index)) {
                    if (player != null && player.isOnline()) {
                        pageTextRenderer.render(player, parent.getLocation());
                        // rebuildPageTextRenderer 销毁了旧 Interaction，需重新显示
                        showClickableEntities(player);
                    }
                }
            }
        }

        realignLines();
        return true;
    }

    /**
     * 在指定位置插入新行
     * 
     * @param index 索引
     * @param content 内容
     * @return 是否成功
     */
    public boolean insertLine(int index, String content) {
        HologramLine line = new HologramLine(this, getNextLineLocation(), content);
        return insertLine(index, line);
    }

    /**
     * 设置指定行的内容
     * 
     * @param index 索引
     * @param content 内容
     * @return 是否成功
     */
    public boolean setLine(int index, String content) {
        HologramLine line = getLine(index);
        if (line == null) {
            return false;
        }

        HologramType previousType = line.getType();
        line.setContent(content);

        if (line.getType() != previousType) {
            // 类型变化，需要重建 PageTextRendererImpl
            rebuildPageTextRenderer();
            // 重新渲染整个页面给所有观看者
            if (parent != null) {
                for (Player player : parent.getViewerPlayers(this.index)) {
                    if (player != null && player.isOnline()) {
                        hideFromPlayer(player);
                        showTo(player);
                        // rebuildPageTextRenderer 销毁了旧 Interaction，需重新显示
                        showClickableEntities(player);
                    }
                }
            }
            realignLines();
        } else if (line.getType() == HologramType.TEXT) {
            // TEXT 行内容变化，更新 PageTextRendererImpl
            if (pageTextRenderer != null && parent != null) {
                for (Player player : parent.getViewerPlayers(this.index)) {
                    if (player != null && player.isOnline()) {
                        pageTextRenderer.updateText(player);
                    }
                }
            }
        }

        return true;
    }

    /**
     * 移除指定行
     * 
     * @param index 索引
     * @return 被移除的行
     */
    public HologramLine removeLine(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }

        HologramLine line = lines.remove(index);
        if (line != null) {
            line.destroy();
            // 重建 PageTextRendererImpl（行结构变化，内部已销毁旧实体）
            rebuildPageTextRenderer();
            // 重新渲染所有文本（rebuildPageTextRenderer 已销毁旧实体，只需 render）
            if (pageTextRenderer != null && parent != null) {
                for (Player player : parent.getViewerPlayers(this.index)) {
                    if (player != null && player.isOnline()) {
                        pageTextRenderer.render(player, parent.getLocation());
                        // rebuildPageTextRenderer 销毁了旧 Interaction，需重新显示
                        showClickableEntities(player);
                    }
                }
            }
            realignLines();
        }

        return line;
    }

    /**
     * 交换两行
     * 
     * @param index1 第一行索引
     * @param index2 第二行索引
     * @return 是否成功
     */
    public boolean swapLines(int index1, int index2) {
        if (index1 < 0 || index1 >= size() || index2 < 0 || index2 >= size()) {
            return false;
        }

        Collections.swap(lines, index1, index2);
        rebuildPageTextRenderer();
        realignLines();

        // 重新渲染给所有观看者
        if (parent != null) {
            for (Player player : parent.getViewerPlayers(this.index)) {
                if (player != null && player.isOnline()) {
                    pageTextRenderer.render(player, parent.getLocation());
                    // rebuildPageTextRenderer 销毁了旧 Interaction，需重新显示
                    showClickableEntities(player);
                }
            }
        }

        return true;
    }

    /**
     * 清空所有行
     */
    public void clearLines() {
        for (HologramLine line : lines) {
            line.destroy();
        }
        lines.clear();
        // 行结构变化，重建 PageTextRendererImpl
        rebuildPageTextRenderer();
    }

    /**
     * 获取下一行的位置
     * 
     * @return 下一行位置
     */
    public Location getNextLineLocation() {
        if (size() == 0 || parent == null) {
            return parent != null ? parent.getLocation().clone() : null;
        }

        HologramLine lastLine = lines.get(lines.size() - 1);
        return lastLine.getLocation().clone().subtract(0, lastLine.getHeight(), 0);
    }

    /*
     * 行对齐方法
     */

    /**
     * 重新对齐所有行
     *
     * 分裂规则（与 PageTextRendererImpl.rebuildGroups 一致）：
     * - X/Z 偏移为零的行合并到主组，占据 Y 空间（连续排列）
     * - X/Z 偏移非零的行分裂为独立偏移组，不占据 Y 空间（悬浮在主组之上/之下）
     * - 非 TEXT 行打断主组连续性
     */
    public void realignLines() {
        if (parent == null) {
            return;
        }

        Location currentLocation = parent.getLocation().clone();
        double lineHeight = parent.getLineHeight();
        Location baseLocation = currentLocation.clone();

        for (HologramLine line : lines) {
            if (line.getType() == HologramType.TEXT) {
                Location lineLocation = line.getLocation();
                if (lineLocation != null) {
                    lineLocation.setX(currentLocation.getX() + line.getOffsetX());
                    lineLocation.setY(currentLocation.getY() + line.getOffsetY());
                    lineLocation.setZ(currentLocation.getZ() + line.getOffsetZ());
                    line.setLocation(lineLocation);
                }
                // X/Z 偏移为零的行占据 Y 空间，偏移行不占据（悬浮）
                boolean hasXZOffset = line.getOffsetX() != 0.0 || line.getOffsetZ() != 0.0;
                if (!hasXZOffset) {
                    currentLocation.subtract(0, lineHeight, 0);
                }
            } else {
                // 非 TEXT 行：使用各自的偏移和高度，占据 Y 空间
                Location lineLocation = line.getLocation();
                if (lineLocation != null) {
                    lineLocation.setX(currentLocation.getX() + line.getOffsetX());
                    lineLocation.setY(currentLocation.getY() + line.getOffsetY());
                    lineLocation.setZ(currentLocation.getZ() + line.getOffsetZ());
                    line.setLocation(lineLocation);
                    line.updateLocation(true);
                }
                currentLocation.subtract(0, line.getHeight(), 0);
            }
        }

        // 行对齐：输出基准位置和每行最终位置
        final String holoName = parent.getName();
        final int pageIdx = getIndex();
        final double baseY = baseLocation.getY();
        WooHolograms.getInstance().debug(() -> formatAlignLog(holoName, pageIdx, baseY, lineHeight));

        // 更新 PageTextRendererImpl 的实体位置（TextDisplay + Interaction）
        if (pageTextRenderer != null) {
            for (UUID uuid : getViewers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                pageTextRenderer.teleport(p);
                // 行位置变化后，Interaction 也需同步传送
                teleportClickableEntities(p);
            }
        }
    }

    /**
     * 格式化行对齐 debug 日志
     */
    private String formatAlignLog(String holoName, int pageIdx, double baseY, double lineHeight) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[Debug.align] realign, hologram=%s, page=%d, baseY=%.3f, lineHeight=%.3f, lineCount=%d",
                holoName, pageIdx, baseY, lineHeight, lines.size()));
        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            Location loc = line.getLocation();
            sb.append(String.format("%n  line[%d] type=%s, offset=(%.2f,%.2f,%.2f), locY=%.3f",
                    i, line.getType(),
                    line.getOffsetX(), line.getOffsetY(), line.getOffsetZ(),
                    loc == null ? 0 : loc.getY()));
        }
        return sb.toString();
    }

    /*
     * 显示/隐藏方法
     */

    /**
     * 从玩家隐藏
     *
     * @param player 玩家
     */
    public void hideFrom(Player player) {
        if (player == null) {
            return;
        }

        // TEXT 行通过 PageTextRendererImpl 隐藏
        if (pageTextRenderer != null) {
            pageTextRenderer.destroy(player);
        }

        // 非 TEXT 行通过各自的渲染器隐藏
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.hide(player);
            }
        }

        // 隐藏可点击实体
        hideClickableEntities(player);
    }

    /**
     * 获取当前正在查看此页的玩家UUID集合
     */
    public Set<UUID> getViewers() {
        Set<UUID> pageViewers = new HashSet<>();
        if (parent != null) {
            for (Map.Entry<UUID, Integer> entry : parent.getViewerPages().entrySet()) {
                if (entry.getValue() == index) {
                    pageViewers.add(entry.getKey());
                }
            }
        }
        return pageViewers;
    }

    /**
     * 获取或创建 PageTextRendererImpl
     */
    public PageTextRendererImpl getPageTextRenderer() {
        if (pageTextRenderer == null) {
            synchronized (textRendererLock) {
                if (pageTextRenderer == null) {
                    pageTextRenderer = new PageTextRendererImpl(this, WooHolograms.getInstance().getRendererFactory().getEntityIdGenerator());
                    // 注册实体ID到 HologramManager
                    if (parent != null) {
                        HologramManager manager = WooHolograms.getInstance().getHologramManager();
                        for (int id : pageTextRenderer.getEntityIds()) {
                            manager.registerEntityId(id, parent);
                        }
                    }
                }
            }
        }
        return pageTextRenderer;
    }

    /**
     * 重建 PageTextRendererImpl（在行结构变化时调用）
     */
    public void rebuildPageTextRenderer() {
        HologramManager manager = WooHolograms.getInstance().getHologramManager();
        if (pageTextRenderer != null) {
            // 先销毁旧渲染器的实体
            for (UUID uuid : getViewers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) pageTextRenderer.destroy(p);
            }
            // 注销旧实体ID
            for (int id : pageTextRenderer.getEntityIds()) {
                manager.unregisterEntityId(id);
            }
            pageTextRenderer.reset();
            pageTextRenderer.rebuildGroups();
        } else {
            pageTextRenderer = new PageTextRendererImpl(this, WooHolograms.getInstance().getRendererFactory().getEntityIdGenerator());
        }
        // 注册新实体ID
        if (parent != null) {
            for (int id : pageTextRenderer.getEntityIds()) {
                manager.registerEntityId(id, parent);
            }
        }
    }

    /**
     * 显示此页给指定玩家（TEXT行通过PageTextRendererImpl，非TEXT行通过各自渲染器）
     */
    public void showTo(Player player) {
        // TEXT 行通过 PageTextRendererImpl 渲染
        getPageTextRenderer().render(player, parent.getLocation());

        // 非 TEXT 行通过各自的渲染器渲染
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.show(player);
            }
        }
    }

    /**
     * 更新此页的 Display Entity 属性给指定玩家（即时刷新，无闪烁）
     * TEXT 行通过 metadata update 更新，非 TEXT 行通过 hide+show 更新
     */
    public void updateDisplayProperties(Player player) {
        // TEXT 行通过 PageTextRendererImpl 的 metadata update 更新
        PageTextRendererImpl renderer = getPageTextRenderer();
        if (renderer != null) {
            renderer.updateMetadata(player);
        }

        // 非 TEXT 行通过 hide+show 更新（实体数量少，性能影响小）
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.hide(player);
                line.show(player);
            }
        }
    }

    /**
     * 从指定玩家隐藏此页（TEXT行通过PageTextRendererImpl，非TEXT行通过各自渲染器）
     */
    public void hideFromPlayer(Player player) {
        // TEXT 行通过 PageTextRendererImpl 销毁
        if (pageTextRenderer != null) {
            pageTextRenderer.destroy(player);
        }

        // 非 TEXT 行通过各自的渲染器隐藏
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.hide(player);
            }
        }
    }

    /**
     * 从所有玩家隐藏
     */
    public void hideFromAll() {
        // TEXT 行通过 PageTextRendererImpl 隐藏
        if (pageTextRenderer != null) {
            for (UUID uuid : getViewers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) pageTextRenderer.destroy(p);
            }
        }

        // 非 TEXT 行通过各自的渲染器隐藏
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.hide();
            }
        }

        if (parent != null) {
            parent.hideClickableEntitiesAll();
        }
    }

    /**
     * 更新位置
     * 
     * @param baseLocation 基础位置
     */
    public void updatePositions(Location baseLocation) {
        realignLines();
    }

    /**
     * 更新文本
     * 
     * @param viewerUuids 查看者 UUID 集合
     */
    public void updateText(Set<UUID> viewerUuids) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : viewerUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        // TEXT 行通过 PageTextRendererImpl 更新
        if (pageTextRenderer != null) {
            for (Player player : players) {
                pageTextRenderer.updateText(player);
            }
        }

        // 非 TEXT 行通过各自的渲染器更新
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.update(players.toArray(new Player[0]));
            }
        }
    }

    /**
     * 更新动画
     * 
     * @param viewerUuids 查看者 UUID 集合
     */
    public void updateAnimations(Set<UUID> viewerUuids) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : viewerUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        // TEXT 行动画通过 PageTextRendererImpl 更新
        if (pageTextRenderer != null) {
            for (Player player : players) {
                pageTextRenderer.updateText(player);
            }
        }

        // 非 TEXT 行动画通过各自的渲染器更新
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                line.updateAnimations(players.toArray(new Player[0]));
            }
        }
    }

    /*
     * 动作方法
     */

    /**
     * 检查是否可点击
     * 
     * @return 是否可点击
     */
    public boolean isClickable() {
        if (parent != null && parent.hasFlag(EnumFlag.DISABLE_ACTIONS)) {
            return false;
        }
        return hasActions();
    }

    /**
     * 检查是否有动作（页面级或任意行级）
     *
     * @return 是否有动作
     */
    public boolean hasActions() {
        for (List<Action> actionList : actions.values()) {
            if (actionList != null && !actionList.isEmpty()) {
                return true;
            }
        }
        for (HologramLine line : lines) {
            if (line.hasActions()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 行级动作变化时调用，检测 isClickable 状态变化并重建分组
     * 仅在状态变化时重建 TextGroup（创建/移除 Interaction 渲染器）
     *
     * @param wasClickable 修改前的 isClickable 状态
     */
    public void onLineActionsChanged(boolean wasClickable) {
        boolean nowClickable = isClickable();
        if (wasClickable != nowClickable) {
            rebuildGroupsAndRerender();
        }
        if (parent != null) {
            parent.onActionsChanged();
        }
    }

    /**
     * 行偏移变化时调用（由 HologramLine.setOffset 触发）
     * 重新对齐所有行位置（偏移行不占 Y 空间），然后重建 TextGroup 并重新渲染
     * X/Z 偏移变化会触发 TextGroup 分裂/合并，需要 rebuildGroups
     */
    public void onLineOffsetChanged() {
        realignLines();
        rebuildGroupsAndRerender();
    }

    /**
     * 添加动作
     *
     * @param clickType 点击类型
     * @param action 动作
     */
    public void addAction(ClickType clickType, Action action) {
        boolean wasClickable = isClickable();
        actions.computeIfAbsent(clickType, k -> new ArrayList<>()).add(action);
        if (wasClickable != isClickable()) {
            rebuildGroupsAndRerender();
        }
        if (parent != null) {
            parent.onActionsChanged();
        }
    }

    /**
     * 获取指定点击类型的动作列表
     *
     * @param clickType 点击类型
     * @return 动作列表
     */
    public List<Action> getActions(ClickType clickType) {
        List<Action> list = actions.get(clickType);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * 获取所有动作
     *
     * @return 动作映射
     */
    public Map<ClickType, List<Action>> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    /**
     * 清除指定点击类型的动作
     *
     * @param clickType 点击类型
     */
    public void clearActions(ClickType clickType) {
        boolean wasClickable = isClickable();
        actions.remove(clickType);
        if (wasClickable != isClickable()) {
            rebuildGroupsAndRerender();
        }
        if (parent != null) {
            parent.onActionsChanged();
        }
    }

    /**
     * 移除指定动作
     *
     * @param clickType 点击类型
     * @param index 动作索引
     */
    public void removeAction(ClickType clickType, int index) {
        List<Action> actionList = actions.get(clickType);
        if (actionList != null && index >= 0 && index < actionList.size()) {
            boolean wasClickable = isClickable();
            actionList.remove(index);
            if (wasClickable != isClickable()) {
                rebuildGroupsAndRerender();
            }
            if (parent != null) {
                parent.onActionsChanged();
            }
        }
    }

    /**
     * 重建 PageTextRendererImpl 分组并重新渲染所有观看者的 TextDisplay
     * 在 isClickable() 状态变化时调用，确保 Interaction 渲染器被正确创建或移除
     */
    private void rebuildGroupsAndRerender() {
        rebuildPageTextRenderer();
        if (pageTextRenderer != null && parent != null) {
            for (Player player : parent.getViewerPlayers(this.index)) {
                if (player != null && player.isOnline()) {
                    pageTextRenderer.render(player, parent.getLocation());
                    // rebuildPageTextRenderer 销毁了旧 Interaction，需重新显示
                    showClickableEntities(player);
                }
            }
        }
    }

    /**
     * 加载完成时调用，重建 TextGroup 分组
     *
     * 加载时 addLine 触发 rebuildPageTextRenderer（此时偏移=0，行进主组），
     * 之后 setOffsetX/Y/Z 是裸 setter 不触发 rebuildGroups。
     * 所以加载完所有行后需要统一重建分组，确保偏移行正确分裂。
     */
    public void onLoadComplete() {
        realignLines();
        rebuildGroupsAndRerender();
    }

    /**
     * 替换指定位置的动作
     *
     * @param clickType 点击类型
     * @param index 动作索引
     * @param action 新动作
     */
    public void setAction(ClickType clickType, int index, Action action) {
        List<Action> actionList = actions.get(clickType);
        if (actionList != null && index >= 0 && index < actionList.size()) {
            actionList.set(index, action);
        }
    }

    /**
     * 执行动作
     *
     * @param player 玩家
     * @param clickType 点击类型
     */
    public void executeActions(Player player, ClickType clickType) {
        List<Action> actionsToExecute = new ArrayList<>();

        if (actions.containsKey(clickType)) {
            List<Action> actionList = actions.get(clickType);
            if (actionList != null) {
                actionsToExecute.addAll(actionList);
            }
        }

        if (clickType != ClickType.ANY && actions.containsKey(ClickType.ANY)) {
            List<Action> anyActions = actions.get(ClickType.ANY);
            if (anyActions != null) {
                actionsToExecute.addAll(anyActions);
            }
        }

        // 动作执行：页面级
        final List<Action> finalActions = new ArrayList<>(actionsToExecute);
        WooHolograms.getInstance().debug(() -> formatActionsLog(
                "click", "page-execute", player, clickType, finalActions));

        for (Action action : actionsToExecute) {
            if (!action.execute(player)) {
                return;
            }
        }
    }

    /**
     * 格式化动作列表 debug 日志（统一格式：[Debug.module] action, key=val...）
     */
    private static String formatActionsLog(String module, String action, Player player, ClickType clickType, List<Action> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[Debug.%s] %s, player=%s, clickType=%s, actionCount=%d",
                module, action, player.getName(), clickType, actions.size()));
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            sb.append(String.format("%n  [%d] type=%s, data=%s",
                    i, a.getType().getName(), a.getData()));
        }
        return sb.toString();
    }

    /*
     * 可点击实体方法
     */

    /**
     * 检查是否包含指定实体 ID
     * 
     * @param entityId 实体 ID
     * @return 是否包含
     */
    public boolean hasEntity(int entityId) {
        // 检查 PageTextRendererImpl 的实体
        if (pageTextRenderer != null) {
            for (int id : pageTextRenderer.getEntityIds()) {
                if (id == entityId) {
                    return true;
                }
            }
        }

        // 检查非TEXT行的实体
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                for (int id : line.getEntityIds()) {
                    if (id == entityId) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    /**
     * 根据实体 ID 获取行
     *
     * @param entityId 实体 ID
     * @return 行，如果不存在返回 null
     */
    public HologramLine getLineByEntityId(int entityId) {
        return getLineByEntityId(entityId, null);
    }

    /**
     * 用玩家视线计算点击 Interaction 实体的 hitY
     * 用于左键（ATTACK）时 Minecraft 不发送精确坐标的情况
     *
     * @param player   玩家
     * @param entityId Interaction 实体 ID
     * @return 相对于实体底部的 Y 偏移，null 表示无法计算
     */
    public Float calculateHitYFromRay(Player player, int entityId) {
        if (pageTextRenderer != null) {
            return pageTextRenderer.calculateHitYFromRay(player, entityId);
        }
        return null;
    }

    /**
     * 根据实体 ID 和点击 Y 坐标获取行
     * 对于合并的 TextDisplay 实体，使用 Y 坐标路由到具体行
     *
     * @param entityId 实体 ID
     * @param hitY     点击位置相对于实体原点的 Y 坐标，null 表示无法确定
     * @return 行，如果不存在返回 null
     */
    public HologramLine getLineByEntityId(int entityId, Float hitY) {
        // 先检查 PageTextRendererImpl 的实体（支持 Y 坐标路由）
        if (pageTextRenderer != null) {
            HologramLine line = pageTextRenderer.getLineByEntityId(entityId, hitY);
            if (line != null) {
                return line;
            }
        }

        // 检查非TEXT行的实体（非TEXT行是独立实体，无需 Y 坐标路由）
        for (HologramLine line : lines) {
            if (line.getType() != HologramType.TEXT) {
                for (int id : line.getEntityIds()) {
                    if (id == entityId) {
                        return line;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 显示可点击实体（委托给 pageTextRenderer）
     *
     * @param player 玩家
     */
    public void showClickableEntities(Player player) {
        if (pageTextRenderer == null || !isClickable()) return;
        pageTextRenderer.showClickableEntities(player);
    }

    /**
     * 隐藏可点击实体（委托给 pageTextRenderer）
     *
     * @param player 玩家
     */
    public void hideClickableEntities(Player player) {
        if (pageTextRenderer == null) return;
        pageTextRenderer.hideClickableEntities(player);
    }

    /**
     * 传送可点击实体（委托给 pageTextRenderer）
     *
     * @param player 玩家
     */
    public void teleportClickableEntities(Player player) {
        if (pageTextRenderer == null || !isClickable()) return;
        pageTextRenderer.teleportClickableEntities(player);
    }

    /*
     * 标志方法
     */

    /**
     * 检查是否有指定标志
     * 
     * @param flag 标志
     * @return 是否有该标志
     */
    public boolean hasFlag(EnumFlag flag) {
        return flags.contains(flag);
    }

    /**
     * 添加标志
     * 
     * @param flags 标志数组
     */
    public void addFlags(EnumFlag... flags) {
        if (flags != null) {
            for (EnumFlag flag : flags) {
                if (flag != null) {
                    this.flags.add(flag);
                }
            }
        }
    }

    /**
     * 移除标志
     * 
     * @param flag 标志
     */
    public void removeFlag(EnumFlag flag) {
        this.flags.remove(flag);
    }

    /**
     * 清除所有标志
     */
    public void clearFlags() {
        this.flags.clear();
    }

    /**
     * 获取所有标志
     * 
     * @return 标志集合
     */
    public Set<EnumFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /*
     * 序列化方法
     */

    /**
     * 序列化为 Map
     * 
     * @return 序列化后的 Map
     */
    public Map<String, Object> serializeToMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        // 序列化行
        List<Map<String, Object>> linesMap = new ArrayList<>();
        for (HologramLine line : lines) {
            linesMap.add(line.serializeToMap());
        }
        map.put("lines", linesMap);

        // 序列化动作
        if (hasActions()) {
            Map<String, List<String>> actionsMap = new LinkedHashMap<>();
            for (Map.Entry<ClickType, List<Action>> entry : actions.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    actionsMap.put(entry.getKey().name(),
                            entry.getValue().stream()
                                    .map(Action::toString)
                                    .collect(Collectors.toList()));
                }
            }
            if (!actionsMap.isEmpty()) {
                map.put("actions", actionsMap);
            }
        }

        if (!flags.isEmpty()) {
            map.put("flags", flags.stream().map(EnumFlag::name).collect(Collectors.toList()));
        }

        return map;
    }

    /**
     * 克隆此页
     * 
     * @param parent 新父全息图
     * @param index 新索引
     * @return 克隆的页
     */
    public HologramPage clone(Hologram parent, int index) {
        HologramPage page = new HologramPage(parent, index);

        // 克隆行
        for (HologramLine line : lines) {
            page.addLine(line.clone(page, page.getNextLineLocation()));
        }

        // 克隆动作
        for (Map.Entry<ClickType, List<Action>> entry : actions.entrySet()) {
            for (Action action : entry.getValue()) {
                page.addAction(entry.getKey(), action);
            }
        }

        // 克隆标志
        page.addFlags(this.flags.toArray(new EnumFlag[this.flags.size()]));

        return page;
    }

    /**
     * 销毁此页
     */
    public void destroy() {
        // 销毁 PageTextRendererImpl
        if (pageTextRenderer != null) {
            for (UUID uuid : getViewers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) pageTextRenderer.destroy(p);
            }
            // 注销实体ID
            HologramManager manager = WooHolograms.getInstance().getHologramManager();
            for (int id : pageTextRenderer.getEntityIds()) {
                manager.unregisterEntityId(id);
            }
            pageTextRenderer.destroyAll();
            pageTextRenderer = null;
        }

        for (HologramLine line : lines) {
            line.destroy();
        }
        lines.clear();
        actions.clear();
        flags.clear();
    }

    @Override
    public String toString() {
        return "HologramPage{" +
                "index=" + index +
                ", lines=" + lines.size() +
                ", actions=" + actions.size() +
                '}';
    }
}
