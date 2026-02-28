package com.oolonghoo.holograms.nms;

import com.oolonghoo.holograms.nms.renderer.*;

/**
 * 用于创建全息图渲染器的工厂接口
 *
 * <p>此工厂的实现提供适合渲染不同类型全息图元素的渲染器实例。</p>
 *
 * @author oolongho
 * @since 1.0.0
 */
public interface NmsHologramRendererFactory {

    /**
     * 创建用于显示文本全息图的渲染器
     *
     * @return 新的 {@link NmsTextHologramRenderer} 实例
     */
    NmsTextHologramRenderer createTextRenderer();

    /**
     * 创建用于显示图标全息图的渲染器
     *
     * @return 新的 {@link NmsIconHologramRenderer} 实例
     */
    NmsIconHologramRenderer createIconRenderer();

    /**
     * 创建用于显示头颅全息图的渲染器
     *
     * @return 新的 {@link NmsHeadHologramRenderer} 实例
     */
    NmsHeadHologramRenderer createHeadRenderer();

    /**
     * 创建用于显示小型头颅全息图的渲染器
     *
     * @return 新的 {@link NmsSmallHeadHologramRenderer} 实例
     */
    NmsSmallHeadHologramRenderer createSmallHeadRenderer();

    /**
     * 创建用于显示实体全息图的渲染器
     *
     * @return 新的 {@link NmsEntityHologramRenderer} 实例
     */
    NmsEntityHologramRenderer createEntityRenderer();

    /**
     * 创建用于显示可点击全息图的渲染器
     *
     * <p>这些全息图支持玩家交互，启用点击时的操作。</p>
     *
     * @return 新的 {@link NmsClickableHologramRenderer} 实例
     */
    NmsClickableHologramRenderer createClickableRenderer();
}
