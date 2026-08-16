package com.oolongho.holograms.nms.versions;

import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实体 ID 生成器
 * 用于生成唯一的实体 ID
 *
 * 1.21.x ~ 26.1: 共享 vanilla 的 Entity.ENTITY_COUNTER（需与 vanilla 一致使用 getAndIncrement，
 *                混用 incrementAndGet 会产生 off-by-one ID 碰撞）
 * 26.2+: Mojang 移除了该字段，回退为独立高位计数器（基值远超 vanilla 实际分配范围，
 *        且避开 packetevents 等常见库的低基值区间，不会与服务器或其他插件冲突）
 */
public class EntityIdGenerator {

    /** 独立计数器基值，vanilla 计数器实际不可能达到此量级 */
    private static final int STANDALONE_BASE = 1_000_000_000;

    private static final AtomicInteger ENTITY_COUNTER = resolveCounter();

    private static AtomicInteger resolveCounter() {
        try {
            Field field = Entity.class.getDeclaredField("ENTITY_COUNTER");
            field.setAccessible(true);
            return (AtomicInteger) field.get(null);
        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().info("[WooHolograms] Entity.ENTITY_COUNTER unavailable (MC 26.2+), using standalone high-base entity id counter");
            return new AtomicInteger(STANDALONE_BASE);
        }
    }

    /**
     * 获取一个空闲的实体 ID
     *
     * @return 新的实体 ID
     */
    public int getFreeEntityId() {
        return ENTITY_COUNTER.getAndIncrement();
    }
}
