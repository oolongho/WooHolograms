package com.oolongho.holograms.hologram;

public enum Billboard {

    FIXED_ANGLE("fixed_angle", "gui.billboard.name-fixed"),
    HORIZONTAL("horizontal", "gui.billboard.name-horizontal"),
    VERTICAL("vertical", "gui.billboard.name-vertical"),
    CENTER("all", "gui.billboard.name-center");

    private final String id;
    private final String displayNameKey;

    Billboard(String id, String displayNameKey) {
        this.id = id;
        this.displayNameKey = displayNameKey;
    }

    public String getId() {
        return id;
    }

    /**
     * 获取显示名称的语言键（纯文本，调用方通过 {@code plugin.getMessages().getRaw(key)} 解析）
     */
    public String getDisplayNameKey() {
        return displayNameKey;
    }

    public static Billboard fromId(String id) {
        if (id == null || id.isEmpty()) {
            return CENTER;
        }
        for (Billboard mode : values()) {
            if (mode.id.equalsIgnoreCase(id)) {
                return mode;
            }
        }
        if (id.equalsIgnoreCase("fixed")) {
            return FIXED_ANGLE;
        }
        return CENTER;
    }
}
