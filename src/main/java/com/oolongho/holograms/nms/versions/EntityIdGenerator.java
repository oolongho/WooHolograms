package com.oolongho.holograms.nms.versions;

import com.oolongho.holograms.nms.util.WooHologramsException;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体 ID 生成器
 * 用于生成唯一的实体 ID
 *
 * 
 * 
 */
public class EntityIdGenerator {

    private static final AtomicInteger ENTITY_COUNTER;

    static {
        try {
            Field field = Entity.class.getDeclaredField("ENTITY_COUNTER");
            field.setAccessible(true);
            ENTITY_COUNTER = (AtomicInteger) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WooHologramsException("Failed to access Entity.ENTITY_COUNTER", e);
        }
    }

    /**
     * 获取一个空闲的实体 ID
     *
     * @return 新的实体 ID
     */
    public int getFreeEntityId() {
        try {
            return ENTITY_COUNTER.incrementAndGet();
        } catch (Exception e) {
            throw new WooHologramsException("Failed to get new entity ID", e);
        }
    }
}
