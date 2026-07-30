package com.oolongho.holograms.hologram;

/**
 * 全息图标志枚举
 * 定义全息图和行的各种行为标志
 * 参考 DecentHolograms 的 EnumFlag 实现
 * 
 */
public enum EnumFlag {

    /**
     * 禁用占位符解析
     * 设置后不会解析 PlaceholderAPI 占位符
     */
    DISABLE_PLACEHOLDERS("disable_placeholders"),

    /**
     * 禁用更新
     * 设置后不会自动更新内容
     */
    DISABLE_UPDATING("disable_updating"),

    /**
     * 禁用动画
     * 设置后不会播放动画效果
     */
    DISABLE_ANIMATIONS("disable_animations"),

    /**
     * 禁用动作
     * 设置后点击不会触发任何动作
     */
    DISABLE_ACTIONS("disable_actions");

    private final String id;

    EnumFlag(String id) {
        this.id = id;
    }

    /**
     * 获取标志 ID
     * @return 标志 ID
     */
    public String getId() {
        return id;
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
