package com.oolongho.holograms.api.event;

import com.oolongho.holograms.api.action.ClickType;
import com.oolongho.holograms.api.hologram.Hologram;
import com.oolongho.holograms.api.hologram.HologramLine;
import com.oolongho.holograms.api.hologram.HologramPage;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 全息图动作执行事件
 * 在点击动作即将执行前触发（晚于 {@link HologramClickEvent}，携带已路由的行信息）。
 * 取消事件将跳过本次全部动作执行
 */
public class HologramActionExecuteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Hologram hologram;
    private final HologramPage page;
    private final HologramLine line;
    private final ClickType clickType;
    private boolean cancelled;

    public HologramActionExecuteEvent(Player player, Hologram hologram, HologramPage page,
                                      @Nullable HologramLine line, ClickType clickType) {
        this.player = player;
        this.hologram = hologram;
        this.page = page;
        this.line = line;
        this.clickType = clickType;
        this.cancelled = false;
    }

    /**
     * 获取触发动作的玩家
     *
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取动作所属全息图
     *
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * 获取动作所属页面
     *
     * @return 页面
     */
    public HologramPage getPage() {
        return page;
    }

    /**
     * 获取命中的行（行级动作路由结果）
     *
     * @return 行；点击未路由到具体行时为 null（将执行页面级动作）
     */
    @Nullable
    public HologramLine getLine() {
        return line;
    }

    /**
     * 获取点击类型
     *
     * @return 点击类型
     */
    public ClickType getClickType() {
        return clickType;
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
