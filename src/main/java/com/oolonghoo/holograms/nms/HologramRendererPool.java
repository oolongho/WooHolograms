package com.oolonghoo.holograms.nms;

import com.oolonghoo.holograms.hologram.HologramType;
import com.oolonghoo.holograms.nms.renderer.NmsTextHologramRenderer;
import com.oolonghoo.holograms.nms.renderer.NmsIconHologramRenderer;
import com.oolonghoo.holograms.nms.renderer.NmsHeadHologramRenderer;
import com.oolonghoo.holograms.nms.renderer.NmsSmallHeadHologramRenderer;
import com.oolonghoo.holograms.nms.renderer.NmsEntityHologramRenderer;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HologramRendererPool {

    private final NmsHologramRendererFactory factory;
    private final Map<HologramType, Queue<NmsHologramRenderer>> pool;
    private final int maxSize;
    private final boolean enabled;

    public HologramRendererPool(NmsHologramRendererFactory factory, int maxSize, boolean enabled) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.enabled = enabled;
        this.pool = new EnumMap<>(HologramType.class);
        
        for (HologramType type : HologramType.values()) {
            pool.put(type, new LinkedList<>());
        }
    }

    public NmsHologramRenderer obtain(HologramType type) {
        if (!enabled) {
            return createRenderer(type);
        }
        
        Queue<NmsHologramRenderer> queue = pool.get(type);
        if (queue != null && !queue.isEmpty()) {
            NmsHologramRenderer renderer = queue.poll();
            if (renderer != null && !renderer.isDestroyed()) {
                return renderer;
            }
        }
        
        return createRenderer(type);
    }

    public void release(NmsHologramRenderer renderer) {
        if (!enabled || renderer == null || renderer.isDestroyed()) {
            return;
        }
        
        HologramType type = getRendererType(renderer);
        if (type == null) {
            return;
        }
        
        Queue<NmsHologramRenderer> queue = pool.get(type);
        if (queue != null && queue.size() < maxSize) {
            queue.offer(renderer);
        }
    }

    public void clear() {
        for (Queue<NmsHologramRenderer> queue : pool.values()) {
            queue.clear();
        }
    }

    public int getTotalPooledSize() {
        int total = 0;
        for (Queue<NmsHologramRenderer> queue : pool.values()) {
            total += queue.size();
        }
        return total;
    }

    public int getPooledSize(HologramType type) {
        Queue<NmsHologramRenderer> queue = pool.get(type);
        return queue != null ? queue.size() : 0;
    }

    private NmsHologramRenderer createRenderer(HologramType type) {
        if (factory == null) {
            return null;
        }
        
        switch (type) {
            case TEXT:
                return factory.createTextRenderer();
            case ICON:
                return factory.createIconRenderer();
            case HEAD:
                return factory.createHeadRenderer();
            case SMALLHEAD:
                return factory.createSmallHeadRenderer();
            case ENTITY:
                return factory.createEntityRenderer();
            default:
                return factory.createTextRenderer();
        }
    }

    private HologramType getRendererType(NmsHologramRenderer renderer) {
        if (renderer instanceof NmsTextHologramRenderer) {
            return HologramType.TEXT;
        } else if (renderer instanceof NmsIconHologramRenderer) {
            return HologramType.ICON;
        } else if (renderer instanceof NmsHeadHologramRenderer) {
            return HologramType.HEAD;
        } else if (renderer instanceof NmsSmallHeadHologramRenderer) {
            return HologramType.SMALLHEAD;
        } else if (renderer instanceof NmsEntityHologramRenderer) {
            return HologramType.ENTITY;
        }
        return HologramType.TEXT;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
