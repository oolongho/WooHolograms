package com.oolonghoo.holograms.api.event;

import com.oolonghoo.holograms.hologram.Hologram;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 全息图加载完成事件
 * 当所有全息图加载完成时触发
 * 
 * @author oolongho
 */
public class HologramsLoadedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    
    private final List<Hologram> holograms;

    public HologramsLoadedEvent(List<Hologram> holograms) {
        this.holograms = holograms;
    }

    /**
     * 获取已加载的全息图列表
     * @return 全息图列表
     */
    public List<Hologram> getHolograms() {
        return holograms;
    }

    /**
     * 获取已加载的全息图数量
     * @return 数量
     */
    public int getLoadedCount() {
        return holograms.size();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
