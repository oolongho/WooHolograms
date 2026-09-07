package com.oolongho.holograms.animation;


import com.oolongho.holograms.api.registry.AnimationRegistry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnimationRegistry 的实现
 *
 * <p>包装内部 AnimationManager。内置动画（wave/typewriter/blink/scroll/gradient）
 * 不允许覆盖/注销；自定义动画允许重复注册（视为更新）。</p>
 */
public class AnimationRegistryImpl implements AnimationRegistry {

    /** 内置动画名称（小写） */
    private static final Set<String> BUILT_IN = Set.of("wave", "typewriter", "blink", "scroll", "gradient");

    /** 通过 API 注册的自定义动画名称（小写） */
    private final Set<String> customNames = ConcurrentHashMap.newKeySet();

    private final AnimationManager manager;

    public AnimationRegistryImpl(AnimationManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean register(com.oolongho.holograms.api.animation.TextAnimation animation) {
        if (animation == null || animation.getName() == null || animation.getName().isEmpty()) {
            return false;
        }
        String lower = animation.getName().toLowerCase(Locale.ROOT);
        if (BUILT_IN.contains(lower)) {
            return false;
        }
        manager.registerAnimation(lower, new TextAnimationBridge(animation));
        customNames.add(lower);
        return true;
    }

    @Override
    public boolean unregister(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        // 仅允许注销本注册表注册的动画；内置与 YML 加载的动画不可通过 API 注销
        if (!customNames.remove(lower)) {
            return false;
        }
        return manager.unregisterAnimation(lower) != null;
    }

    @Override
    public boolean isRegistered(String name) {
        return name != null && manager.getAnimation(name) != null;
    }

    @Override
    public Set<String> getAnimationNames() {
        Set<String> names = new HashSet<>();
        for (TextAnimation animation : manager.getAnimations()) {
            names.add(animation.getName().toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * API 动画 → 内部动画的桥接
     * 内部动画体系基于抽象类（携带预编译缓存），此处适配为继承实现
     */
    private static final class TextAnimationBridge extends TextAnimation {

        private final com.oolongho.holograms.api.animation.TextAnimation delegate;

        TextAnimationBridge(com.oolongho.holograms.api.animation.TextAnimation delegate) {
            super(delegate.getName(), Math.max(1, delegate.getSpeed()), Math.max(0, delegate.getPause()),
                    delegate.getAliases().toArray(new String[0]));
            this.delegate = delegate;
        }

        @Override
        public String animate(String string, long step, String... args) {
            return delegate.animate(string, step, args);
        }

        /**
         * 预编译帧（内部优化路径）。
         * 第三方动画无法静态枚举全部帧，退化为单帧占位；
         * 实际渲染走 animate()，不受此影响
         */
        @Override
        protected String[] precompile(String text, String... args) {
            return new String[]{delegate.animate(text, 0L, args)};
        }
    }
}
