package com.oolonghoo.holograms.hologram;

import java.util.Objects;

public class Brightness {

    public static final int MAX_LIGHT = 15;
    public static final Brightness DEFAULT = new Brightness(-1, -1);

    private final int skyLight;
    private final int blockLight;

    private Brightness(int skyLight, int blockLight) {
        this.skyLight = clamp(skyLight);
        this.blockLight = clamp(blockLight);
    }

    public static Brightness of(int sky, int block) {
        return new Brightness(sky, block);
    }

    public static Brightness of(int light) {
        return new Brightness(light, light);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(MAX_LIGHT, value));
    }

    public int getSkyLight() {
        return skyLight;
    }

    public int getBlockLight() {
        return blockLight;
    }

    public boolean isDefault() {
        return skyLight < 0 || blockLight < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brightness that = (Brightness) o;
        return skyLight == that.skyLight && blockLight == that.blockLight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skyLight, blockLight);
    }

    @Override
    public String toString() {
        return "Brightness{skyLight=" + skyLight + ", blockLight=" + blockLight + "}";
    }
}
