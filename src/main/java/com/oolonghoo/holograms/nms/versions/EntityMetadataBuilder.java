package com.oolonghoo.holograms.nms.versions;

import com.google.common.base.Strings;
import com.oolonghoo.holograms.hologram.Brightness;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 实体元数据构建器
 * 用于构建实体的元数据
 *
 * @author oolongho
 * @since 1.0.0
 */
public class EntityMetadataBuilder {

    private final List<SynchedEntityData.DataItem<?>> watchableObjects;

    private EntityMetadataBuilder() {
        this.watchableObjects = new ArrayList<>();
    }

    /**
     * 转换为可监视对象列表
     *
     * @return 可监视对象列表
     */
    public List<SynchedEntityData.DataItem<?>> toWatchableObjects() {
        return watchableObjects;
    }

    /**
     * 设置实体为不可见
     *
     * @return this
     */
    public EntityMetadataBuilder withInvisible() {
        /*
         * 实体属性：
         * 0x01 - 着火
         * 0x02 - 潜行
         * 0x08 - 疾跑
         * 0x10 - 游泳
         * 0x20 - 不可见
         * 0x40 - 发光效果
         * 0x80 - 鞘翅飞行
         */
        watchableObjects.add(EntityMetadataType.ENTITY_PROPERTIES.construct((byte) 0x20));
        return this;
    }

    /**
     * 设置实体为发光
     *
     * @return this
     */
    EntityMetadataBuilder withGlowing() {
        watchableObjects.add(EntityMetadataType.ENTITY_PROPERTIES.construct((byte) 0x60)); // 不可见 + 发光
        return this;
    }

    /**
     * 设置盔甲架属性
     *
     * @param small  是否小型
     * @param marker 是否为标记（无碰撞箱）
     * @return this
     */
    public EntityMetadataBuilder withArmorStandProperties(boolean small, boolean marker) {
        /*
         * 盔甲架属性：
         * 0x01 - 小型
         * 0x02 - 未使用
         * 0x04 - 有手臂
         * 0x08 - 移除底板
         * 0x10 - 标记（零碰撞箱）
         */
        byte data = 0x08; // 始终移除底板
        if (small) {
            data |= 0x01;
        }
        if (marker) {
            data |= 0x10;
        }

        watchableObjects.add(EntityMetadataType.ARMOR_STAND_PROPERTIES.construct(data));
        return this;
    }

    /**
     * 设置自定义名称
     *
     * @param customName 自定义名称
     * @return this
     */
    EntityMetadataBuilder withCustomName(String customName) {
        Component component = CraftChatMessage.fromStringOrNull(customName);
        Optional<Component> optionalComponent = Optional.ofNullable(component);
        watchableObjects.add(EntityMetadataType.ENTITY_CUSTOM_NAME.construct(optionalComponent));
        boolean visible = !Strings.isNullOrEmpty(customName);
        watchableObjects.add(EntityMetadataType.ENTITY_CUSTOM_NAME_VISIBLE.construct(visible));
        return this;
    }

    /**
     * 设置自定义名称（带可见性控制）
     *
     * @param customName 自定义名称
     * @param visible    是否可见
     * @return this
     */
    public EntityMetadataBuilder withCustomName(String customName, boolean visible) {
        Component component = CraftChatMessage.fromStringOrNull(customName);
        Optional<Component> optionalComponent = Optional.ofNullable(component);
        watchableObjects.add(EntityMetadataType.ENTITY_CUSTOM_NAME.construct(optionalComponent));
        watchableObjects.add(EntityMetadataType.ENTITY_CUSTOM_NAME_VISIBLE.construct(visible));
        return this;
    }

    /**
     * 设置物品
     *
     * @param itemStack 物品
     * @return this
     */
    public EntityMetadataBuilder withItemStack(ItemStack itemStack) {
        watchableObjects.add(EntityMetadataType.ITEM_STACK.construct(CraftItemStack.asNMSCopy(itemStack)));
        return this;
    }

    /**
     * 设置实体为静音
     *
     * @return this
     */
    public EntityMetadataBuilder withSilent() {
        watchableObjects.add(EntityMetadataType.ENTITY_SILENT.construct(true));
        return this;
    }

    /**
     * 设置实体无重力
     *
     * @return this
     */
    public EntityMetadataBuilder withNoGravity() {
        watchableObjects.add(EntityMetadataType.ENTITY_HAS_NO_GRAVITY.construct(true));
        return this;
    }

    /**
     * 设置盔甲架头部旋转
     * 用于模拟 Billboard 模式和阴影方向
     *
     * @param x X 轴旋转（弧度）
     * @param y Y 轴旋转（弧度）
     * @param z Z 轴旋转（弧度）
     * @return this
     */
    EntityMetadataBuilder withHeadRotation(float x, float y, float z) {
        watchableObjects.add(EntityMetadataType.ARMOR_STAND_HEAD_POSE.construct(new Rotations(x, y, z)));
        return this;
    }

    /**
     * 设置盔甲架身体旋转
     *
     * @param x X 轴旋转（弧度）
     * @param y Y 轴旋转（弧度）
     * @param z Z 轴旋转（弧度）
     * @return this
     */
    EntityMetadataBuilder withBodyRotation(float x, float y, float z) {
        watchableObjects.add(EntityMetadataType.ARMOR_STAND_BODY_POSE.construct(new Rotations(x, y, z)));
        return this;
    }

    /**
     * 设置阴影效果
     * 注意：盔甲架本身不支持阴影半径，此方法用于未来扩展
     * 当前版本通过调整实体位置来模拟阴影效果
     *
     * @param shadowRadius   阴影半径
     * @param shadowStrength 阴影强度
     * @return this
     */
    EntityMetadataBuilder withShadow(float shadowRadius, float shadowStrength) {
        // 盔甲架不支持直接设置阴影半径和强度
        // 在 Display Entity 支持后可以添加此功能
        // 当前版本保留此方法用于未来扩展
        return this;
    }

    /**
     * 设置亮度覆盖
     * 注意：盔甲架本身不支持亮度覆盖，此方法用于未来扩展
     * 当前版本通过发光效果模拟亮度
     *
     * @param brightness 亮度对象
     * @return this
     */
    EntityMetadataBuilder withBrightness(Brightness brightness) {
        if (brightness == null || brightness.isDefault()) {
            return this;
        }

        // 如果亮度较高，可以添加发光效果来模拟
        if (brightness.getBlockLight() >= 15 || brightness.getSkyLight() >= 15) {
            // 设置发光效果
            watchableObjects.add(EntityMetadataType.ENTITY_PROPERTIES.construct((byte) 0x60));
        }

        return this;
    }

    /**
     * 设置透视模式
     * 通过调整实体属性实现透视效果
     *
     * @param seeThrough 是否透视
     * @return this
     */
    EntityMetadataBuilder withSeeThrough(boolean seeThrough) {
        // 透视效果需要通过特殊的渲染方式实现
        // 当前版本保留此方法用于未来扩展
        return this;
    }

    /**
     * 创建一个新的构建器
     *
     * @return 新的构建器实例
     */
    public static EntityMetadataBuilder create() {
        return new EntityMetadataBuilder();
    }
}
