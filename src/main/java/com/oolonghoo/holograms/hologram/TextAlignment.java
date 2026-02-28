package com.oolonghoo.holograms.hologram;

/**
 * 文本对齐枚举
 * 定义全息图文本的对齐方式
 * 
 * @author oolongho
 * @since 1.0.0
 */
public enum TextAlignment {

    /**
     * 左对齐
     * 文本从左边缘开始
     */
    LEFT("left", "左对齐"),

    /**
     * 居中对齐
     * 文本居中显示
     */
    CENTER("center", "居中"),

    /**
     * 右对齐
     * 文本从右边缘开始
     */
    RIGHT("right", "右对齐");

    private final String id;
    private final String displayName;

    TextAlignment(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * 获取对齐方式 ID
     * 
     * @return 对齐方式 ID
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
     * 根据对齐方式获取 X 轴偏移
     * 用于文本显示实体的位置计算
     * 
     * @param textWidth 文本宽度
     * @return X 轴偏移量
     */
    public double getOffsetX(double textWidth) {
        switch (this) {
            case LEFT:
                return 0;
            case CENTER:
                return -textWidth / 2;
            case RIGHT:
                return -textWidth;
            default:
                return 0;
        }
    }

    /**
     * 根据 ID 获取对齐方式
     * 
     * @param id 对齐方式 ID
     * @return 对齐方式，如果不存在返回 CENTER
     */
    public static TextAlignment fromId(String id) {
        if (id == null || id.isEmpty()) {
            return CENTER;
        }

        for (TextAlignment alignment : values()) {
            if (alignment.id.equalsIgnoreCase(id)) {
                return alignment;
            }
        }
        return CENTER;
    }
}
