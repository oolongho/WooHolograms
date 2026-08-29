package com.oolongho.holograms.listener;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.util.SchedulerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * 玩家事件监听器
 * 处理玩家加入、退出等事件
 *
 */
public class PlayerListener implements Listener {

    private final WooHolograms plugin;

    public PlayerListener(WooHolograms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 立即注入数据包监听器（PlayerJoinEvent 时 channel 已就绪）
        plugin.getPacketListener().inject(event.getPlayer());
        // 延迟一 tick 确保玩家完全加载
        SchedulerUtil.runTaskLater(event.getPlayer(), () -> {
            plugin.getHologramManager().onPlayerJoin(event.getPlayer());
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPacketListener().uninject(event.getPlayer());
        plugin.getHologramManager().onPlayerQuit(event.getPlayer());
    }

    /**
     * 玩家换世界后客户端会清空所有实体（含数据包实体），
     * 必须重置观看状态，否则可见性循环认为"已显示"而不重发 → 全息图永久消失直到 reload。
     * 同世界传送不丢失客户端实体（由可见性循环按视距处理），无需监听。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getHologramManager().onPlayerTeleport(event.getPlayer());
    }

    /**
     * 重生后客户端同样清空所有实体，重置观看状态
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getHologramManager().onPlayerTeleport(event.getPlayer());
    }
}
