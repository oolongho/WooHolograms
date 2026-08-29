package com.oolongho.holograms.nms.versions;

import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.TextAlignment;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体元数据构建器
 * 用于构建实体的元数据
 *
 * 
 * 
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
     * 设置实体发光效果（0x40 标志位）
     * 用于 ItemDisplay 附魔光效等场景
     *
     * @return this
     */
    public EntityMetadataBuilder withGlow() {
        mergeEntityProperties((byte) 0x40);
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
     * 设置 Interaction 实体的宽度
     *
     * @param width 宽度（最大 10.0）
     * @return this
     */
    public EntityMetadataBuilder withInteractionWidth(float width) {
        watchableObjects.add(EntityMetadataType.INTERACTION_WIDTH.construct(width));
        return this;
    }

    /**
     * 设置 Interaction 实体的高度
     *
     * @param height 高度（最大 10.0）
     * @return this
     */
    public EntityMetadataBuilder withInteractionHeight(float height) {
        watchableObjects.add(EntityMetadataType.INTERACTION_HEIGHT.construct(height));
        return this;
    }

    /**
     * 设置 Interaction 实体是否响应（触发玩家手臂挥动动画）
     *
     * @param response 是否响应
     * @return this
     */
    public EntityMetadataBuilder withInteractionResponse(boolean response) {
        watchableObjects.add(EntityMetadataType.INTERACTION_RESPONSE.construct(response));
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
     * 设置 ItemDisplay Entity 的物品
     *
     * @param itemStack 物品
     * @return this
     */
    public EntityMetadataBuilder withItemDisplayItem(org.bukkit.inventory.ItemStack itemStack) {
        watchableObjects.add(EntityMetadataType.ITEM_DISPLAY_ITEM_STACK.construct(CraftItemStack.asNMSCopy(itemStack)));
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
    public EntityMetadataBuilder withHeadRotation(float x, float y, float z) {
        watchableObjects.add(EntityMetadataType.ARMOR_STAND_HEAD_POSE.construct(new Rotations(x, y, z)));
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
    public EntityMetadataBuilder withBrightness(Brightness brightness) {
        if (brightness == null) {
            return this;
        }

        if (brightness.getBlockLight() >= 15 || brightness.getSkyLight() >= 15) {
            // 合并 invisible(0x20) 和 glow(0x40) 标志，避免与 withInvisible() 产生重复 accessor
            byte glowFlags = 0x40;
            mergeEntityProperties(glowFlags);
        }

        return this;
    }

    /**
     * 合并实体属性标志位，如果已存在 ENTITY_PROPERTIES 条目则合并，否则新增
     */
    public void mergeEntityProperties(byte flags) {
        EntityDataAccessor<Byte> targetAccessor = EntityMetadataType.ENTITY_PROPERTIES_OBJECT;
        for (int i = 0; i < watchableObjects.size(); i++) {
            SynchedEntityData.DataItem<?> item = watchableObjects.get(i);
            if (item.getAccessor().equals(targetAccessor)) {
                // 合并标志位
                @SuppressWarnings("unchecked")
                SynchedEntityData.DataItem<Byte> byteItem = (SynchedEntityData.DataItem<Byte>) item;
                byte existing = byteItem.getValue();
                watchableObjects.set(i, EntityMetadataType.ENTITY_PROPERTIES.construct((byte) (existing | flags)));
                return;
            }
        }
        // 不存在则新增
        watchableObjects.add(EntityMetadataType.ENTITY_PROPERTIES.construct(flags));
    }

    /**
     * 设置 Display Entity 的亮度覆盖
     * 使用 Display.Brightness 类设置亮度
     *
     * @param brightness 亮度对象
     * @return this
     */
    public EntityMetadataBuilder withDisplayBrightness(Brightness brightness) {
        // 始终发送亮度字段：-1 表示使用世界光照（Minecraft 默认值），
        // 确保 metadata update 时客户端能正确重置亮度，而非保留旧值
        int brightnessInt;
        if (brightness == null || brightness.isDefault()) {
            brightnessInt = -1;
        } else {
            brightnessInt = brightness.getBlockLight() << 4 | brightness.getSkyLight() << 20;
        }
        watchableObjects.add(EntityMetadataType.DISPLAY_BRIGHTNESS.construct(brightnessInt));
        return this;
    }

    /**
     * 设置 Display Entity 的 Billboard 模式
     *
     * @param billboard Billboard 模式
     * @return this
     */
    public EntityMetadataBuilder withBillboard(Billboard billboard) {
        if (billboard == null) {
            billboard = Billboard.CENTER;
        }
        byte billboardValue;
        switch (billboard) {
            case FIXED_ANGLE:
                billboardValue = 0;
                break;
            case VERTICAL:
                billboardValue = 1;
                break;
            case HORIZONTAL:
                billboardValue = 2;
                break;
            case CENTER:
            default:
                billboardValue = 3;
                break;
        }
        watchableObjects.add(EntityMetadataType.DISPLAY_BILLBOARD.construct(billboardValue));
        return this;
    }



    /**
     * 设置 TextDisplay Entity 的文本内容
     *
     * @param text 文本内容
     * @return this
     */
    public EntityMetadataBuilder withTextDisplayText(String text) {
        Component component = CraftChatMessage.fromStringOrNull(text);
        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_TEXT.construct(component != null ? component : Component.empty()));
        return this;
    }

    /**
     * 设置 TextDisplay Entity 的文本内容（多行）
     * 逐行创建 Component，用换行 Component 连接
     *
     * @param lines 文本行列表
     * @return this
     */
    public EntityMetadataBuilder withTextDisplayText(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_TEXT.construct(Component.empty()));
            return this;
        }

        if (lines.size() == 1) {
            return withTextDisplayText(lines.get(0));
        }

        // 多行：逐行创建 Component，用换行 Component 连接
        MutableComponent root = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                root.append(Component.literal("\n"));
            }
            Component lineComponent = CraftChatMessage.fromStringOrNull(lines.get(i));
            root.append(lineComponent != null ? lineComponent : Component.empty());
        }

        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_TEXT.construct(root));
        return this;
    }

    /**
     * 设置 TextDisplay Entity 的文本对齐方式
     *
     * @param alignment 文本对齐方式
     * @return this
     */
    public EntityMetadataBuilder withTextAlignment(TextAlignment alignment) {
        if (alignment == null) {
            alignment = TextAlignment.LEFT;
        }
        byte styleFlags;
        switch (alignment) {
            case LEFT:
                styleFlags = 0x08;
                break;
            case RIGHT:
                styleFlags = 0x10;
                break;
            case CENTER:
            default:
                styleFlags = 0x00;
                break;
        }
        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_STYLE_FLAGS.construct(styleFlags));
        return this;
    }

    /**
     * 设置 TextDisplay Entity 的线宽
     *
     * @param lineWidth 线宽（默认 200）
     * @return this
     */
    public EntityMetadataBuilder withTextLineWidth(int lineWidth) {
        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_LINE_WIDTH.construct(lineWidth));
        return this;
    }

    /**
     * 设置 TextDisplay Entity 的背景颜色
     *
     * @param argb ARGB 颜色值（0xAA000000 为默认透明黑色）
     * @return this
     */
    public EntityMetadataBuilder withTextBackgroundColor(int argb) {
        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_BACKGROUND_COLOR.construct(argb));
        return this;
    }

    /**
     * 设置 TextDisplay Entity 的文本不透明度
     *
     * @param opacity 不透明度（0-255，-1 表示默认）
     * @return this
     */
    public EntityMetadataBuilder withTextOpacity(byte opacity) {
        watchableObjects.add(EntityMetadataType.TEXT_DISPLAY_OPACITY.construct(opacity));
        return this;
    }

    /**
     * 设置 Display Entity 的缩放
     *
     * @param x X 轴缩放
     * @param y Y 轴缩放
     * @param z Z 轴缩放
     * @return this
     */
    public EntityMetadataBuilder withScale(float x, float y, float z) {
        watchableObjects.add(EntityMetadataType.DISPLAY_SCALE.construct(new org.joml.Vector3f(x, y, z)));
        return this;
    }

    /**
     * 设置 Display Entity 的平移
     *
     * @param x X 轴平移
     * @param y Y 轴平移
     * @param z Z 轴平移
     * @return this
     */
    public EntityMetadataBuilder withTranslation(double x, double y, double z) {
        watchableObjects.add(EntityMetadataType.DISPLAY_TRANSLATION.construct(new org.joml.Vector3f((float) x, (float) y, (float) z)));
        return this;
    }

    /**
     * 设置 Display Entity 的阴影半径
     *
     * @param radius 阴影半径
     * @return this
     */
    public EntityMetadataBuilder withShadowRadius(float radius) {
        watchableObjects.add(EntityMetadataType.DISPLAY_SHADOW_RADIUS.construct(radius));
        return this;
    }

    /**
     * 设置 Display Entity 的阴影强度
     *
     * @param strength 阴影强度
     * @return this
     */
    public EntityMetadataBuilder withShadowStrength(float strength) {
        watchableObjects.add(EntityMetadataType.DISPLAY_SHADOW_STRENGTH.construct(strength));
        return this;
    }

    /**
     * 设置 Display Entity 的发光颜色覆盖
     *
     * @param argb ARGB 颜色值
     * @return this
     */
    public EntityMetadataBuilder withGlowColor(int argb) {
        watchableObjects.add(EntityMetadataType.DISPLAY_GLOW_COLOR_OVERRIDE.construct(argb | 0xFF000000));
        withGlow(); // 自动启用发光标志，使颜色可见
        return this;
    }

    /**
     * 应用 Display Entity 高级属性（缩放、平移、阴影、发光颜色）
     * 行级别属性优先，未设置时继承全息图级别属性
     *
     * @param line 全息图行（可为 null）
     * @param hologram 全息图（可为 null）
     * @return this
     */
    public EntityMetadataBuilder withDisplayProperties(HologramLine line, Hologram hologram) {
        return withDisplayProperties(line, hologram, false);
    }

    /**
     * 应用 Display Entity 高级属性（缩放、平移、阴影、发光颜色）
     * 行级别属性优先，未设置时继承全息图级别属性
     *
     * @param line 全息图行（可为 null）
     * @param hologram 全息图（可为 null）
     * @param skipGlowColor 是否跳过发光颜色设置（当后续会由 Chroma 覆盖时使用）
     * @return this
     */
    public EntityMetadataBuilder withDisplayProperties(HologramLine line, Hologram hologram, boolean skipGlowColor) {
        // 缩放：行级别优先，否则继承全息图
        float sx = 1.0f, sy = 1.0f, sz = 1.0f;
        if (hologram != null) {
            sx = hologram.getScaleX();
            sy = hologram.getScaleY();
            sz = hologram.getScaleZ();
        }
        if (line != null) {
            if (line.getScaleX() != null) sx = line.getScaleX();
            if (line.getScaleY() != null) sy = line.getScaleY();
            if (line.getScaleZ() != null) sz = line.getScaleZ();
        }
        // 仅在非默认值时写入，减少网络包大小
        if (sx != 1.0f || sy != 1.0f || sz != 1.0f) {
            withScale(sx, sy, sz);
        }

        // 平移
        double tx = 0, ty = 0, tz = 0;
        if (hologram != null) {
            tx = hologram.getTranslationX();
            ty = hologram.getTranslationY();
            tz = hologram.getTranslationZ();
        }
        if (line != null) {
            if (line.getTranslationX() != null) tx = line.getTranslationX();
            if (line.getTranslationY() != null) ty = line.getTranslationY();
            if (line.getTranslationZ() != null) tz = line.getTranslationZ();
        }
        if (tx != 0 || ty != 0 || tz != 0) {
            withTranslation(tx, ty, tz);
        }

        // 阴影半径
        float sRadius = 0;
        if (hologram != null) sRadius = hologram.getShadowRadius();
        if (line != null && line.getShadowRadius() != null) sRadius = line.getShadowRadius();
        if (sRadius != 0) {
            withShadowRadius(sRadius);
        }

        // 阴影强度
        float sStrength = 1.0f;
        if (hologram != null) sStrength = hologram.getShadowStrength();
        if (line != null && line.getShadowStrength() != null) sStrength = line.getShadowStrength();
        if (sStrength != 1.0f) {
            withShadowStrength(sStrength);
        }

        // 发光颜色
        if (!skipGlowColor) {
            int gc = -1;
            if (hologram != null) gc = hologram.getGlowColor();
            if (line != null && line.getGlowColor() != null) gc = line.getGlowColor();
            if (gc != -1) {
                withGlowColor(gc);
            }
        }

        // 亮度覆盖：行级别优先，否则继承全息图级别
        Brightness effectiveBrightness = null;
        if (hologram != null) effectiveBrightness = hologram.getBrightness();
        if (line != null && line.getBrightness() != null) effectiveBrightness = line.getBrightness();
        if (effectiveBrightness != null && !effectiveBrightness.isDefault()) {
            withDisplayBrightness(effectiveBrightness);
        } else {
            withDisplayBrightness(null);
        }

        return this;
    }

    /**
     * 设置 Display Entity 的属性（缩放、平移、阴影、发光颜色、亮度）
     * 从行列表中聚合属性：每个属性使用第一个非 null 的行值，否则继承全息图值。
     * 用于 TEXT 行组（多行合并为单个 TextDisplay 实体）。
     *
     * @param lines 行列表（文本组中的所有行）
     * @param hologram 全息图（可为 null）
     * @param skipGlowColor 是否跳过发光颜色设置（当后续会由 Chroma 覆盖时使用）
     * @return this
     */
    public EntityMetadataBuilder withDisplayProperties(List<HologramLine> lines, Hologram hologram, boolean skipGlowColor) {
        // 缩放：每个分量使用第一个非 null 行值
        float sx = 1.0f, sy = 1.0f, sz = 1.0f;
        if (hologram != null) {
            sx = hologram.getScaleX();
            sy = hologram.getScaleY();
            sz = hologram.getScaleZ();
        }
        if (lines != null) {
            Float lx = null, ly = null, lz = null;
            for (HologramLine line : lines) {
                if (lx == null) lx = line.getScaleX();
                if (ly == null) ly = line.getScaleY();
                if (lz == null) lz = line.getScaleZ();
                if (lx != null && ly != null && lz != null) break;
            }
            if (lx != null) sx = lx;
            if (ly != null) sy = ly;
            if (lz != null) sz = lz;
        }
        if (sx != 1.0f || sy != 1.0f || sz != 1.0f) {
            withScale(sx, sy, sz);
        }

        // 平移
        double tx = 0, ty = 0, tz = 0;
        if (hologram != null) {
            tx = hologram.getTranslationX();
            ty = hologram.getTranslationY();
            tz = hologram.getTranslationZ();
        }
        if (lines != null) {
            Double dx = null, dy = null, dz = null;
            for (HologramLine line : lines) {
                if (dx == null) dx = line.getTranslationX();
                if (dy == null) dy = line.getTranslationY();
                if (dz == null) dz = line.getTranslationZ();
                if (dx != null && dy != null && dz != null) break;
            }
            if (dx != null) tx = dx;
            if (dy != null) ty = dy;
            if (dz != null) tz = dz;
        }
        if (tx != 0 || ty != 0 || tz != 0) {
            withTranslation(tx, ty, tz);
        }

        // 阴影半径
        float sRadius = 0;
        if (hologram != null) sRadius = hologram.getShadowRadius();
        if (lines != null) {
            for (HologramLine line : lines) {
                if (line.getShadowRadius() != null) { sRadius = line.getShadowRadius(); break; }
            }
        }
        if (sRadius != 0) {
            withShadowRadius(sRadius);
        }

        // 阴影强度
        float sStrength = 1.0f;
        if (hologram != null) sStrength = hologram.getShadowStrength();
        if (lines != null) {
            for (HologramLine line : lines) {
                if (line.getShadowStrength() != null) { sStrength = line.getShadowStrength(); break; }
            }
        }
        if (sStrength != 1.0f) {
            withShadowStrength(sStrength);
        }

        // 发光颜色
        if (!skipGlowColor) {
            int gc = -1;
            if (hologram != null) gc = hologram.getGlowColor();
            if (lines != null) {
                for (HologramLine line : lines) {
                    if (line.getGlowColor() != null) { gc = line.getGlowColor(); break; }
                }
            }
            if (gc != -1) {
                withGlowColor(gc);
            }
        }

        // 亮度覆盖：组内第一个非 null 行值优先，否则继承全息图级别
        Brightness effectiveBrightness = null;
        if (hologram != null) effectiveBrightness = hologram.getBrightness();
        if (lines != null) {
            for (HologramLine line : lines) {
                if (line.getBrightness() != null) { effectiveBrightness = line.getBrightness(); break; }
            }
        }
        withDisplayBrightness(effectiveBrightness);

        return this;
    }

    /**
     * 设置 Display Entity 的属性（行列表版本，不跳过发光颜色）
     */
    public EntityMetadataBuilder withDisplayProperties(List<HologramLine> lines, Hologram hologram) {
        return withDisplayProperties(lines, hologram, false);
    }

    /**
     * 设置 BlockDisplay Entity 的方块状态
     *
     * @param material Bukkit Material（必须是方块类型）
     * @return this
     */
    public EntityMetadataBuilder withBlockState(org.bukkit.Material material) {
        if (material == null || !material.isBlock()) {
            return this;
        }
        org.bukkit.block.data.BlockData blockData = material.createBlockData();
        if (blockData instanceof CraftBlockData craftBlockData) {
            watchableObjects.add(EntityMetadataType.BLOCK_DISPLAY_BLOCK_STATE.construct(craftBlockData.getState()));
        }
        return this;
    }

    /**
     * 设置 BlockDisplay Entity 的方块状态（BlockData 重载，用于 CraftEngine 自定义方块）
     *
     * @param blockData Bukkit BlockData
     * @return this
     */
    public EntityMetadataBuilder withBlockState(org.bukkit.block.data.BlockData blockData) {
        if (blockData instanceof CraftBlockData craftBlockData) {
            watchableObjects.add(EntityMetadataType.BLOCK_DISPLAY_BLOCK_STATE.construct(craftBlockData.getState()));
        }
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
