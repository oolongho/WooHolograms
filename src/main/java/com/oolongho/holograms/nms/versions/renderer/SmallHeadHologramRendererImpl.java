package com.oolongho.holograms.nms.versions.renderer;
import com.oolongho.holograms.nms.renderer.NmsSmallHeadHologramRenderer;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;

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
