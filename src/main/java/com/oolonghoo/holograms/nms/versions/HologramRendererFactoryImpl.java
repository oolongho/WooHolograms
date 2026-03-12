package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
import com.oolonghoo.holograms.nms.renderer.*;
import com.oolonghoo.holograms.nms.versions.renderer.*;

/**
 * 全息图渲染器工厂实现
 *
 * 
 * 
 */
public class HologramRendererFactoryImpl implements NmsHologramRendererFactory {

    private final EntityIdGenerator entityIdGenerator;

    public HologramRendererFactoryImpl(EntityIdGenerator entityIdGenerator) {
        this.entityIdGenerator = entityIdGenerator;
    }

    @Override
    public NmsTextHologramRenderer createTextRenderer() {
        return new TextHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsIconHologramRenderer createIconRenderer() {
        return new IconHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsHeadHologramRenderer createHeadRenderer() {
        return new HeadHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsSmallHeadHologramRenderer createSmallHeadRenderer() {
        return new SmallHeadHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsEntityHologramRenderer createEntityRenderer() {
        return new EntityHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsClickableHologramRenderer createClickableRenderer() {
        return new ClickableHologramRendererImpl(entityIdGenerator);
    }
}
