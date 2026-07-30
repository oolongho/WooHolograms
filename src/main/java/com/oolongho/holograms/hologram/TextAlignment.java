package com.oolongho.holograms.hologram;

public enum TextAlignment {

    LEFT("left", "gui.alignment.name-left"),
    CENTER("center", "gui.alignment.name-center"),
    RIGHT("right", "gui.alignment.name-right");

    private final String id;
    private final String displayNameKey;

    TextAlignment(String id, String displayNameKey) {
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

    public static TextAlignment fromId(String id) {
        if (id == null || id.isEmpty()) {
            return LEFT;
        }
        for (TextAlignment alignment : values()) {
            if (alignment.id.equalsIgnoreCase(id)) {
                return alignment;
            }
        }
        return LEFT;
    }
}
