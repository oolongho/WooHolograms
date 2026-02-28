package com.oolonghoo.holograms.listener;

import com.oolonghoo.holograms.WooHolograms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 处理玩家加入、退出等事件
 * 
 * @author oolongho
 */
public class PlayerListener implements Listener {

    private final WooHolograms plugin;

    public PlayerListener(WooHolograms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 延迟一 tick 确保玩家完全加载
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getHologramManager().onPlayerJoin(event.getPlayer());
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getHologramManager().onPlayerQuit(event.getPlayer());
    }
}
