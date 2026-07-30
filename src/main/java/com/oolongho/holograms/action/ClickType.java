package com.oolongho.holograms.action;

/**
 * 点击类型枚举
 * 定义全息图支持的点击类型
 * 
 */
public enum ClickType {

    /**
     * 左键点击
     */
    LEFT("left", "click_types.left"),

    /**
     * 右键点击
     */
    RIGHT("right", "click_types.right"),

    /**
     * Shift + 左键点击
     */
    SHIFT_LEFT("shift_left", "click_types.shift_left"),

    /**
     * Shift + 右键点击
     */
    SHIFT_RIGHT("shift_right", "click_types.shift_right"),

    /**
     * 任意点击
     */
    ANY("any", "click_types.any");

    private final String id;
    private final String descriptionKey;

    ClickType(String id, String descriptionKey) {
        this.id = id;
        this.descriptionKey = descriptionKey;
    }

    /**
     * 获取 ID
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取描述的语言键（调用方通过 {@code plugin.getMessages().getRaw(key)} 解析）
     */
    public String getDescriptionKey() {
        return descriptionKey;
    }

    /**
     * 根据 ID 获取点击类型
     * @param id ID
     * @return 点击类型，如果不存在返回 ANY
     */
    public static ClickType fromId(String id) {
        if (id == null) {
            return ANY;
        }
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
