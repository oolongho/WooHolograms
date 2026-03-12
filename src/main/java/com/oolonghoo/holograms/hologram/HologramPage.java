package com.oolonghoo.holograms.hologram;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
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
 * @author oolongho
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

    // 可点击实体渲染器
    private final List<NmsHologramRenderer> clickableRenderers;

    // 标志
    private final Set<EnumFlag> flags;

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
        this.clickableRenderers = new ArrayList<>();
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
            height += line.getHeight();
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
        if (parent.isDownOrigin()) {
            center.add(0, getHeight() / 2, 0);
        } else {
            center.subtract(0, getHeight() / 2, 0);
        }
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
     * 获取行数（别名方法）
     * 
     * @return 行数
     */
    public int getLineCount() {
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

        // 显示给当前观看者
        if (parent != null) {
            Set<Player> viewers = parent.getViewerPlayers(this.index);
            for (Player player : viewers) {
                if (player != null && player.isOnline()) {
                    line.show(player);
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
            line.hide();
            line.show();
            realignLines();
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
        realignLines();
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
     */
    public void realignLines() {
        if (parent == null) {
            return;
        }

        Location currentLocation = parent.getLocation().clone();

        // 如果从下往上，先移动到顶部
        if (parent.isDownOrigin()) {
            currentLocation.add(0, getHeight(), 0);
        }

        for (HologramLine line : lines) {
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

    /*
     * 显示/隐藏方法
     */

    /**
     * 显示给玩家
     * 
     * @param player 玩家
     * @param baseLocation 基础位置
     * @param factory 渲染器工厂
     */
    public void showTo(Player player, Location baseLocation, NmsHologramRendererFactory factory) {
        if (player == null || !player.isOnline()) {
            return;
        }

        for (HologramLine line : lines) {
            if (line.isEnabled()) {
                line.show(player);
            }
        }
    }

    /**
     * 显示给多个玩家
     * 
     * @param viewerUuids 查看者 UUID 集合
     * @param baseLocation 基础位置
     * @param factory 渲染器工厂
     */
    public void showTo(Set<UUID> viewerUuids, Location baseLocation, NmsHologramRendererFactory factory) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : viewerUuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        for (HologramLine line : lines) {
            if (line.isEnabled()) {
                for (Player player : players) {
                    line.show(player);
                }
            }
        }
    }

    /**
     * 从玩家隐藏
     * 
     * @param player 玩家
     */
    public void hideFrom(Player player) {
        if (player == null) {
            return;
        }

        for (HologramLine line : lines) {
            line.hide(player);
        }

        // 隐藏可点击实体
        hideClickableEntities(player);
    }

    /**
     * 从所有玩家隐藏
     */
    public void hideFromAll() {
        for (HologramLine line : lines) {
            line.hide();
        }

        for (NmsHologramRenderer renderer : clickableRenderers) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                renderer.destroy(player);
            }
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

        for (HologramLine line : lines) {
            line.update(players.toArray(new Player[players.size()]));
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

        for (HologramLine line : lines) {
            line.updateAnimations(players.toArray(new Player[players.size()]));
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
     * 检查是否有动作
     * 
     * @return 是否有动作
     */
    public boolean hasActions() {
        for (List<Action> actionList : actions.values()) {
            if (actionList != null && !actionList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加动作
     * 
     * @param clickType 点击类型
     * @param action 动作
     */
    public void addAction(ClickType clickType, Action action) {
        actions.computeIfAbsent(clickType, k -> new ArrayList<>()).add(action);
    }

    /**
     * 获取指定点击类型的动作列表
     * 
     * @param clickType 点击类型
     * @return 动作列表
     */
    public List<Action> getActions(ClickType clickType) {
        return actions.getOrDefault(clickType, new ArrayList<>());
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
        actions.remove(clickType);
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
            actionList.remove(index);
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
        
        for (Action action : actionsToExecute) {
            if (!action.execute(player)) {
                return;
            }
        }
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
        // 检查可点击实体
        for (NmsHologramRenderer renderer : clickableRenderers) {
            if (renderer.getEntityIds().contains(entityId)) {
                return true;
            }
        }

        // 检查行实体
        for (HologramLine line : lines) {
            for (int id : line.getEntityIds()) {
                if (id == entityId) {
                    return true;
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
        for (HologramLine line : lines) {
            for (int id : line.getEntityIds()) {
                if (id == entityId) {
                    return line;
                }
            }
        }
        return null;
    }

    /**
     * 获取可点击渲染器
     * 
     * @param index 索引
     * @return 渲染器
     */
    public NmsHologramRenderer getClickableRenderer(int index) {
        if (index >= clickableRenderers.size()) {
            NmsHologramRenderer renderer = WooHolograms.getInstance()
                    .getRendererFactory()
                    .createClickableRenderer();
            clickableRenderers.add(renderer);
        }
        return clickableRenderers.get(index);
    }

    /**
     * 获取所有可点击渲染器
     * 
     * @return 渲染器列表
     */
    public List<NmsHologramRenderer> getClickableEntityRenderers() {
        return Collections.unmodifiableList(clickableRenderers);
    }

    /**
     * 显示可点击实体
     * 
     * @param player 玩家
     */
    public void showClickableEntities(Player player) {
        if (!isClickable()) {
            return;
        }

        int amount = (int) (getHeight() / 2) + 1;
        Location location = parent.getLocation().clone();
        location.setY((int) (location.getY() - (parent.isDownOrigin() ? 0 : getHeight())) + 0.5);

        for (int i = 0; i < amount; i++) {
            getClickableRenderer(i);
            // 渲染可点击实体
            // renderer.display(player, location);
            location.add(0, 1.8, 0);
        }
    }

    /**
     * 隐藏可点击实体
     * 
     * @param player 玩家
     */
    public void hideClickableEntities(Player player) {
        for (NmsHologramRenderer renderer : clickableRenderers) {
            renderer.destroy(player);
        }
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
        Map<String, List<String>> actionsMap = new LinkedHashMap<>();
        for (Map.Entry<ClickType, List<Action>> entry : actions.entrySet()) {
            actionsMap.put(entry.getKey().name(), 
                    entry.getValue().stream()
                            .map(Action::toString)
                            .collect(Collectors.toList()));
        }
        map.put("actions", actionsMap);

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
        for (HologramLine line : lines) {
            line.destroy();
        }
        lines.clear();

        for (NmsHologramRenderer renderer : clickableRenderers) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                renderer.destroy(player);
            }
        }
        clickableRenderers.clear();

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
