package com.oolongho.holograms.nms;

import com.oolongho.holograms.nms.renderer.*;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;

/**
 * 用于创建全息图渲染器的工厂接口
 *
 * <p>此工厂的实现提供适合渲染不同类型全息图元素的渲染器实例。</p>
 *
 * 
 * 
 */
public interface NmsHologramRendererFactory {

    /**
     * 获取实体ID生成器
     *
     * @return {@link EntityIdGenerator} 实例
     */
    EntityIdGenerator getEntityIdGenerator();

    /**
     * 创建用于显示文本全息图的渲染器
     *
     * @return 新的 {@link NmsTextHologramRenderer} 实例
     */
    NmsTextHologramRenderer createTextRenderer();

    /**
     * 创建用于显示图标全息图的渲染器
     *
     * @return 新的 {@link NmsItemDisplayRenderer} 实例
     */
    NmsItemDisplayRenderer createIconRenderer();

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
     * 创建用于显示方块全息图的渲染器
     *
     * @return 新的 {@link NmsBlockHologramRenderer} 实例
     */
    NmsBlockHologramRenderer createBlockRenderer();

    NmsEntityHologramRenderer createEntityRenderer();

    NmsClickableHologramRenderer createClickableRenderer();
}
