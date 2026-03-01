package com.oolonghoo.holograms.hologram;

public enum Billboard {

    FIXED_ANGLE("fixed_angle", "固定角度"),
    HORIZONTAL("horizontal", "水平跟随"),
    VERTICAL("vertical", "垂直跟随"),
    CENTER("all", "完全跟随");

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
        if (id.equalsIgnoreCase("fixed")) {
            return FIXED_ANGLE;
        }
        return CENTER;
    }
}
