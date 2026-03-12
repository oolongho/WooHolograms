package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.util.WooHologramsException;
import net.minecraft.world.entity.EntityType;
import org.bukkit.NamespacedKey;

import java.util.Optional;

/**
 * 实体类型注册表
 * 用于将 Bukkit EntityType 转换为 NMS EntityTypes
 *
 * 
 * 
 */
public final class EntityTypeRegistry {

    private EntityTypeRegistry() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取实体类型的高度
     *
     * @param entityType Bukkit 实体类型
     * @return 高度
     */
    public static double getEntityTypeHeight(org.bukkit.entity.EntityType entityType) {
        return findEntityTypes(entityType).getDimensions().height();
    }

    /**
     * 查找 NMS EntityTypes
     *
     * @param entityType Bukkit 实体类型
     * @return NMS EntityTypes
     */
    static EntityType<?> findEntityTypes(org.bukkit.entity.EntityType entityType) {
        NamespacedKey namespacedKey = getNamespacedKey(entityType);
        String key = namespacedKey.getKey();
        Optional<EntityType<?>> entityTypes = EntityType.byString(key);
        if (entityTypes.isPresent()) {
            return entityTypes.get();
        }
        throw new WooHologramsException("Invalid entity type: " + entityType);
    }

    private static NamespacedKey getNamespacedKey(org.bukkit.entity.EntityType entityType) {
        try {
            return entityType.getKey();
        } catch (IllegalStateException e) {
            throw new WooHologramsException("Couldn't get key for entity type: " + entityType);
        }
    }
}
