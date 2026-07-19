package com.oolongho.holograms.nms.versions;

import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Interaction;
import org.joml.Vector3fc;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 实体元数据类型
 * 用于构建实体的元数据项
 *
 * 26.1+ 中 NMS 实体字段访问权限从 public 变为 private/protected，
 * 因此需要通过反射获取 EntityDataAccessor 实例。
 *
 * @param <T> 元数据值的类型
 */
class EntityMetadataType<T> {

    // 实体属性（不可见、着火等）
    static final EntityDataAccessor<Byte> ENTITY_PROPERTIES_OBJECT = getAccessor(Entity.class, "DATA_SHARED_FLAGS_ID");

    // 实体自定义名称（26.1+ 中类型为 Optional<Component>）
    private static final EntityDataAccessor<Optional<Component>> ENTITY_CUSTOM_NAME_OBJECT = getAccessor(Entity.class, "DATA_CUSTOM_NAME");

    // 实体自定义名称是否可见
    private static final EntityDataAccessor<Boolean> ENTITY_CUSTOM_NAME_VISIBLE_OBJECT = getAccessor(Entity.class, "DATA_CUSTOM_NAME_VISIBLE");

    // 实体是否静音
    private static final EntityDataAccessor<Boolean> ENTITY_SILENT_OBJECT = getAccessor(Entity.class, "DATA_SILENT");

    // 实体是否有重力
    private static final EntityDataAccessor<Boolean> ENTITY_HAS_NO_GRAVITY_OBJECT = getAccessor(Entity.class, "DATA_NO_GRAVITY");

    // 盔甲架属性
    private static final EntityDataAccessor<Byte> ARMOR_STAND_PROPERTIES_OBJECT = getAccessor(ArmorStand.class, "DATA_CLIENT_FLAGS");

    // 物品实体的物品
    private static final EntityDataAccessor<ItemStack> ITEM_STACK_OBJECT = getAccessor(ItemEntity.class, "DATA_ITEM");

    // 盔甲架头部旋转（用于模拟阴影方向）
    private static final EntityDataAccessor<Rotations> ARMOR_STAND_HEAD_POSE_OBJECT = getAccessor(ArmorStand.class, "DATA_HEAD_POSE");

    // 盔甲架身体旋转
    private static final EntityDataAccessor<Rotations> ARMOR_STAND_BODY_POSE_OBJECT = getAccessor(ArmorStand.class, "DATA_BODY_POSE");

    // Display Entity - Billboard 渲染约束
    private static final EntityDataAccessor<Byte> DISPLAY_BILLBOARD_OBJECT = getAccessor(Display.class, "DATA_BILLBOARD_RENDER_CONSTRAINTS_ID");

    // Display Entity - 亮度覆盖
    private static final EntityDataAccessor<Integer> DISPLAY_BRIGHTNESS_OBJECT = getAccessor(Display.class, "DATA_BRIGHTNESS_OVERRIDE_ID");

    // Display Entity - 平移（26.1+ 字段类型为 Vector3fc，Vector3f 实现该接口，兼容 1.21.x 与 26.1+）
    private static final EntityDataAccessor<Vector3fc> DISPLAY_TRANSLATION_OBJECT = getAccessor(Display.class, "DATA_TRANSLATION_ID");

    // Display Entity - 缩放
    private static final EntityDataAccessor<Vector3fc> DISPLAY_SCALE_OBJECT = getAccessor(Display.class, "DATA_SCALE_ID");

    // Display Entity - 左旋转
    private static final EntityDataAccessor<org.joml.Quaternionf> DISPLAY_LEFT_ROTATION_OBJECT = getAccessor(Display.class, "DATA_LEFT_ROTATION_ID");

    // Display Entity - 右旋转
    private static final EntityDataAccessor<org.joml.Quaternionf> DISPLAY_RIGHT_ROTATION_OBJECT = getAccessor(Display.class, "DATA_RIGHT_ROTATION_ID");

    // Display Entity - 阴影半径
    private static final EntityDataAccessor<Float> DISPLAY_SHADOW_RADIUS_OBJECT = getAccessor(Display.class, "DATA_SHADOW_RADIUS_ID");

    // Display Entity - 阴影强度
    private static final EntityDataAccessor<Float> DISPLAY_SHADOW_STRENGTH_OBJECT = getAccessor(Display.class, "DATA_SHADOW_STRENGTH_ID");

    // Display Entity - 发光颜色覆盖
    private static final EntityDataAccessor<Integer> DISPLAY_GLOW_COLOR_OVERRIDE_OBJECT = getAccessor(Display.class, "DATA_GLOW_COLOR_OVERRIDE_ID");

    // ItemDisplay Entity - 物品
    private static final EntityDataAccessor<ItemStack> ITEM_DISPLAY_ITEM_STACK_OBJECT = getAccessor(Display.ItemDisplay.class, "DATA_ITEM_STACK_ID");

    // TextDisplay Entity - 文本内容
    private static final EntityDataAccessor<Component> TEXT_DISPLAY_TEXT_OBJECT = getAccessor(Display.TextDisplay.class, "DATA_TEXT_ID");

    // TextDisplay Entity - 线宽
    private static final EntityDataAccessor<Integer> TEXT_DISPLAY_LINE_WIDTH_OBJECT = getAccessor(Display.TextDisplay.class, "DATA_LINE_WIDTH_ID");

    // TextDisplay Entity - 文本不透明度
    private static final EntityDataAccessor<Byte> TEXT_DISPLAY_OPACITY_OBJECT = getAccessor(Display.TextDisplay.class, "DATA_TEXT_OPACITY_ID");

    // TextDisplay Entity - 背景颜色
    private static final EntityDataAccessor<Integer> TEXT_DISPLAY_BACKGROUND_COLOR_OBJECT = getAccessor(Display.TextDisplay.class, "DATA_BACKGROUND_COLOR_ID");

    // TextDisplay Entity - 样式标志（包含对齐方式）
    private static final EntityDataAccessor<Byte> TEXT_DISPLAY_STYLE_FLAGS_OBJECT = getAccessor(Display.TextDisplay.class, "DATA_STYLE_FLAGS_ID");

    // BlockDisplay Entity - 方块状态
    private static final EntityDataAccessor<BlockState> BLOCK_DISPLAY_BLOCK_STATE_OBJECT = getAccessor(Display.BlockDisplay.class, "DATA_BLOCK_STATE_ID");

    // Interaction Entity - 宽度
    private static final EntityDataAccessor<Float> INTERACTION_WIDTH_OBJECT = getAccessor(Interaction.class, "DATA_WIDTH_ID");

    // Interaction Entity - 高度
    private static final EntityDataAccessor<Float> INTERACTION_HEIGHT_OBJECT = getAccessor(Interaction.class, "DATA_HEIGHT_ID");

    // Interaction Entity - 响应（是否触发手臂挥动动画）
    private static final EntityDataAccessor<Boolean> INTERACTION_RESPONSE_OBJECT = getAccessor(Interaction.class, "DATA_RESPONSE_ID");

    /**
     * 通过反射获取 EntityDataAccessor 字段
     * 26.1+ 中这些字段变为 private/protected，无法直接访问
     */
    @SuppressWarnings("unchecked")
    private static <T> EntityDataAccessor<T> getAccessor(Class<?> holderClass, String fieldName) {
        try {
            Field field = holderClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (EntityDataAccessor<T>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access " + holderClass.getSimpleName() + "." + fieldName, e);
        }
    }

    // 静态实例
    static final EntityMetadataType<Byte> ENTITY_PROPERTIES = new EntityMetadataType<>(ENTITY_PROPERTIES_OBJECT);
    static final EntityMetadataType<Optional<Component>> ENTITY_CUSTOM_NAME = new EntityMetadataType<>(ENTITY_CUSTOM_NAME_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_CUSTOM_NAME_VISIBLE = new EntityMetadataType<>(ENTITY_CUSTOM_NAME_VISIBLE_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_SILENT = new EntityMetadataType<>(ENTITY_SILENT_OBJECT);
    static final EntityMetadataType<Boolean> ENTITY_HAS_NO_GRAVITY = new EntityMetadataType<>(ENTITY_HAS_NO_GRAVITY_OBJECT);
    static final EntityMetadataType<Byte> ARMOR_STAND_PROPERTIES = new EntityMetadataType<>(ARMOR_STAND_PROPERTIES_OBJECT);
    static final EntityMetadataType<ItemStack> ITEM_STACK = new EntityMetadataType<>(ITEM_STACK_OBJECT);
    static final EntityMetadataType<Rotations> ARMOR_STAND_HEAD_POSE = new EntityMetadataType<>(ARMOR_STAND_HEAD_POSE_OBJECT);
    static final EntityMetadataType<Rotations> ARMOR_STAND_BODY_POSE = new EntityMetadataType<>(ARMOR_STAND_BODY_POSE_OBJECT);

    // Display Entity 静态实例
    static final EntityMetadataType<Byte> DISPLAY_BILLBOARD = new EntityMetadataType<>(DISPLAY_BILLBOARD_OBJECT);
    static final EntityMetadataType<Integer> DISPLAY_BRIGHTNESS = new EntityMetadataType<>(DISPLAY_BRIGHTNESS_OBJECT);
    static final EntityMetadataType<Vector3fc> DISPLAY_TRANSLATION = new EntityMetadataType<>(DISPLAY_TRANSLATION_OBJECT);
    static final EntityMetadataType<Vector3fc> DISPLAY_SCALE = new EntityMetadataType<>(DISPLAY_SCALE_OBJECT);
    static final EntityMetadataType<org.joml.Quaternionf> DISPLAY_LEFT_ROTATION = new EntityMetadataType<>(DISPLAY_LEFT_ROTATION_OBJECT);
    static final EntityMetadataType<org.joml.Quaternionf> DISPLAY_RIGHT_ROTATION = new EntityMetadataType<>(DISPLAY_RIGHT_ROTATION_OBJECT);
    static final EntityMetadataType<Float> DISPLAY_SHADOW_RADIUS = new EntityMetadataType<>(DISPLAY_SHADOW_RADIUS_OBJECT);
    static final EntityMetadataType<Float> DISPLAY_SHADOW_STRENGTH = new EntityMetadataType<>(DISPLAY_SHADOW_STRENGTH_OBJECT);
    static final EntityMetadataType<Integer> DISPLAY_GLOW_COLOR_OVERRIDE = new EntityMetadataType<>(DISPLAY_GLOW_COLOR_OVERRIDE_OBJECT);

    // ItemDisplay Entity 静态实例
    static final EntityMetadataType<ItemStack> ITEM_DISPLAY_ITEM_STACK = new EntityMetadataType<>(ITEM_DISPLAY_ITEM_STACK_OBJECT);

    // TextDisplay Entity 静态实例
    static final EntityMetadataType<Component> TEXT_DISPLAY_TEXT = new EntityMetadataType<>(TEXT_DISPLAY_TEXT_OBJECT);
    static final EntityMetadataType<Integer> TEXT_DISPLAY_LINE_WIDTH = new EntityMetadataType<>(TEXT_DISPLAY_LINE_WIDTH_OBJECT);
    static final EntityMetadataType<Byte> TEXT_DISPLAY_OPACITY = new EntityMetadataType<>(TEXT_DISPLAY_OPACITY_OBJECT);
    static final EntityMetadataType<Integer> TEXT_DISPLAY_BACKGROUND_COLOR = new EntityMetadataType<>(TEXT_DISPLAY_BACKGROUND_COLOR_OBJECT);
    static final EntityMetadataType<Byte> TEXT_DISPLAY_STYLE_FLAGS = new EntityMetadataType<>(TEXT_DISPLAY_STYLE_FLAGS_OBJECT);

    // BlockDisplay Entity 静态实例
    static final EntityMetadataType<BlockState> BLOCK_DISPLAY_BLOCK_STATE = new EntityMetadataType<>(BLOCK_DISPLAY_BLOCK_STATE_OBJECT);

    // Interaction Entity 静态实例
    static final EntityMetadataType<Float> INTERACTION_WIDTH = new EntityMetadataType<>(INTERACTION_WIDTH_OBJECT);
    static final EntityMetadataType<Float> INTERACTION_HEIGHT = new EntityMetadataType<>(INTERACTION_HEIGHT_OBJECT);
    static final EntityMetadataType<Boolean> INTERACTION_RESPONSE = new EntityMetadataType<>(INTERACTION_RESPONSE_OBJECT);

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
