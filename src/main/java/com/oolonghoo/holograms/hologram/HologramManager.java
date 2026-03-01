package com.oolonghoo.holograms.hologram;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.api.event.HologramCreateEvent;
import com.oolonghoo.holograms.api.event.HologramDeleteEvent;
import com.oolonghoo.holograms.api.event.HologramsLoadedEvent;
import com.oolonghoo.holograms.storage.HologramStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全息图管理器
 * 负责全息图的创建、删除、存储和更新
 * 参考 DecentHolograms 的 HologramManager 实现
 * 
 * @author oolongho
 */
public class HologramManager {

    private final WooHolograms plugin;
    private final HologramStorage storage;

    // 全息图缓存
    private final Map<String, Hologram> holograms;

    // 更新任务
    private UpdateTask updateTask;

    // 全息图缓存（API 用）
    private static final Map<String, Hologram> CACHED_HOLOGRAMS = new ConcurrentHashMap<>();

    /*
     * 构造函数
     */

    /**
     * 创建全息图管理器
     * 
     * @param plugin 插件实例
     * @param storage 存储器
     */
    public HologramManager(WooHolograms plugin, HologramStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.holograms = new ConcurrentHashMap<>();
    }

    /*
     * 静态缓存方法
     */

    /**
     * 获取缓存的全息图
     * 
     * @param name 名称
     * @return 全息图
     */
    public static Hologram getCachedHologram(String name) {
        return CACHED_HOLOGRAMS.get(name);
    }

    /**
     * 获取所有缓存的全息图名称
     * 
     * @return 名称集合
     */
    public static Set<String> getCachedHologramNames() {
        return CACHED_HOLOGRAMS.keySet();
    }

    /**
     * 获取所有缓存的全息图
     * 
     * @return 全息图集合
     */
    public static Collection<Hologram> getCachedHolograms() {
        return CACHED_HOLOGRAMS.values();
    }

    /*
     * 创建方法
     */

    /**
     * 创建全息图
     * 
     * @param name 名称
     * @param location 位置
     * @return 创建的全息图，如果名称已存在则返回 null
     */
    public Hologram createHologram(String name, Location location) {
        return createHologram(name, location, true);
    }

    /**
     * 创建全息图
     * 
     * @param name 名称
     * @param location 位置
     * @param saveToFile 是否保存到文件
     * @return 创建的全息图，如果名称已存在则返回 null
     */
    public Hologram createHologram(String name, Location location, boolean saveToFile) {
        if (holograms.containsKey(name)) {
            return null;
        }

        Hologram hologram = new Hologram(name, location, saveToFile);
        hologram.setStorage(storage);

        // 触发事件
        HologramCreateEvent event = new HologramCreateEvent(hologram);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        holograms.put(name, hologram);
        CACHED_HOLOGRAMS.put(name, hologram);

        // 显示给附近玩家
        showToNearby(hologram);

        return hologram;
    }

    /*
     * 获取方法
     */

    /**
     * 获取全息图
     * 
     * @param name 名称
     * @return 全息图，如果不存在返回 null
     */
    public Hologram getHologram(String name) {
        return holograms.get(name);
    }

    /**
     * 获取所有全息图
     * 
     * @return 全息图集合
     */
    public Collection<Hologram> getHolograms() {
        return Collections.unmodifiableCollection(holograms.values());
    }
    
    /**
     * 获取所有全息图（别名方法）
     * 
     * @return 全息图集合
     */
    public Collection<Hologram> getAllHolograms() {
        return getHolograms();
    }

    /**
     * 获取全息图数量
     * 
     * @return 数量
     */
    public int getHologramCount() {
        return holograms.size();
    }

    /**
     * 检查全息图是否存在
     * 
     * @param name 名称
     * @return 是否存在
     */
    public boolean containsHologram(String name) {
        return holograms.containsKey(name);
    }

    /**
     * 获取指定世界的全息图
     * 
     * @param worldName 世界名称
     * @return 全息图列表
     */
    public List<Hologram> getHologramsInWorld(String worldName) {
        List<Hologram> result = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            Location loc = hologram.getLocation();
            if (loc != null && loc.getWorld() != null && loc.getWorld().getName().equals(worldName)) {
                result.add(hologram);
            }
        }
        return result;
    }

    /**
     * 获取所有全息图名称
     * 
     * @return 名称集合
     */
    public Set<String> getHologramNames() {
        return Collections.unmodifiableSet(holograms.keySet());
    }

    /*
     * 删除方法
     */

    /**
     * 删除全息图
     * 
     * @param name 名称
     * @return 是否成功删除
     */
    public boolean deleteHologram(String name) {
        Hologram hologram = holograms.remove(name);
        if (hologram == null) {
            return false;
        }

        // 触发事件
        HologramDeleteEvent event = new HologramDeleteEvent(hologram);
        Bukkit.getPluginManager().callEvent(event);

        // 从缓存移除
        CACHED_HOLOGRAMS.remove(name);

        // 销毁全息图
        hologram.destroy();

        return true;
    }

    /**
     * 移除全息图（不删除文件）
     * 
     * @param name 名称
     * @return 被移除的全息图
     */
    public Hologram removeHologram(String name) {
        Hologram hologram = holograms.remove(name);
        if (hologram != null) {
            CACHED_HOLOGRAMS.remove(name);
            hologram.hideAll();
        }
        return hologram;
    }

    /*
     * 加载/保存方法
     */

    /**
     * 加载所有全息图
     */
    public void loadAll() {
        Map<String, Hologram> loaded = storage.loadAll();

        for (Map.Entry<String, Hologram> entry : loaded.entrySet()) {
            String name = entry.getKey();
            Hologram hologram = entry.getValue();

            holograms.put(name, hologram);
            CACHED_HOLOGRAMS.put(name, hologram);
            
            // 显示给附近玩家
            showToNearby(hologram);
        }

        // 触发加载完成事件
        HologramsLoadedEvent event = new HologramsLoadedEvent(new ArrayList<>(loaded.values()));
        Bukkit.getPluginManager().callEvent(event);

        // 启动更新任务
        startUpdateTask();

        plugin.getLogger().info("已加载 " + loaded.size() + " 个全息图");
    }

    /**
     * 保存所有全息图
     */
    public void saveAll() {
        int count = 0;
        for (Hologram hologram : holograms.values()) {
            if (hologram.isSaveToFile()) {
                hologram.save();
                count++;
            }
        }

        plugin.getLogger().info("已保存 " + count + " 个全息图");
    }

    /**
     * 重载所有全息图
     */
    public void reload() {
        // 停止更新任务
        stopUpdateTask();

        // 隐藏并清除所有全息图
        for (Hologram hologram : holograms.values()) {
            hologram.hideAll();
        }
        holograms.clear();
        CACHED_HOLOGRAMS.clear();

        // 重新加载
        loadAll();
    }

    /**
     * 清除所有全息图
     */
    public void clear() {
        stopUpdateTask();

        for (Hologram hologram : holograms.values()) {
            hologram.destroy();
        }

        holograms.clear();
        CACHED_HOLOGRAMS.clear();
    }

    /*
     * 显示方法
     */

    /**
     * 显示全息图给附近玩家
     * 
     * @param hologram 全息图
     */
    private void showToNearby(Hologram hologram) {
        if (!hologram.isEnabled()) {
            return;
        }

        Location location = hologram.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        double displayRange = hologram.getDisplayRange();
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= displayRange * displayRange) {
                hologram.show(player, 0);
            }
        }
    }

    /*
     * 玩家事件处理
     */

    /**
     * 玩家加入时显示附近全息图
     * 
     * @param player 玩家
     */
    public void onPlayerJoin(Player player) {
        for (Hologram hologram : holograms.values()) {
            if (hologram.isEnabled()) {
                Location loc = hologram.getLocation();
                if (loc != null && loc.getWorld() != null && 
                    loc.getWorld().equals(player.getWorld())) {
                    double displayRange = hologram.getDisplayRange();
                    if (player.getLocation().distanceSquared(loc) <= displayRange * displayRange) {
                        hologram.show(player, 0);
                    }
                }
            }
        }
    }

    /**
     * 玩家离开时移除查看者
     * 
     * @param player 玩家
     */
    public void onPlayerQuit(Player player) {
        for (Hologram hologram : holograms.values()) {
            hologram.onQuit(player);
        }
    }

    /**
     * 玩家移动时更新可见性
     * 
     * @param player 玩家
     */
    public void onPlayerMove(Player player) {
        for (Hologram hologram : holograms.values()) {
            if (!hologram.isEnabled()) {
                continue;
            }

            Location loc = hologram.getLocation();
            if (loc == null || loc.getWorld() == null || 
                !loc.getWorld().equals(player.getWorld())) {
                continue;
            }

            double displayRange = hologram.getDisplayRange();
            double distanceSquared = player.getLocation().distanceSquared(loc);
            boolean isViewer = hologram.isVisible(player);

            if (distanceSquared <= displayRange * displayRange && !isViewer) {
                hologram.show(player, hologram.getPlayerPage(player));
            } else if (distanceSquared > displayRange * displayRange && isViewer) {
                hologram.hide(player);
            }
        }
    }

    /**
     * 玩家传送时更新可见性
     * 
     * @param player 玩家
     */
    public void onPlayerTeleport(Player player) {
        // 先隐藏所有
        for (Hologram hologram : holograms.values()) {
            if (hologram.isVisible(player)) {
                hologram.hide(player);
            }
        }

        // 延迟显示（等待客户端加载世界）
        Bukkit.getScheduler().runTaskLater(plugin, () -> onPlayerJoin(player), 20L);
    }

    /*
     * 世界事件处理
     */

    /**
     * 世界加载时显示全息图
     * 
     * @param worldName 世界名称
     */
    public void onWorldLoad(String worldName) {
        for (Hologram hologram : getHologramsInWorld(worldName)) {
            showToNearby(hologram);
        }
    }

    /**
     * 世界卸载时隐藏全息图
     * 
     * @param worldName 世界名称
     */
    public void onWorldUnload(String worldName) {
        for (Hologram hologram : getHologramsInWorld(worldName)) {
            hologram.hideAll();
        }
    }

    /*
     * 更新任务
     */

    /**
     * 启动更新任务
     */
    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        long interval = plugin.getConfigManager() != null 
                ? plugin.getConfigManager().getUpdateInterval() 
                : 3L;

        if (interval <= 0) {
            return;
        }

        updateTask = new UpdateTask();
        updateTask.runTaskTimerAsynchronously(plugin, interval, interval);
    }

    /**
     * 停止更新任务
     */
    private void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * 更新任务
     * 负责更新动画和占位符
     */
    private class UpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            for (Hologram hologram : holograms.values()) {
                if (hologram.isEnabled()) {
                    hologram.updateAnimationsAll();
                }
            }
        }
    }

    /*
     * 点击处理
     */

    /**
     * 处理玩家点击
     * 
     * @param player 玩家
     * @param entityId 实体 ID
     * @param clickType 点击类型
     * @return 是否处理成功
     */
    public boolean handleClick(Player player, int entityId, com.oolonghoo.holograms.action.ClickType clickType) {
        for (Hologram hologram : holograms.values()) {
            if (hologram.isVisible(player)) {
                if (hologram.onClick(player, entityId, clickType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * 工具方法
     */

    /**
     * 获取存储器
     * 
     * @return 存储器
     */
    public HologramStorage getStorage() {
        return storage;
    }

    /**
     * 获取玩家可见的全息图数量
     * 
     * @param player 玩家
     * @return 数量
     */
    public int getVisibleHologramCount(Player player) {
        int count = 0;
        for (Hologram hologram : holograms.values()) {
            if (hologram.isVisible(player)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取玩家可见的全息图
     * 
     * @param player 玩家
     * @return 全息图列表
     */
    public List<Hologram> getVisibleHolograms(Player player) {
        List<Hologram> result = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            if (hologram.isVisible(player)) {
                result.add(hologram);
            }
        }
        return result;
    }

    /**
     * 检查全息图是否存在（兼容旧方法）
     * 
     * @param id 全息图 ID
     * @return 是否存在
     */
    public boolean exists(String id) {
        return holograms.containsKey(id);
    }

    @Override
    public String toString() {
        return "HologramManager{" +
                "holograms=" + holograms.size() +
                '}';
    }
}
