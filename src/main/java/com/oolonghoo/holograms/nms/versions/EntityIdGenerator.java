package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.util.ReflectField;
import com.oolonghoo.holograms.nms.util.WooHologramsException;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体 ID 生成器
 * 用于生成唯一的实体 ID
 *
 * 
 * 
 */
public class EntityIdGenerator {

    private static final ReflectField<AtomicInteger> ENTITY_COUNT_FIELD = new ReflectField<>(Entity.class, "ENTITY_COUNTER");

    /**
     * 获取一个空闲的实体 ID
     *
     * @return 新的实体 ID
     */
    public int getFreeEntityId() {
        try {
            /*
             * 我们以与服务器相同的方式获取新的实体 ID。这是为了确保
             * ID 是唯一的，不会与任何其他实体冲突。
             */
            AtomicInteger entityCount = ENTITY_COUNT_FIELD.get(null);
            return entityCount.incrementAndGet();
        } catch (Exception e) {
            throw new WooHologramsException("Failed to get new entity ID", e);
        }
    }
}
