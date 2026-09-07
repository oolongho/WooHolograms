package com.oolongho.holograms.action;

import com.oolongho.holograms.api.registry.ActionExecutor;
import com.oolongho.holograms.api.registry.ActionTypeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActionTypeRegistry 的实现
 *
 * <p>基于内部 ActionType 静态注册表，将 API 执行器包装为匿名 ActionType 注册。
 * 维护"自定义名称"集合用于区分内置类型：内置类型不可覆盖/注销，
 * 自定义类型允许重复注册（视为更新）。</p>
 */
public class ActionTypeRegistryImpl implements ActionTypeRegistry {

    /** 通过 API 注册的自定义类型名称（大写） */
    private final Set<String> customNames = ConcurrentHashMap.newKeySet();

    @Override
    public boolean register(String name, ActionExecutor executor) {
        if (name == null || executor == null || !isValidName(name)) {
            return false;
        }
        String upper = normalizeName(name);
        // 内置类型不允许覆盖
        if (!customNames.contains(upper) && ActionType.getByName(upper) != null) {
            return false;
        }
        // 自定义重注册视为更新：先注销旧实例
        ActionType.unregister(upper);
        try {
            new ActionType(upper) {
                @Override
                public boolean execute(org.bukkit.entity.Player player, String... args) {
                    return executor.execute(player, args);
                }

                {
                    register();
                }
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
        customNames.add(upper);
        return true;
    }

    @Override
    public boolean unregister(String name) {
        if (name == null) {
            return false;
        }
        String upper = normalizeName(name);
        if (!customNames.remove(upper)) {
            return false;
        }
        ActionType.unregister(upper);
        return true;
    }

    @Override
    public boolean isRegistered(String name) {
        return name != null && ActionType.getByName(normalizeName(name)) != null;
    }

    @Override
    public Set<String> getRegisteredNames() {
        Set<String> names = new java.util.HashSet<>();
        for (ActionType type : ActionType.getActionTypes()) {
            names.add(type.getName());
        }
        return Collections.unmodifiableSet(names);
    }

    @Override
    @Nullable
    public String normalize(String name) {
        if (name == null) {
            return null;
        }
        String upper = normalizeName(name);
        return ActionType.getByName(upper) != null ? upper : null;
    }

    /** 名称归一化（大写） */
    private static String normalizeName(String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    /** 名称合法性：仅字母/数字/下划线 */
    private static boolean isValidName(String name) {
        if (name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }
}
