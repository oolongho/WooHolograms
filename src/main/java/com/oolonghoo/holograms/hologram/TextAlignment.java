package com.oolonghoo.holograms.hologram;

public enum TextAlignment {

    LEFT("left", "左对齐"),
    CENTER("center", "居中"),
    RIGHT("right", "右对齐");

    private final String id;
    private final String displayName;

    TextAlignment(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
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
