package com.oolongho.holograms.api.event;

import com.oolongho.holograms.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图移动事件
 * 当全息图位置发生变化（setLocation/teleport/move）时触发。
 * 取消事件将中止本次移动
 */
public class HologramMoveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Hologram hologram;
    private final Location from;
    private final Location to;
    private boolean cancelled;

    public HologramMoveEvent(Hologram hologram, Location from, Location to) {
        this.hologram = hologram;
        this.from = from;
        this.to = to;
        this.cancelled = false;
    }

    /**
     * 获取被移动的全息图
     *
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * 获取原位置
     *
     * @return 原位置（可能为 null）
     */
    public Location getFrom() {
        return from != null ? from.clone() : null;
    }

    /**
     * 获取目标位置
     *
     * @return 目标位置
     */
    public Location getTo() {
        return to != null ? to.clone() : null;
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
