package com.oolonghoo.holograms.nms.event;

/**
 * 表示玩家与实体之间的交互类型
 *
 * @author oolongho
 * @since 1.0.0
 */
public enum NmsEntityInteractAction {
    /**
     * 左键点击
     */
    LEFT_CLICK,

    /**
     * 右键点击
     */
    RIGHT_CLICK,

    /**
     * 潜行左键点击
     */
    SHIFT_LEFT_CLICK,

    /**
     * 潜行右键点击
     */
    SHIFT_RIGHT_CLICK
}
