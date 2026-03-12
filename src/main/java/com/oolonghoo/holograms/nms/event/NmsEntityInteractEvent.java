package com.oolonghoo.holograms.nms.event;

import org.bukkit.entity.Player;

/**
 * 玩家与实体交互事件
 *
 * <p>这不是一个 Bukkit 事件，无法通过 Bukkit 监听器捕获！</p>
 *
 * 
 * 
 */
public class NmsEntityInteractEvent {

    private final Player player;
    private final int entityId;
    private final NmsEntityInteractAction action;
    private boolean handled = false;

    public NmsEntityInteractEvent(Player player, int entityId, NmsEntityInteractAction action) {
        this.player = player;
        this.entityId = entityId;
        this.action = action;
    }

    /**
     * 获取与实体交互的玩家
     *
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取玩家交互的实体 ID
     *
     * @return 实体 ID
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * 获取交互类型
     *
     * @return 交互类型
     */
    public NmsEntityInteractAction getAction() {
        return action;
    }

    /**
     * 检查事件是否已被处理
     * 如果已处理，点击将被取消，服务器将无法检测到它
     *
     * @return 是否已处理
     */
    public boolean isHandled() {
        return handled;
    }

    /**
     * 设置事件是否已被处理
     * 如果设置为 true，点击将被取消，服务器将无法检测到它
     *
     * @param handled 是否已处理
     */
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
