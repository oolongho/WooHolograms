package com.oolonghoo.holograms.hologram;

/**
 * 全息图标志枚举
 * 定义全息图和行的各种行为标志
 * 参考 DecentHolograms 的 EnumFlag 实现
 * 
 * @author oolongho
 */
public enum EnumFlag {

    /**
     * 禁用占位符解析
     * 设置后不会解析 PlaceholderAPI 占位符
     */
    DISABLE_PLACEHOLDERS("disable_placeholders", "禁用占位符"),

    /**
     * 禁用更新
     * 设置后不会自动更新内容
     */
    DISABLE_UPDATING("disable_updating", "禁用更新"),

    /**
     * 禁用动画
     * 设置后不会播放动画效果
     */
    DISABLE_ANIMATIONS("disable_animations", "禁用动画"),

    /**
     * 禁用动作
     * 设置后点击不会触发任何动作
     */
    DISABLE_ACTIONS("disable_actions", "禁用动作"),

    /**
     * 始终面向玩家
     * 设置后全息图会始终面向观看的玩家
     */
    ALWAYS_FACE_PLAYER("always_face_player", "始终面向玩家"),

    /**
     * 可点击
     * 设置后全息图可以被玩家点击
     */
    CLICKABLE("clickable", "可点击");

    private final String id;
    private final String description;

    EnumFlag(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * 获取标志 ID
     * @return 标志 ID
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
     * 根据 ID 获取标志
     * @param id 标志 ID
     * @return 标志，如果不存在返回 null
     */
    public static EnumFlag fromId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        for (EnumFlag flag : values()) {
            if (flag.id.equalsIgnoreCase(id) || flag.name().equalsIgnoreCase(id)) {
                return flag;
            }
        }
        return null;
    }
}
