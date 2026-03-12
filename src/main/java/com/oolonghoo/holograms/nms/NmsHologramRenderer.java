package com.oolonghoo.holograms.nms;

import com.oolonghoo.holograms.hologram.HologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * NMS 全息图渲染器接口
 * 负责将全息图渲染给玩家
 * 
 */
public interface NmsHologramRenderer {

    /**
     * 获取此渲染器的实体 ID 列表
     * @return 实体 ID 列表
     */
    List<Integer> getEntityIds();

    /**
     * 渲染全息图行给指定玩家
     * @param player 目标玩家
     * @param location 渲染位置
     * @param line 全息图行数据
     */
    void render(Player player, Location location, HologramLine line);

    /**
     * 渲染全息图行给多个玩家
     * @param players 目标玩家集合
     * @param location 渲染位置
     * @param line 全息图行数据
     */
    void render(Collection<Player> players, Location location, HologramLine line);

    /**
     * 更新全息图行的文本
     * @param player 目标玩家
     * @param line 全息图行数据
     */
    void updateText(Player player, HologramLine line);

    /**
     * 更新全息图行的文本给多个玩家
     * @param players 目标玩家集合
     * @param line 全息图行数据
     */
    void updateText(Collection<Player> players, HologramLine line);

    /**
     * 销毁全息图行（对指定玩家）
     * @param player 目标玩家
     */
    void destroy(Player player);

    /**
     * 销毁全息图行（对多个玩家）
     * @param players 目标玩家集合
     */
    void destroy(Collection<Player> players);

    /**
     * 传送全息图行到新位置
     * @param player 目标玩家
     * @param location 新位置
     */
    void teleport(Player player, Location location);

    /**
     * 传送全息图行到新位置（对多个玩家）
     * @param players 目标玩家集合
     * @param location 新位置
     */
    void teleport(Collection<Player> players, Location location);

    /**
     * 获取 NMS 适配器
     * @return NMS 适配器
     */
    NmsAdapter getAdapter();

    /**
     * 检查渲染器是否已销毁
     * @return 是否已销毁
     */
    boolean isDestroyed();
}
