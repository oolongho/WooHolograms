package com.oolonghoo.holograms.listener;

import com.oolonghoo.holograms.WooHolograms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * 世界事件监听器
 * 处理世界加载、卸载等事件
 * 
 * @author oolongho
 */
public class WorldListener implements Listener {

    private final WooHolograms plugin;

    public WorldListener(WooHolograms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getHologramManager().onWorldLoad(event.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.getHologramManager().onWorldUnload(event.getWorld().getName());
    }
}
