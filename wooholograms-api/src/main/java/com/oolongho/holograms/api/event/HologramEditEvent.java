package com.oolongho.holograms.api.event;

import com.oolongho.holograms.api.hologram.Hologram;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 全息图编辑事件
 * 当批量编辑器（{@code HologramEditor#apply()}）提交属性修改后触发。
 * 本事件不可取消（修改已生效），仅用于同步/响应/日志等场景
 */
public class HologramEditEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Hologram hologram;

    public HologramEditEvent(Hologram hologram) {
        this.hologram = hologram;
    }

    /**
     * 获取被编辑的全息图
     *
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
