package com.oolonghoo.holograms.api;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramManager;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;

/**
 * WooHolograms API
 * 提供对外接口供其他插件使用
 * 
 * @author oolongho
 */
public class WooHologramsAPI {

    private static WooHolograms instance;

    /**
     * 初始化 API
     * @param plugin 插件实例
     */
    public static void initialize(WooHolograms plugin) {
        instance = plugin;
    }

    /**
     * 检查 API 是否已加载
     * @return 是否已加载
     */
    public static boolean isLoaded() {
        return instance != null && instance.isEnabled();
    }

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public static WooHolograms getInstance() {
        return instance;
    }

    /**
     * 获取全息图管理器
     * @return 全息图管理器
     */
    public static HologramManager getHologramManager() {
        if (!isLoaded()) {
            throw new IllegalStateException("WooHolograms is not loaded");
        }
        return instance.getHologramManager();
    }

    /**
     * 创建全息图
     * @param id 全息图 ID
     * @param location 位置
     * @return 创建的全息图，如果 ID 已存在则返回 Optional.empty()
     */
    public static Optional<Hologram> createHologram(String id, Location location) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        Hologram hologram = getHologramManager().createHologram(id, location);
        return Optional.ofNullable(hologram);
    }

    /**
     * 获取全息图
     * @param id 全息图 ID
     * @return 全息图
     */
    public static Optional<Hologram> getHologram(String id) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        return Optional.ofNullable(getHologramManager().getHologram(id));
    }

    /**
     * 删除全息图
     * @param id 全息图 ID
     * @return 是否成功删除
     */
    public static boolean deleteHologram(String id) {
        if (!isLoaded()) {
            return false;
        }
        return getHologramManager().deleteHologram(id);
    }

    /**
     * 检查全息图是否存在
     * @param id 全息图 ID
     * @return 是否存在
     */
    public static boolean hologramExists(String id) {
        if (!isLoaded()) {
            return false;
        }
        return getHologramManager().exists(id);
    }

    /**
     * 获取所有全息图
     * @return 全息图集合
     */
    public static Collection<Hologram> getAllHolograms() {
        if (!isLoaded()) {
            return java.util.Collections.emptyList();
        }
        return getHologramManager().getAllHolograms();
    }

    /**
     * 获取全息图数量
     * @return 全息图数量
     */
    public static int getHologramCount() {
        if (!isLoaded()) {
            return 0;
        }
        return getHologramManager().getHologramCount();
    }
}
