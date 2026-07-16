package com.oolongho.holograms.nms.util;

import org.bukkit.Location;

/**
 * 表示一个带有 yaw 和 pitch 的 3D 位置。
 * 此位置不特定于任何世界。
 *
 * <p>此实现是不可变的。</p>
 */
public class HologramPosition {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public HologramPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public HologramPosition(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public HologramPosition subtractY(double y) {
        if (y == 0) {
            return this;
        }
        return new HologramPosition(this.x, this.y - y, this.z, yaw, pitch);
    }

    public HologramPosition addY(double y) {
        if (y == 0) {
            return this;
        }
        return new HologramPosition(this.x, this.y + y, this.z, yaw, pitch);
    }

    public static HologramPosition fromBukkitLocation(Location location) {
        return new HologramPosition(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public static HologramPosition fromLocation(Location location) {
        return fromBukkitLocation(location);
    }

    public Location toBukkitLocation(String worldName) {
        return new Location(
                org.bukkit.Bukkit.getWorld(worldName),
                x, y, z, yaw, pitch
        );
    }

    @Override
    public String toString() {
        return "HologramPosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HologramPosition that = (HologramPosition) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Float.compare(that.yaw, yaw) == 0 &&
                Float.compare(that.pitch, pitch) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
        result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
        return result;
    }
}
