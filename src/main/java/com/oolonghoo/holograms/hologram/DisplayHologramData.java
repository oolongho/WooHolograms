package com.oolonghoo.holograms.hologram;

import java.util.Objects;

/**
 * 显示效果数据类
 * 存储全息图的高级视觉效果设置
 * 参考 Minecraft 1.19.4+ Display Entity 的属性
 * 
 * @since 1.0.0
 */
public class DisplayHologramData {

    // 默认值常量
    /**
     * 透明背景常量
     */
    public static final int TRANSPARENT_BACKGROUND = 0x00000000;

    /**
     * 默认背景色（透明）
     */
    public static final int DEFAULT_BACKGROUND = TRANSPARENT_BACKGROUND;

    /**
     * 默认阴影半径
     */
    public static final float DEFAULT_SHADOW_RADIUS = 0.0f;

    /**
     * 默认阴影强度
     */
    public static final float DEFAULT_SHADOW_STRENGTH = 1.0f;

    /**
     * 默认行宽
     */
    public static final int DEFAULT_LINE_WIDTH = 200;

    /*
     * 字段
     */

    // 广告牌模式
    private Billboard billboard;

    // 亮度覆盖
    private Brightness brightness;

    // 阴影效果
    private float shadowRadius;
    private float shadowStrength;

    // 背景色（ARGB 格式）
    private int background;

    // 文本效果
    private boolean textShadow;
    private TextAlignment alignment;
    private int lineWidth;

    // 透视效果
    private boolean seeThrough;

    /**
     * 创建默认的显示效果数据
     */
    public DisplayHologramData() {
        this.billboard = Billboard.CENTER;
        this.brightness = null;
        this.shadowRadius = DEFAULT_SHADOW_RADIUS;
        this.shadowStrength = DEFAULT_SHADOW_STRENGTH;
        this.background = DEFAULT_BACKGROUND;
        this.textShadow = true;
        this.seeThrough = false;
        this.alignment = TextAlignment.CENTER;
        this.lineWidth = DEFAULT_LINE_WIDTH;
    }

    /**
     * 复制构造函数
     * 
     * @param other 其他显示效果数据
     */
    public DisplayHologramData(DisplayHologramData other) {
        if (other == null) {
            this.billboard = Billboard.CENTER;
            this.brightness = null;
            this.shadowRadius = DEFAULT_SHADOW_RADIUS;
            this.shadowStrength = DEFAULT_SHADOW_STRENGTH;
            this.background = DEFAULT_BACKGROUND;
            this.textShadow = true;
            this.seeThrough = false;
            this.alignment = TextAlignment.CENTER;
            this.lineWidth = DEFAULT_LINE_WIDTH;
        } else {
            this.billboard = other.billboard;
            this.brightness = other.brightness;
            this.shadowRadius = other.shadowRadius;
            this.shadowStrength = other.shadowStrength;
            this.background = other.background;
            this.textShadow = other.textShadow;
            this.seeThrough = other.seeThrough;
            this.alignment = other.alignment;
            this.lineWidth = other.lineWidth;
        }
    }

    /*
     * 静态工厂方法
     */

    /**
     * 创建默认的显示效果数据
     * 
     * @return 默认显示效果数据
     */
    public static DisplayHologramData createDefault() {
        return new DisplayHologramData();
    }

    /**
     * 创建透视模式的显示效果数据
     * 
     * @return 透视显示效果数据
     */
    public static DisplayHologramData createSeeThrough() {
        DisplayHologramData data = new DisplayHologramData();
        data.setSeeThrough(true);
        return data;
    }

    /**
     * 创建固定朝向的显示效果数据
     * 
     * @return 固定朝向显示效果数据
     */
    public static DisplayHologramData createFixed() {
        DisplayHologramData data = new DisplayHologramData();
        data.setBillboard(Billboard.FIXED_ANGLE);
        return data;
    }

    /*
     * 广告牌模式
     */

    /**
     * 获取广告牌模式
     * 
     * @return 广告牌模式
     */
    public Billboard getBillboard() {
        return billboard;
    }

    /**
     * 设置广告牌模式
     * 
     * @param billboard 广告牌模式
     */
    public void setBillboard(Billboard billboard) {
        this.billboard = billboard != null ? billboard : Billboard.CENTER;
    }

    /*
     * 亮度
     */

    /**
     * 获取亮度覆盖
     * 
     * @return 亮度覆盖
     */
    public Brightness getBrightness() {
        return brightness;
    }

    /**
     * 设置亮度覆盖
     * 
     * @param brightness 亮度覆盖
     */
    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public boolean hasBrightnessOverride() {
        return brightness != null;
    }

    /*
     * 阴影效果
     */

    /**
     * 获取阴影半径
     * 
     * @return 阴影半径
     */
    public float getShadowRadius() {
        return shadowRadius;
    }

    /**
     * 设置阴影半径
     * 
     * @param shadowRadius 阴影半径 (0.0 - 无上限)
     */
    public void setShadowRadius(float shadowRadius) {
        this.shadowRadius = Math.max(0.0f, shadowRadius);
    }

    /**
     * 获取阴影强度
     * 
     * @return 阴影强度
     */
    public float getShadowStrength() {
        return shadowStrength;
    }

    /**
     * 设置阴影强度
     * 
     * @param shadowStrength 阴影强度 (0.0 - 1.0+)
     */
    public void setShadowStrength(float shadowStrength) {
        this.shadowStrength = Math.max(0.0f, shadowStrength);
    }

    /**
     * 检查是否有阴影
     * 
     * @return 是否有阴影
     */
    public boolean hasShadow() {
        return shadowRadius > 0.0f;
    }

    /*
     * 背景色
     */

    /**
     * 获取背景色（ARGB 格式）
     * 
     * @return 背景色
     */
    public int getBackground() {
        return background;
    }

    /**
     * 设置背景色（ARGB 格式）
     * 
     * @param background 背景色
     */
    public void setBackground(int background) {
        this.background = background;
    }

    /**
     * 设置背景色（RGBA 格式）
     * 
     * @param red 红色分量 (0-255)
     * @param green 绿色分量 (0-255)
     * @param blue 蓝色分量 (0-255)
     * @param alpha 透明度 (0-255)
     */
    public void setBackground(int red, int green, int blue, int alpha) {
        this.background = (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 设置背景色（RGB 格式，不透明）
     * 
     * @param red 红色分量 (0-255)
     * @param green 绿色分量 (0-255)
     * @param blue 蓝色分量 (0-255)
     */
    public void setBackground(int red, int green, int blue) {
        setBackground(red, green, blue, 255);
    }

    /**
     * 设置透明背景
     */
    public void setTransparentBackground() {
        this.background = TRANSPARENT_BACKGROUND;
    }

    /**
     * 检查是否有背景色
     * 
     * @return 是否有背景色
     */
    public boolean hasBackground() {
        return (background >>> 24) != 0; // 检查 alpha 通道
    }

    /**
     * 获取背景色的 alpha 通道
     * 
     * @return alpha 值 (0-255)
     */
    public int getBackgroundAlpha() {
        return (background >>> 24) & 0xFF;
    }

    /**
     * 获取背景色的红色分量
     * 
     * @return 红色分量 (0-255)
     */
    public int getBackgroundRed() {
        return (background >>> 16) & 0xFF;
    }

    /**
     * 获取背景色的绿色分量
     * 
     * @return 绿色分量 (0-255)
     */
    public int getBackgroundGreen() {
        return (background >>> 8) & 0xFF;
    }

    /**
     * 获取背景色的蓝色分量
     * 
     * @return 蓝色分量 (0-255)
     */
    public int getBackgroundBlue() {
        return background & 0xFF;
    }

    /*
     * 文本效果
     */

    /**
     * 检查是否有文本阴影
     * 
     * @return 是否有文本阴影
     */
    public boolean hasTextShadow() {
        return textShadow;
    }

    /**
     * 设置文本阴影
     * 
     * @param textShadow 是否有文本阴影
     */
    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    /**
     * 获取文本对齐方式
     * 
     * @return 文本对齐方式
     */
    public TextAlignment getAlignment() {
        return alignment;
    }

    /**
     * 设置文本对齐方式
     * 
     * @param alignment 文本对齐方式
     */
    public void setAlignment(TextAlignment alignment) {
        this.alignment = alignment != null ? alignment : TextAlignment.CENTER;
    }

    /**
     * 获取行宽
     * 
     * @return 行宽
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * 设置行宽
     * 
     * @param lineWidth 行宽 (最小为 1)
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = Math.max(1, lineWidth);
    }

    /*
     * 透视效果
     */

    /**
     * 检查是否为透视模式
     * 
     * @return 是否为透视模式
     */
    public boolean isSeeThrough() {
        return seeThrough;
    }

    /**
     * 设置透视模式
     * 
     * @param seeThrough 是否为透视模式
     */
    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
    }

    /*
     * 克隆和序列化
     */

    /**
     * 克隆此对象
     * 
     * @return 克隆的对象
     */
    public DisplayHologramData clone() {
        return new DisplayHologramData(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisplayHologramData that = (DisplayHologramData) o;
        return Float.compare(that.shadowRadius, shadowRadius) == 0 &&
                Float.compare(that.shadowStrength, shadowStrength) == 0 &&
                background == that.background &&
                textShadow == that.textShadow &&
                seeThrough == that.seeThrough &&
                lineWidth == that.lineWidth &&
                billboard == that.billboard &&
                Objects.equals(brightness, that.brightness) &&
                alignment == that.alignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(billboard, brightness, shadowRadius, shadowStrength, 
                background, textShadow, seeThrough, alignment, lineWidth);
    }

    @Override
    public String toString() {
        return "DisplayHologramData{" +
                "billboard=" + billboard +
                ", brightness=" + brightness +
                ", shadowRadius=" + shadowRadius +
                ", shadowStrength=" + shadowStrength +
                ", background=#" + Integer.toHexString(background) +
                ", textShadow=" + textShadow +
                ", seeThrough=" + seeThrough +
                ", alignment=" + alignment +
                ", lineWidth=" + lineWidth +
                '}';
    }
}
