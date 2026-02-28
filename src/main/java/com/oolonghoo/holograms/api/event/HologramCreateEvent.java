package com.oolonghoo.holograms.api.event;

import com.oolonghoo.holograms.hologram.Hologram;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图创建事件
 * 当全息图被创建时触发
 * 
 * @author oolongho
 */
public class HologramCreateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Hologram hologram;
    private boolean cancelled;

    public HologramCreateEvent(Hologram hologram) {
        this.hologram = hologram;
        this.cancelled = false;
    }

    /**
     * 获取被创建的全息图
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
