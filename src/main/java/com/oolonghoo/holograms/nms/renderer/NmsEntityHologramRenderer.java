package com.oolonghoo.holograms.nms.renderer;

import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import org.bukkit.entity.EntityType;

/**
 * 实体全息图渲染器接口
 * 用于显示自定义实体类型的全息图
 *
 * @author oolongho
 */
public interface NmsEntityHologramRenderer extends NmsHologramRenderer {

    /**
     * 获取实体类型
     *
     * @return 实体类型
     */
    EntityType getEntityType();

    /**
     * 设置实体类型
     *
     * @param entityType 实体类型
     */
    void setEntityType(EntityType entityType);
}
