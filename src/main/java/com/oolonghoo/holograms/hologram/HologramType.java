package com.oolonghoo.holograms.hologram;

/**
 * 全息图行类型枚举
 * 定义不同类型的全息图行内容
 * 参考 DecentHolograms 的 HologramLineType 实现
 * 
 * @author oolongho
 */
public enum HologramType {

    /**
     * 文本类型
     * 普通文本显示，支持颜色代码和占位符
     */
    TEXT("TEXT", "文本", 0.25, -0.5),

    /**
     * 物品图标类型
     * 显示为掉落物形式的物品图标
     */
    ICON("ICON", "物品图标", 0.5, -0.55),

    /**
     * 头颅类型
     * 显示为正常大小的玩家头颅
     */
    HEAD("HEAD", "头颅", 0.6, -2.0),

    /**
     * 小头颅类型
     * 显示为小号玩家头颅
     */
    SMALLHEAD("SMALLHEAD", "小头颅", 0.4, -1.1875),

    UNKNOWN("UNKNOWN", "未知", 0.25, 0);

    private final String id;
    private final String displayName;
    private final double defaultHeight;
    private final double offsetY;

    HologramType(String id, String displayName, double defaultHeight, double offsetY) {
        this.id = id;
        this.displayName = displayName;
        this.defaultHeight = defaultHeight;
        this.offsetY = offsetY;
    }

    /**
     * 获取类型 ID
     * @return 类型 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取默认高度
     * @return 默认高度
     */
    public double getDefaultHeight() {
        return defaultHeight;
    }

    /**
     * 获取 Y 轴偏移
     * @return Y 轴偏移
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * 根据内容解析类型
     * @param content 内容字符串
     * @return 解析出的类型
     */
    public static HologramType parseFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return TEXT;
        }

        String upperContent = content.toUpperCase();

        if (upperContent.startsWith("#ICON:")) {
            return ICON;
        } else if (upperContent.startsWith("#SMALLHEAD:")) {
            return SMALLHEAD;
        } else if (upperContent.startsWith("#HEAD:")) {
            return HEAD;
        }

        return TEXT;
    }

    /**
     * 根据 ID 获取类型
     * @param id 类型 ID
     * @return 类型，如果不存在返回 UNKNOWN
     */
    public static HologramType fromId(String id) {
        if (id == null || id.isEmpty()) {
            return UNKNOWN;
        }

        for (HologramType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * 获取内容前缀
     * @return 内容前缀，如果不是特殊类型则返回空字符串
     */
    public String getContentPrefix() {
        switch (this) {
            case ICON:
                return "#ICON:";
            case HEAD:
                return "#HEAD:";
            case SMALLHEAD:
                return "#SMALLHEAD:";
            default:
                return "";
        }
    }

    /**
     * 检查是否为物品类型
     * @return 是否为物品类型
     */
    public boolean isItemType() {
        return this == ICON || this == HEAD || this == SMALLHEAD;
    }

    public boolean isTextType() {
        return this == TEXT;
    }
}
