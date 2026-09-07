package com.oolongho.holograms;

import com.oolongho.holograms.api.HologramApi;
import com.oolongho.holograms.api.hologram.HologramRegistry;

/**
 * HologramApi 的插件侧实现
 *
 * <p>将 API 门面转发到内部管理器。由主类在 onEnable 时注册到
 * {@link com.oolongho.holograms.api.WooHologramsApiProvider}，onDisable 时注销。</p>
 */
public class WooHologramsApiImpl implements HologramApi {

    private final WooHolograms plugin;

    public WooHologramsApiImpl(WooHolograms plugin) {
        this.plugin = plugin;
    }

    @Override
    public HologramRegistry holograms() {
        return plugin.getHologramManager();
    }
}
