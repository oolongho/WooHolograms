package com.oolonghoo.holograms.hologram;

public enum Billboard {

    CENTER("center", "居中"),
    FIXED("fixed", "固定"),
    VERTICAL("vertical", "垂直"),
    HORIZONTAL("horizontal", "水平");

    private final String id;
    private final String displayName;

    Billboard(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
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
        return CENTER;
    }
}
