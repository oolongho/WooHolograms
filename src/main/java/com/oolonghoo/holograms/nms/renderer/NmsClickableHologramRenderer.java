package com.oolonghoo.holograms.nms.renderer;

import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 可点击全息图渲染器
 * 用于全息图中的点击检测
 *
 * @author oolongho
 * @since 1.0.0
 */
public interface NmsClickableHologramRenderer extends NmsHologramRenderer {

    /**
     * 生成可点击实体
     *
     * @param player   为其生成实体的玩家
     * @param position 生成实体的位置
     */
    void display(Player player, DecentPosition position);

    /**
     * 移动可点击实体
     *
     * @param player   为其移动实体的玩家
     * @param position 移动到的位置
     */
    void move(Player player, DecentPosition position);

    /**
     * 隐藏可点击实体
     *
     * @param player 为其隐藏实体的玩家
     */
    void hide(Player player);

    /**
     * 获取可点击实体的 ID
     *
     * @return 可点击实体的 ID
     */
    int getEntityId();
    
    /**
     * 获取实体 ID 列表（默认实现）
     * 
     * @return 实体 ID 列表
     */
    @Override
    default List<Integer> getEntityIds() {
        return Collections.singletonList(getEntityId());
    }
    
    /**
     * 渲染全息图行给指定玩家（默认实现）
     */
    @Override
    default void render(Player player, Location location, com.oolonghoo.holograms.hologram.HologramLine line) {
        // 可点击渲染器不需要渲染内容
    }
    
    /**
     * 渲染全息图行给多个玩家（默认实现）
     */
    @Override
    default void render(java.util.Collection<Player> players, Location location, com.oolonghoo.holograms.hologram.HologramLine line) {
        // 可点击渲染器不需要渲染内容
    }
    
    /**
     * 更新全息图行的文本（默认实现）
     */
    @Override
    default void updateText(Player player, com.oolonghoo.holograms.hologram.HologramLine line) {
        // 可点击渲染器不需要更新文本
    }
    
    /**
     * 更新全息图行的文本给多个玩家（默认实现）
     */
    @Override
    default void updateText(java.util.Collection<Player> players, com.oolonghoo.holograms.hologram.HologramLine line) {
        // 可点击渲染器不需要更新文本
    }
    
    /**
     * 销毁全息图行（对指定玩家）
     */
    @Override
    default void destroy(Player player) {
        hide(player);
    }
    
    /**
     * 销毁全息图行（对多个玩家）
     */
    @Override
    default void destroy(java.util.Collection<Player> players) {
        for (Player player : players) {
            hide(player);
        }
    }
    
    /**
     * 传送全息图行到新位置（默认实现）
     */
    @Override
    default void teleport(Player player, Location location) {
        move(player, DecentPosition.fromLocation(location));
    }
    
    /**
     * 传送全息图行到新位置（对多个玩家）
     */
    @Override
    default void teleport(java.util.Collection<Player> players, Location location) {
        DecentPosition position = DecentPosition.fromLocation(location);
        for (Player player : players) {
            move(player, position);
        }
    }
    
    /**
     * 获取 NMS 适配器（默认实现）
     */
    @Override
    default com.oolonghoo.holograms.nms.NmsAdapter getAdapter() {
        return null;
    }
    
    /**
     * 检查渲染器是否已销毁（默认实现）
     */
    @Override
    default boolean isDestroyed() {
        return false;
    }
}
