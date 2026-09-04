package com.oolongho.holograms.nms.versions;

import com.oolongho.holograms.nms.util.WooHologramsException;
import net.minecraft.world.entity.EntityType;
import org.bukkit.craftbukkit.entity.CraftEntityType;

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
     * 使用 CraftBukkit 桥接而非 EntityType.byString（MC 26.2 起该方法已被移除），
     * bukkitToMinecraft 在 1.21.x ~ 26.2+ 上签名一致，编译产物可直接跨版本链接
     *
     * @param entityType Bukkit 实体类型
     * @return NMS EntityTypes
     */
    static EntityType<?> findEntityTypes(org.bukkit.entity.EntityType entityType) {
        EntityType<?> type = CraftEntityType.bukkitToMinecraft(entityType);
        if (type == null) {
            throw new WooHologramsException("Invalid entity type: " + entityType);
        }
        return type;
    }
}
