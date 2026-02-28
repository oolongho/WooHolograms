package com.oolonghoo.holograms.hologram;

import java.util.Objects;

/**
 * 亮度类
 * 定义全息图的亮度覆盖效果
 * 参考 Minecraft Display Entity 的亮度覆盖
 * 
 * @author oolongho
 * @since 1.0.0
 */
public class Brightness {

    /**
     * 默认亮度（不覆盖）
     */
    public static final Brightness DEFAULT = new Brightness(-1, -1);

    /**
     * 最大亮度
     */
    public static final Brightness MAX = new Brightness(15, 15);

    /**
     * 最小亮度
     */
    public static final Brightness MIN = new Brightness(0, 0);

    /**
     * 最大方块光亮度
     */
    public static final int MAX_BLOCK_LIGHT = 15;

    /**
     * 最大天空光亮度
     */
    public static final int MAX_SKY_LIGHT = 15;

    private final int blockLight;
    private final int skyLight;

    /**
     * 创建亮度对象
     * 
     * @param blockLight 方块光亮度 (0-15, -1 表示不覆盖)
     * @param skyLight 天空光亮度 (0-15, -1 表示不覆盖)
     */
    public Brightness(int blockLight, int skyLight) {
        this.blockLight = clampLight(blockLight);
        this.skyLight = clampLight(skyLight);
    }

    /**
     * 创建亮度对象（使用相同的方块光和天空光）
     * 
     * @param light 亮度值 (0-15)
     * @return 亮度对象
     */
    public static Brightness of(int light) {
        return new Brightness(light, light);
    }

    /**
     * 创建亮度对象
     * 
     * @param blockLight 方块光亮度
     * @param skyLight 天空光亮度
     * @return 亮度对象
     */
    public static Brightness of(int blockLight, int skyLight) {
        return new Brightness(blockLight, skyLight);
    }

    /**
     * 限制亮度值在有效范围内
     * 
     * @param value 原始值
     * @return 限制后的值
     */
    private static int clampLight(int value) {
        if (value < -1) {
            return -1;
        }
        return Math.min(value, MAX_BLOCK_LIGHT);
    }

    /**
     * 获取方块光亮度
     * 
     * @return 方块光亮度 (0-15, -1 表示不覆盖)
     */
    public int getBlockLight() {
        return blockLight;
    }

    /**
     * 获取天空光亮度
     * 
     * @return 天空光亮度 (0-15, -1 表示不覆盖)
     */
    public int getSkyLight() {
        return skyLight;
    }

    /**
     * 检查是否使用默认亮度（不覆盖）
     * 
     * @return 是否使用默认亮度
     */
    public boolean isDefault() {
        return blockLight < 0 && skyLight < 0;
    }

    /**
     * 检查是否有有效的方块光覆盖
     * 
     * @return 是否有有效的方块光覆盖
     */
    public boolean hasBlockLightOverride() {
        return blockLight >= 0;
    }

    /**
     * 检查是否有有效的天空光覆盖
     * 
     * @return 是否有有效的天空光覆盖
     */
    public boolean hasSkyLightOverride() {
        return skyLight >= 0;
    }

    /**
     * 获取组合亮度值
     * 用于 Minecraft 的亮度计算
     * 
     * @return 组合亮度值
     */
    public int getCombinedLight() {
        if (isDefault()) {
            return -1;
        }
        return (skyLight << 20) | (blockLight << 4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brightness brightness = (Brightness) o;
        return blockLight == brightness.blockLight && skyLight == brightness.skyLight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockLight, skyLight);
    }

    @Override
    public String toString() {
        return "Brightness{" +
                "blockLight=" + blockLight +
                ", skyLight=" + skyLight +
                '}';
    }
}
