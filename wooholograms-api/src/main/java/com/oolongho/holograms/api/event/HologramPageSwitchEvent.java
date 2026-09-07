package com.oolongho.holograms.api.event;

import com.oolongho.holograms.api.hologram.Hologram;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图翻页事件
 * 当玩家从一个页面切换到另一个页面时触发（玩家进入范围首次显示不触发）。
 * 取消事件将保持玩家停留在当前页面
 */
public class HologramPageSwitchEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Hologram hologram;
    private final int fromIndex;
    private final int toIndex;
    private boolean cancelled;

    public HologramPageSwitchEvent(Player player, Hologram hologram, int fromIndex, int toIndex) {
        this.player = player;
        this.hologram = hologram;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cancelled = false;
    }

    /**
     * 获取翻页的玩家
     *
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取发生翻页的全息图
     *
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * 获取原页面索引
     *
     * @return 原页码（从 0 开始）
     */
    public int getFromIndex() {
        return fromIndex;
    }

    /**
     * 获取目标页面索引
     *
     * @return 目标页码（从 0 开始）
     */
    public int getToIndex() {
        return toIndex;
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
