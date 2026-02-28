package com.oolonghoo.holograms.nms.util;

import org.bukkit.Location;

/**
 * 表示一个带有 yaw 和 pitch 的 3D 位置。
 * 此位置不特定于任何世界。
 *
 * <p>此实现是不可变的。</p>
 *
 * @author oolongho
 * @since 1.0.0
 */
public class DecentPosition {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public DecentPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public DecentPosition(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    /**
     * 获取 X 坐标
     * @return X 坐标
     */
    public double getX() {
        return x;
    }

    /**
     * 获取 Y 坐标
     * @return Y 坐标
     */
    public double getY() {
        return y;
    }

    /**
     * 获取 Z 坐标
     * @return Z 坐标
     */
    public double getZ() {
        return z;
    }

    /**
     * 获取 yaw 角度
     * @return yaw 角度
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * 获取 pitch 角度
     * @return pitch 角度
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * 从 Y 坐标中减去指定值
     *
     * @param y 要减去的值
     * @return 一个新的 {@link DecentPosition}，带有减去后的 Y 位置
     */
    public DecentPosition subtractY(double y) {
        if (y == 0) {
            return this;
        }
        return new DecentPosition(this.x, this.y - y, this.z, yaw, pitch);
    }

    /**
     * 向 Y 坐标添加指定值
     *
     * @param y 要添加的值
     * @return 一个新的 {@link DecentPosition}，带有添加后的 Y 位置
     */
    public DecentPosition addY(double y) {
        if (y == 0) {
            return this;
        }
        return new DecentPosition(this.x, this.y + y, this.z, yaw, pitch);
    }

    /**
     * 从 Bukkit Location 创建 {@link DecentPosition}
     *
     * @param location Bukkit Location
     * @return 新的 {@link DecentPosition}
     */
    public static DecentPosition fromBukkitLocation(Location location) {
        return new DecentPosition(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }
    
    /**
     * 从 Bukkit Location 创建 {@link DecentPosition}（别名方法）
     *
     * @param location Bukkit Location
     * @return 新的 {@link DecentPosition}
     */
    public static DecentPosition fromLocation(Location location) {
        return fromBukkitLocation(location);
    }

    /**
     * 转换为 Bukkit Location
     *
     * @param worldName 世界名称
     * @return Bukkit Location
     */
    public Location toBukkitLocation(String worldName) {
        return new Location(
                org.bukkit.Bukkit.getWorld(worldName),
                x, y, z, yaw, pitch
        );
    }

    @Override
    public String toString() {
        return "DecentPosition{" +
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
        DecentPosition that = (DecentPosition) o;
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
