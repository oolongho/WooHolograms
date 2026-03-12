package com.oolonghoo.holograms.nms;

import org.bukkit.entity.Player;

/**
 * NMS 适配器接口，作为 NMS 实现的主要访问点。
 *
 * <p>此类提供对全息图渲染器工厂和包监听器的访问。</p>
 *
 * 
 * 
 */
public interface NmsAdapter {

    /**
     * 获取用于创建全息图渲染器的工厂。
     *
     * @return {@link NmsHologramRendererFactory} 的实例
     */
    NmsHologramRendererFactory getHologramRendererFactory();

    /**
     * 为玩家注册包监听器。
     *
     * <p>一个玩家只能注册一个监听器。</p>
     *
     * @param player   要为其注册监听器的玩家
     * @param listener 要注册的监听器
     */
    void registerPacketListener(Player player, NmsPacketListener listener);

    /**
     * 为玩家注销包监听器。
     *
     * <p>如果给定的玩家没有注册监听器，则不会发生任何事情。</p>
     *
     * @param player 要为其注销监听器的玩家
     */
    void unregisterPacketListener(Player player);

    /**
     * 获取适配器支持的版本。
     *
     * @return 版本字符串
     */
    String getVersion();
}
