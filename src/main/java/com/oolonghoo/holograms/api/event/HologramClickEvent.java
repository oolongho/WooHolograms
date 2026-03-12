package com.oolonghoo.holograms.api.event;

import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图点击事件
 * 当玩家点击全息图时触发
 * 
 */
public class HologramClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 点击的玩家
     */
    private final Player player;

    /**
     * 被点击的全息图
     */
    private final Hologram hologram;

    /**
     * 被点击的页面
     */
    private final HologramPage page;

    /**
     * 点击类型
     */
    private final ClickType clickType;

    /**
     * 被点击的实体 ID
     */
    private final int entityId;

    /**
     * 是否取消事件
     */
    private boolean cancelled;

    /**
     * 构造函数
     * 
     * @param hologram 全息图
     * @param player 玩家
     * @param clickType 点击类型
     */
    public HologramClickEvent(Hologram hologram, Player player, ClickType clickType) {
        this(hologram, null, player, clickType, -1);
    }

    /**
     * 构造函数（完整参数）
     * 
     * @param hologram 全息图
     * @param page 页面
     * @param player 玩家
     * @param clickType 点击类型
     * @param entityId 实体 ID
     */
    public HologramClickEvent(Hologram hologram, HologramPage page, Player player, ClickType clickType, int entityId) {
        this.hologram = hologram;
        this.page = page;
        this.player = player;
        this.clickType = clickType;
        this.entityId = entityId;
        this.cancelled = false;
    }

    /**
     * 获取被点击的全息图
     * 
     * @return 全息图
     */
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * 获取被点击的页面
     * 
     * @return 页面，可能为 null
     */
    public HologramPage getPage() {
        return page;
    }

    /**
     * 获取页面索引
     * 
     * @return 页面索引，如果页面为 null 返回 -1
     */
    public int getPageIndex() {
        return page != null ? page.getIndex() : -1;
    }

    /**
     * 获取点击的玩家
     * 
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取点击类型
     * 
     * @return 点击类型
     */
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * 获取被点击的实体 ID
     * 
     * @return 实体 ID，如果未知返回 -1
     */
    public int getEntityId() {
        return entityId;
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
