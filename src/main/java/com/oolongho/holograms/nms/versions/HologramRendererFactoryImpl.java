package com.oolongho.holograms.nms.versions;

import com.oolongho.holograms.nms.NmsHologramRendererFactory;
import com.oolongho.holograms.nms.renderer.*;
import com.oolongho.holograms.nms.versions.renderer.*;

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

    /**
     * 获取实体ID生成器
     * @return 实体ID生成器
     */
    public EntityIdGenerator getEntityIdGenerator() {
        return entityIdGenerator;
    }

    @Override
    public NmsTextHologramRenderer createTextRenderer() {
        return new TextHologramRendererImpl(entityIdGenerator);
    }

    @Override
    public NmsItemDisplayRenderer createIconRenderer() {
        return new ItemDisplayRendererImpl(entityIdGenerator);
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
    public NmsBlockHologramRenderer createBlockRenderer() {
        return new BlockHologramRendererImpl(entityIdGenerator);
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
