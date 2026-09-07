package com.oolongho.holograms.api.registry;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 动作类型注册表
 *
 * <p>用于注册/注销自定义动作类型。注册后即可在动作配置中以
 * {@code 名称:参数} 的形式使用（与内置动作格式一致）。</p>
 *
 * <p>注册约定：</p>
 * <ul>
 *   <li>名称仅允许字母/数字/下划线，注册时不区分大小写（内部统一大写）</li>
 *   <li>不能覆盖内置动作类型（NONE/MESSAGE/COMMAND/CONSOLE/SOUND/TELEPORT/SERVER/NEXT_PAGE/PREV_PAGE/PAGE）</li>
 *   <li>重复注册同名自定义类型视为更新（覆盖旧执行器）</li>
 *   <li>插件禁用时建议调用 {@link #unregister(String)} 清理（服务器关闭时无需）</li>
 * </ul>
 */
public interface ActionTypeRegistry {

    /**
     * 注册自定义动作类型
     *
     * @param name     动作类型名称（字母/数字/下划线）
     * @param executor 执行器
     * @return 是否成功（名称非法或与内置类型冲突返回 false）
     */
    boolean register(String name, ActionExecutor executor);

    /**
     * 注销自定义动作类型
     *
     * @param name 动作类型名称
     * @return 是否成功注销（不存在或为内置类型返回 false）
     */
    boolean unregister(String name);

    /**
     * 动作类型是否已注册
     *
     * @param name 动作类型名称
     * @return 是否已注册
     */
    boolean isRegistered(String name);

    /**
     * 获取全部已注册的动作类型名称（含内置）
     *
     * @return 只读名称集合
     */
    Set<String> getRegisteredNames();

    /**
     * 按名称获取动作类型名称（大小写归一化辅助；未注册返回 null）
     *
     * @param name 任意大小写的名称
     * @return 归一化后的名称；未注册返回 null
     */
    @Nullable
    String normalize(String name);
}
