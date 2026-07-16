package com.oolongho.holograms.util;

import com.oolongho.holograms.WooHolograms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * 统一调度工具类，兼容 Paper 和 Folia
 * <p>
 * Folia 使用区域化线程调度器，与传统 Bukkit 的全局主线程模型不同。
 * 本类封装了两种调度 API 的差异，使调用方无需关心运行环境。
 */
public final class SchedulerUtil {

    private static WooHolograms plugin;
    private static volatile boolean folia;

    private SchedulerUtil() {}

    /**
     * 初始化调度工具，必须在插件启动时调用
     *
     * @param plugin 插件实例
     */
    public static void initialize(WooHolograms plugin) {
        SchedulerUtil.plugin = plugin;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
    }

    /**
     * 检测当前服务端是否为 Folia
     *
     * @return 是否为 Folia
     */
    public static boolean isFolia() {
        return folia;
    }

    /**
     * 在主线程（Paper）或玩家所属区域线程（Folia）执行任务
     *
     * @param player 玩家上下文
     * @param task   要执行的任务
     */
    public static void runTask(Player player, Runnable task) {
        if (isFolia()) {
            player.getScheduler().run(plugin, t -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 在主线程（Paper）或全局区域线程（Folia）执行任务
     * 适用于无实体上下文的操作，如控制台命令
     *
     * @param task 要执行的任务
     */
    public static void runTask(Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 延迟执行任务（玩家上下文），返回可取消的任务句柄
     *
     * @param player     玩家上下文
     * @param task       要执行的任务
     * @param delayTicks 延迟 tick 数
     * @return 任务句柄，可用于取消任务
     */
    public static TaskHandle runTaskLater(Player player, Runnable task, long delayTicks) {
        if (isFolia()) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask =
                    player.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks);
            return new TaskHandle(scheduledTask, true);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
            return new TaskHandle(bukkitTask, false);
        }
    }

    /**
     * 延迟执行任务（全局区域）
     *
     * @param task       要执行的任务
     * @param delayTicks 延迟 tick 数
     */
    public static TaskHandle runTaskLater(Runnable task, long delayTicks) {
        if (isFolia()) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask =
                    Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks);
            return new TaskHandle(scheduledTask, true);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
            return new TaskHandle(bukkitTask, false);
        }
    }

    /**
     * 在指定位置的区域线程执行任务（Folia），或在主线程执行（Paper）
     *
     * @param location 位置上下文
     * @param task     要执行的任务
     */
    public static void runTaskAt(Location location, Runnable task) {
        if (isFolia()) {
            Bukkit.getRegionScheduler().run(plugin, location, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 异步执行任务
     *
     * @param task 要执行的任务
     */
    public static void runTaskAsynchronously(Runnable task) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * 以固定间隔重复执行任务（全局区域），返回可取消的任务句柄
     *
     * @param task             要执行的任务
     * @param initialDelayTicks 初始延迟 tick 数
     * @param periodTicks       执行间隔 tick 数
     * @return 任务句柄，可用于取消任务
     */
    public static TaskHandle runAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks) {
        if (isFolia()) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask =
                    Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), initialDelayTicks, periodTicks);
            return new TaskHandle(scheduledTask, true);
        } else {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks);
            return new TaskHandle(bukkitTask, false);
        }
    }

    /**
     * 异步传送玩家
     *
     * @param player   玩家
     * @param location 目标位置
     */
    public static void teleportAsync(Player player, Location location) {
        player.teleportAsync(location);
    }

    /**
     * 统一任务句柄，封装 Folia ScheduledTask 和 BukkitTask 的取消逻辑
     */
    public static class TaskHandle {

        private final Object task;
        private final boolean folia;

        private TaskHandle(Object task, boolean folia) {
            this.task = task;
            this.folia = folia;
        }

        /**
         * 取消任务
         */
        public void cancel() {
            if (folia) {
                ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
            } else {
                Bukkit.getScheduler().cancelTask(((BukkitTask) task).getTaskId());
            }
        }
    }
}
