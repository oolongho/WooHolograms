package com.oolonghoo.holograms.nms;

import com.oolonghoo.holograms.nms.event.NmsEntityInteractEvent;

/**
 * NMS 数据包监听器接口
 * 用于处理 NMS 相关事件
 *
 * @author oolongho
 * @since 1.0.0
 */
public interface NmsPacketListener {

    /**
     * 当玩家与实体交互时调用
     *
     * @param event 交互事件
     * @see NmsEntityInteractEvent
     */
    void onEntityInteract(NmsEntityInteractEvent event);
}
