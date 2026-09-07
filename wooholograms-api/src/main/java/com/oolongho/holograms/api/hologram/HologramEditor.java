package com.oolongho.holograms.api.hologram;

import org.jetbrains.annotations.Nullable;

/**
 * 全息图批量编辑器
 *
 * <p>将多个属性修改一次性提交，避免逐个 setter 触发多次刷新。
 * 通过 {@code hologram.edit()} 获取，调用 {@link #apply()} 提交：</p>
 * <pre>{@code
 * hologram.edit()
 *         .billboard(Billboard.CENTER)
 *         .lineHeight(0.3)
 *         .backgroundAlpha(64)
 *         .apply();
 * }</pre>
 *
 * <p>只有调用过 {@link #apply()} 的属性才会生效；未设置的属性保持原值。</p>
 */
public interface HologramEditor {

    /** 设置整体朝向（yaw，度） */
    HologramEditor facing(float facing);

    /** 设置垂直倾斜角度（null 表示清除覆盖） */
    HologramEditor pitch(@Nullable Float pitch);

    /** 设置 Billboard 模式 */
    HologramEditor billboard(Billboard billboard);

    /** 设置文本对齐方式 */
    HologramEditor alignment(TextAlignment alignment);

    /** 设置双面渲染 */
    HologramEditor doubleSided(boolean doubleSided);

    /** 设置背景透明度（0~255） */
    HologramEditor backgroundAlpha(int backgroundAlpha);

    /** 设置背景颜色（RGB） */
    HologramEditor backgroundColor(int backgroundColor);

    /** 设置文本自动换行宽度（像素） */
    HologramEditor lineWidth(int lineWidth);

    /** 设置行高 */
    HologramEditor lineHeight(double lineHeight);

    /** 设置三轴缩放 */
    HologramEditor scale(float x, float y, float z);

    /** 设置三轴平移 */
    HologramEditor translation(double x, double y, double z);

    /** 设置阴影半径 */
    HologramEditor shadowRadius(float shadowRadius);

    /** 设置阴影强度 */
    HologramEditor shadowStrength(float shadowStrength);

    /** 设置发光颜色覆盖（-1 表示不覆盖） */
    HologramEditor glowColor(int glowColor);

    /** 设置亮度覆盖（null 表示继承环境光照） */
    HologramEditor brightness(@Nullable Brightness brightness);

    /** 设置彩虹背景色 */
    HologramEditor chromaBackground(boolean chromaBackground);

    /** 设置彩虹发光色 */
    HologramEditor chromaGlow(boolean chromaGlow);

    /** 设置显示范围（方块） */
    HologramEditor displayRange(double displayRange);

    /** 设置更新范围（方块） */
    HologramEditor updateRange(double updateRange);

    /** 设置自动刷新间隔（tick，0 = 禁用） */
    HologramEditor updateInterval(int updateInterval);

    /** 设置查看权限（null 或空表示无限制） */
    HologramEditor permission(@Nullable String permission);

    /** 追加标志 */
    HologramEditor addFlags(EnumFlag... flags);

    /**
     * 提交全部修改（一次性应用到全息图并刷新观看者）。
     * 提交后编辑器不可复用
     */
    void apply();
}
