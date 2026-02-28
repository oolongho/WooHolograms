package com.oolonghoo.holograms.hologram;

/**
 * 广告牌模式枚举
 * 定义全息图的旋转锁定方式
 * 参考 Minecraft Display Entity 的 Billboard 模式
 * 
 * @author oolongho
 * @since 1.0.0
 */
public enum Billboard {

    /**
     * 固定模式
     * 全息图不随玩家视角旋转
     */
    FIXED("fixed", "固定"),

    /**
     * 垂直模式
     * 全息图仅绕垂直轴（Y轴）旋转，面向玩家
     */
    VERTICAL("vertical", "垂直"),

    /**
     * 水平模式
     * 全息图仅绕水平轴旋转
     */
    HORIZONTAL("horizontal", "水平"),

    /**
     * 居中模式
     * 全息图完全面向玩家，可绕所有轴旋转
     */
    CENTER("center", "居中");

    private final String id;
    private final String displayName;

    Billboard(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * 获取模式 ID
     * 
     * @return 模式 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据 ID 获取广告牌模式
     * 
     * @param id 模式 ID
     * @return 广告牌模式，如果不存在返回 FIXED
     */
    public static Billboard fromId(String id) {
        if (id == null || id.isEmpty()) {
            return FIXED;
        }

        for (Billboard mode : values()) {
            if (mode.id.equalsIgnoreCase(id)) {
                return mode;
            }
        }
        return FIXED;
    }
}
