package com.oolonghoo.holograms.api.event;

import com.oolonghoo.holograms.hologram.Hologram;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图删除事件
 * 当全息图被删除时触发
 * 
 */
public class HologramDeleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Hologram hologram;

    public HologramDeleteEvent(Hologram hologram) {
        this.hologram = hologram;
    }

    /**
     * 获取被删除的全息图
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
