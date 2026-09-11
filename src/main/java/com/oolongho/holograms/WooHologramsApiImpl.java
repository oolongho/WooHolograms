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
    private final com.oolongho.holograms.api.registry.ActionTypeRegistry actionRegistry =
            new com.oolongho.holograms.action.ActionTypeRegistryImpl();
    private final com.oolongho.holograms.api.registry.AnimationRegistry animationRegistry;

    public WooHologramsApiImpl(WooHolograms plugin) {
        this.plugin = plugin;
        this.animationRegistry = new com.oolongho.holograms.animation.AnimationRegistryImpl(plugin.getAnimationManager());
    }

    @Override
    public HologramRegistry holograms() {
        return plugin.getHologramManager();
    }

    @Override
    public com.oolongho.holograms.api.registry.ActionTypeRegistry actions() {
        return actionRegistry;
    }

    @Override
    public com.oolongho.holograms.api.registry.AnimationRegistry animations() {
        return animationRegistry;
    }
}
