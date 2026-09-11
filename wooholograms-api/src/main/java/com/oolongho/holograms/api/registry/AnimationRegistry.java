package com.oolongho.holograms.api.registry;

import com.oolongho.holograms.api.animation.TextAnimation;

import java.util.Set;

/**
 * 动画注册表
 *
 * <p>用于注册/注销自定义文本动画。注册后即可在行内容中以
 * {@code <#ANIM:名称:参数>文本</#ANIM>} 语法使用。</p>
 *
 * <p>注册约定：</p>
 * <ul>
 *   <li>名称不区分大小写（内部统一小写）</li>
 *   <li>不能覆盖内置动画（wave/typewriter/blink/scroll/gradient）</li>
 *   <li>重复注册同名自定义动画视为更新（覆盖旧实例）</li>
 *   <li>注册后新解析的行内容立即生效；已有行等待下一次内容刷新</li>
 *   <li>注意：插件重载（/wh reload）会重建动画表，第三方插件应监听
 *       HologramsLoadedEvent 并在其中重新注册</li>
 * </ul>
 */
public interface AnimationRegistry {

    /**
     * 注册自定义文本动画
     *
     * @param animation 动画实例
     * @return 是否成功（实例非法或与内置动画冲突返回 false）
     */
    boolean register(TextAnimation animation);

    /**
     * 注销自定义文本动画
     *
     * @param name 动画名称
     * @return 是否成功注销（不存在或为内置动画返回 false）
     */
    boolean unregister(String name);

    /**
     * 动画是否已注册
     *
     * @param name 动画名称
     * @return 是否已注册
     */
    boolean isRegistered(String name);

    /**
     * 获取全部已注册的动画名称（含内置）
     *
     * @return 只读名称集合
     */
    Set<String> getAnimationNames();
}
