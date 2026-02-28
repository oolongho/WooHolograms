package com.oolonghoo.holograms.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * 位置工具类
 * 处理位置的序列化和反序列化
 * 
 * @author oolongho
 */
public class LocationUtil {

    /**
     * 将位置序列化为字符串
     * 格式: world,x,y,z,yaw,pitch
     * 
     * @param location 位置
     * @return 序列化字符串
     */
    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * 将字符串反序列化为位置
     * 
     * @param serialized 序列化字符串
     * @return 位置，如果解析失败返回 null
     */
    public static Location deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        
        String[] parts = serialized.split(",");
        if (parts.length < 4) {
            return null;
        }
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将位置序列化为简洁字符串（不含 yaw 和 pitch）
     * 格式: world,x,y,z
     * 
     * @param location 位置
     * @return 序列化字符串
     */
    public static String serializeSimple(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        
        return String.format("%s,%.2f,%.2f,%.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }

    /**
     * 将简洁字符串反序列化为位置
     * 
     * @param serialized 序列化字符串
     * @return 位置，如果解析失败返回 null
     */
    public static Location deserializeSimple(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        
        String[] parts = serialized.split(",");
        if (parts.length < 4) {
            return null;
        }
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 检查两个位置是否在同一方块
     * 
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 是否在同一方块
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * 获取位置的方块中心位置
     * 
     * @param location 位置
     * @return 方块中心位置
     */
    public static Location getBlockCenter(Location location) {
        if (location == null) {
            return null;
        }
        
        return new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.5,
                location.getBlockZ() + 0.5,
                location.getYaw(),
                location.getPitch()
        );
    }

    /**
     * 格式化位置为可读字符串
     * 
     * @param location 位置
     * @return 可读字符串
     */
    public static String format(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Unknown";
        }
        
        return String.format("%s (%.1f, %.1f, %.1f)",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }

    /**
     * 将位置转换为字符串（别名方法）
     * 
     * @param location 位置
     * @return 序列化字符串
     */
    public static String toString(Location location) {
        return serialize(location);
    }

    /**
     * 从字符串解析位置（别名方法）
     * 
     * @param string 字符串
     * @return 位置
     */
    public static Location fromString(String string) {
        return deserialize(string);
    }

    /**
     * 将字符串解析为位置
     * 支持多种格式：
     * - world,x,y,z
     * - world:x:y:z
     * - world,x,y,z,yaw,pitch
     * 
     * @param string 字符串
     * @return 位置，如果解析失败返回 null
     */
    public static Location asLocation(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        // 尝试使用冒号分隔
        String[] parts;
        if (string.contains(":")) {
            parts = string.split(":");
        } else if (string.contains(",")) {
            parts = string.split(",");
        } else {
            return null;
        }

        if (parts.length < 4) {
            return null;
        }

        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                return null;
            }

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
