package com.oolonghoo.holograms.action;

/**
 * 点击类型枚举
 * 定义全息图支持的点击类型
 * 
 */
public enum ClickType {

    /**
     * 左键点击
     */
    LEFT("left", "左键点击"),
    
    /**
     * 右键点击
     */
    RIGHT("right", "右键点击"),
    
    /**
     * Shift + 左键点击
     */
    SHIFT_LEFT("shift_left", "Shift + 左键点击"),
    
    /**
     * Shift + 右键点击
     */
    SHIFT_RIGHT("shift_right", "Shift + 右键点击"),
    
    /**
     * 任意点击
     */
    ANY("any", "任意点击");

    private final String id;
    private final String description;

    ClickType(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * 获取 ID
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取描述
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据 ID 获取点击类型
     * @param id ID
     * @return 点击类型，如果不存在返回 ANY
     */
    public static ClickType fromId(String id) {
        for (ClickType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return ANY;
    }

    /**
     * 检查是否匹配指定点击类型
     * @param other 其他点击类型
     * @return 是否匹配
     */
    public boolean matches(ClickType other) {
        return this == ANY || other == ANY || this == other;
    }
}
