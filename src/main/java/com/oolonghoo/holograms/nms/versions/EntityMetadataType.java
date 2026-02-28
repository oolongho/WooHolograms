package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.util.ReflectUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * 实体元数据类型
 * 用于构建实体的元数据项
 *
 * @param <T> 元数据值的类型
 * @author oolongho
 * @since 1.0.0
 */
class EntityMetadataType<T> {

    // 实体属性（不可见、着火等）
    private static final EntityDataAccessor<Byte> ENTITY_PROPERTIES_OBJECT
            = ReflectUtil.getFieldValue(Entity.class, "DATA_SHARED_FLAGS_ID");

    // 实体自定义名称
    private static final EntityDataAccessor<Optional<Component>> ENTITY_CUSTOM_NAME_OBJECT
            = ReflectUtil.getFieldValue(Entity.class, "DATA_CUSTOM_NAME");

    // 实体自定义名称是否可见
    private static final EntityDataAccessor<Boolean> ENTITY_CUSTOM_NAME_VISIBLE_OBJECT
            = ReflectUtil.getFieldValue(Entity.class, "DATA_CUSTOM_NAME_VISIBLE");

    // 实体是否静音
    private static final EntityDataAccessor<Boolean> ENTITY_SILENT_OBJECT
            = ReflectUtil.getFieldValue(Entity.class, "DATA_SILENT");

    // 实体是否有重力
    private static final EntityDataAccessor<Boolean> ENTITY_HAS_NO_GRAVITY_OBJECT
            = ReflectUtil.getFieldValue(Entity.class, "DATA_NO_GRAVITY");

    // 盔甲架属性
    private static final EntityDataAccessor<Byte> ARMOR_STAND_PROPERTIES_OBJECT
            = ReflectUtil.getFieldValue(ArmorStand.class, "DATA_CLIENT_FLAGS");

    // 物品实体的物品
    private static final EntityDataAccessor<ItemStack> ITEM_STACK_OBJECT
            = ReflectUtil.getFieldValue(ItemEntity.class, "DATA_ITEM");

    // 盔甲架头部旋转（用于模拟阴影方向）
    private static final EntityDataAccessor<net.minecraft.core.Rotations> ARMOR_STAND_HEAD_POSE_OBJECT
            = ReflectUtil.getFieldValue(ArmorStand.class, "DATA_HEAD_POSE");

    // 盔甲架身体旋转
    private static final EntityDataAccessor<net.minecraft.core.Rotations> ARMOR_STAND_BODY_POSE_OBJECT
            = ReflectUtil.getFieldValue(ArmorStand.class, "DATA_BODY_POSE");

    // 静态实例
    static final EntityMetadataType<Byte> ENTITY_PROPERTIES = new EntityMetadataType<>(ENTITY_PROPERTIES_OBJECT);
    static final EntityMetadataType<Optional<Component>> ENTITY_CUSTOM_NAME = new EntityMetadataType<>(ENTITY_CUSTOM_NAME_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new EntityMetadataType<>(ENTITY_CUSTOM_NAME_VISIBLE_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_SILENT = new EntityMetadataType<>(ENTITY_SILENT_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_HAS_NO_GRAVITY = new EntityMetadataType<>(ENTITY_HAS_NO_GRAVITY_OBJECT);
    static final EntityMetadataType<Byte> ARMOR_STAND_PROPERTIES = new EntityMetadataType<>(ARMOR_STAND_PROPERTIES_OBJECT);
    static final EntityMetadataType<ItemStack> ITEM_STACK = new EntityMetadataType<>(ITEM_STACK_OBJECT);
    static final EntityMetadataType<net.minecraft.core.Rotations> ARMOR_STAND_HEAD_POSE = new EntityMetadataType<>(ARMOR_STAND_HEAD_POSE_OBJECT);
    static final EntityMetadataType<net.minecraft.core.Rotations> ARMOR_STAND_BODY_POSE = new EntityMetadataType<>(ARMOR_STAND_BODY_POSE_OBJECT);

    private final EntityDataAccessor<T> entityDataAccessor;

    private EntityMetadataType(EntityDataAccessor<T> entityDataAccessor) {
        this.entityDataAccessor = entityDataAccessor;
    }

    /**
     * 构建一个元数据项
     *
     * @param value 值
     * @return 元数据项
     */
    SynchedEntityData.DataItem<T> construct(T value) {
        return new SynchedEntityData.DataItem<>(entityDataAccessor, value);
    }
}
