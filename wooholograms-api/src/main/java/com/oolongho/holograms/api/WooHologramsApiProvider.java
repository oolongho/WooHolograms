package com.oolongho.holograms.api;

import java.util.Optional;

/**
 * WooHolograms API 提供者
 *
 * <p>WooHolograms 插件启用时注册实例，第三方插件在依赖插件加载后即可获取。
 * 推荐在 Bukkit 任务的延迟一 tick 后访问（确保 {@code plugin.yml} 的 depend 已完成加载）：</p>
 * <pre>{@code
 * if (getServer().getPluginManager().getPlugin("WooHolograms") == null) return;
 * getServer().getScheduler().runTask(this, () -> {
 *     HologramApi api = WooHologramsApiProvider.getOrThrow();
 *     // ...
 * });
 * }</pre>
 */
public final class WooHologramsApiProvider {

    private static volatile HologramApi instance;

    private WooHologramsApiProvider() {
    }

    /**
     * 注册 API 实例（由 WooHolograms 插件在启用时调用；传入 null 表示注销）
     *
     * @param api API 实例
     */
    public static void register(@org.jetbrains.annotations.Nullable HologramApi api) {
        instance = api;
    }

    /**
     * 获取 API 实例
     *
     * @return API 实例；WooHolograms 未加载或已禁用时为空
     */
    public static Optional<HologramApi> get() {
        return Optional.ofNullable(instance);
    }

    /**
     * 获取 API 实例，未加载时抛出异常
     *
     * @return API 实例
     * @throws IllegalStateException WooHolograms 未加载或已禁用
     */
    public static HologramApi getOrThrow() {
        HologramApi api = instance;
        if (api == null) {
            throw new IllegalStateException("WooHolograms is not loaded");
        }
        return api;
    }

    /**
     * WooHolograms 是否已加载且启用
     *
     * @return 是否可用
     */
    public static boolean isAvailable() {
        return instance != null;
    }
}
