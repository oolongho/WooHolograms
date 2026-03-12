package com.oolonghoo.holograms.nms.versions.renderer;
import com.oolonghoo.holograms.nms.renderer.NmsSmallHeadHologramRenderer;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;

/**
 * 小型头颅全息图渲染器实现
 *
 * 
 * 
 */
public class SmallHeadHologramRendererImpl extends HeadHologramRendererImpl implements NmsSmallHeadHologramRenderer {

    public SmallHeadHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        super(entityIdGenerator, true);
    }
}
