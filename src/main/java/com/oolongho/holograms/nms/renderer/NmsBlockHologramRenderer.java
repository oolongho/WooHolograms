package com.oolongho.holograms.nms.renderer;

import com.oolongho.holograms.nms.NmsHologramRenderer;
import org.bukkit.Material;

/**
 * 方块全息图渲染器接口
 * 用于使用 BlockDisplay 实体展示方块
 */
public interface NmsBlockHologramRenderer extends NmsHologramRenderer {

    /**
     * 获取方块材质
     *
     * @return 方块材质
     */
    Material getBlockMaterial();

    /**
     * 设置方块材质
     *
     * @param material 方块材质
     */
    void setBlockMaterial(Material material);
}
