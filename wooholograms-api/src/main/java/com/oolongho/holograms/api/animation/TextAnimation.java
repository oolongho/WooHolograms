package com.oolongho.holograms.api.animation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 文本动画 SPI 接口
 *
 * <p>第三方插件实现此接口并通过 {@code AnimationRegistry#register} 注册自定义动画，
 * 之后即可在行内容中使用 {@code <#ANIM:名称:参数>文本</#ANIM>} 语法调用。</p>
 *
 * <pre>{@code
 * public class ReverseAnimation implements TextAnimation {
 *     public String getName() { return "reverse"; }
 *     public int getSpeed() { return 4; }
 *     public int getPause() { return 0; }
 *     public List<String> getAliases() { return List.of(); }
 *     public String animate(String text, long step, String... args) {
 *         return new StringBuilder(text).reverse().toString();
 *     }
 * }
 * api.animations().register(new ReverseAnimation());
 * }</pre>
 */
public interface TextAnimation {

    /**
     * 获取动画名称（行内容中 {@code <#ANIM:名称>} 使用的标识，不区分大小写）
     *
     * @return 动画名称
     */
    @NotNull
    String getName();

    /**
     * 获取动画别名（可选，同样可用于内容引用）
     *
     * @return 别名列表（可为空列表）
     */
    @NotNull
    List<String> getAliases();

    /**
     * 获取动画速度（每步持续的 tick 数；1 = 每 tick 一帧）
     *
     * @return 速度 tick 数
     */
    int getSpeed();

    /**
     * 获取动画末尾暂停时间（tick，0 表示不暂停）
     *
     * @return 暂停 tick 数
     */
    int getPause();

    /**
     * 渲染动画帧
     *
     * @param text  原始文本（未着色）
     * @param step  全局动画步进计数（每 tick 递增）
     * @param args  内容中的参数串（{@code <#ANIM:name:args>} 的 args，逗号分隔原样传入；可为空）
     * @return 渲染后的文本（可含 {@code &} 颜色代码，插件会统一着色）
     */
    @NotNull
    String animate(@NotNull String text, long step, @NotNull String... args);
}
