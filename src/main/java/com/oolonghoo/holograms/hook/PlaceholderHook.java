package com.oolonghoo.holograms.hook;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI 扩展
 * 提供全息图相关占位符
 * 
 * @author oolongho
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private final WooHolograms plugin;

    public PlaceholderHook(WooHolograms plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wooholograms";
    }

    @Override
    public @NotNull String getAuthor() {
        return "oolongho";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        String[] args = params.split("_", 3);
        String action = args[0].toLowerCase();

        switch (action) {
            case "count":
                return handleCountPlaceholder(args);
            case "exists":
                return handleExistsPlaceholder(args);
            case "enabled":
                return handleEnabledPlaceholder(args);
            case "viewers":
                return handleViewersPlaceholder(args);
            case "pages":
                return handlePagesPlaceholder(args);
            case "lines":
                return handleLinesPlaceholder(args);
            case "location":
                return handleLocationPlaceholder(args);
            case "visible":
                if (player == null || !player.isOnline()) {
                    return "false";
                }
                return handleVisiblePlaceholder(player.getPlayer(), args);
            default:
                return null;
        }
    }

    /**
     * 处理 count 占位符
     * %wooholograms_count% - 获取全息图总数
     * %wooholograms_count_world_<世界名>% - 获取指定世界的全息图数量
     */
    private String handleCountPlaceholder(String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("world")) {
            if (args.length >= 3) {
                String worldName = args[2];
                long count = plugin.getHologramManager().getHolograms().stream()
                        .filter(h -> h.getLocation() != null && 
                                h.getLocation().getWorld() != null &&
                                h.getLocation().getWorld().getName().equals(worldName))
                        .count();
                return String.valueOf(count);
            }
        }
        return String.valueOf(plugin.getHologramManager().getHologramCount());
    }

    /**
     * 处理 exists 占位符
     * %wooholograms_exists_<全息图名>% - 检查全息图是否存在
     */
    private String handleExistsPlaceholder(String[] args) {
        if (args.length < 2) {
            return "false";
        }
        String hologramName = args[1];
        boolean exists = plugin.getHologramManager().containsHologram(hologramName);
        return exists ? "true" : "false";
    }

    /**
     * 处理 enabled 占位符
     * %wooholograms_enabled_<全息图名>% - 检查全息图是否启用
     */
    private String handleEnabledPlaceholder(String[] args) {
        if (args.length < 2) {
            return "false";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            return "false";
        }
        return hologram.isEnabled() ? "true" : "false";
    }

    /**
     * 处理 viewers 占位符
     * %wooholograms_viewers_<全息图名>% - 获取全息图观看者数量
     */
    private String handleViewersPlaceholder(String[] args) {
        if (args.length < 2) {
            return "0";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            return "0";
        }
        return String.valueOf(hologram.getViewers().size());
    }

    /**
     * 处理 pages 占位符
     * %wooholograms_pages_<全息图名>% - 获取全息图页数
     */
    private String handlePagesPlaceholder(String[] args) {
        if (args.length < 2) {
            return "0";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            return "0";
        }
        return String.valueOf(hologram.getPageCount());
    }

    /**
     * 处理 lines 占位符
     * %wooholograms_lines_<全息图名>% - 获取全息图总行数
     * %wooholograms_lines_<全息图名>_<页码>% - 获取指定页的行数
     */
    private String handleLinesPlaceholder(String[] args) {
        if (args.length < 2) {
            return "0";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            return "0";
        }

        if (args.length >= 3) {
            try {
                int pageIndex = Integer.parseInt(args[2]);
                if (pageIndex >= 0 && pageIndex < hologram.getPageCount()) {
                    return String.valueOf(hologram.getPage(pageIndex).size());
                }
                return "0";
            } catch (NumberFormatException e) {
                return "0";
            }
        }

        // 计算总行数
        int totalLines = 0;
        for (int i = 0; i < hologram.getPageCount(); i++) {
            totalLines += hologram.getPage(i).size();
        }
        return String.valueOf(totalLines);
    }

    /**
     * 处理 location 占位符
     * %wooholograms_location_<全息图名>% - 获取全息图位置
     * %wooholograms_location_<全息图名>_world% - 获取世界名
     * %wooholograms_location_<全息图名>_x% - 获取 X 坐标
     * %wooholograms_location_<全息图名>_y% - 获取 Y 坐标
     * %wooholograms_location_<全息图名>_z% - 获取 Z 坐标
     */
    private String handleLocationPlaceholder(String[] args) {
        if (args.length < 2) {
            return "unknown";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null || hologram.getLocation() == null) {
            return "unknown";
        }

        org.bukkit.Location loc = hologram.getLocation();
        
        if (args.length >= 3) {
            String type = args[2].toLowerCase();
            switch (type) {
                case "world":
                    return loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
                case "x":
                    return String.format("%.2f", loc.getX());
                case "y":
                    return String.format("%.2f", loc.getY());
                case "z":
                    return String.format("%.2f", loc.getZ());
                default:
                    return "unknown";
            }
        }

        // 返回完整位置
        if (loc.getWorld() == null) {
            return "unknown";
        }
        return String.format("%s, %.2f, %.2f, %.2f",
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * 处理 visible 占位符
     * %wooholograms_visible_<全息图名>% - 检查玩家是否能看到全息图
     */
    private String handleVisiblePlaceholder(Player player, String[] args) {
        if (args.length < 2) {
            return "false";
        }
        String hologramName = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            return "false";
        }
        return hologram.isVisible(player) ? "true" : "false";
    }

    /**
     * 注册占位符扩展
     * 
     * @return 是否注册成功
     */
    public boolean register() {
        return super.register();
    }
    
    /**
     * 替换字符串中的占位符
     * 
     * @param player 玩家
     * @param text 文本
     * @return 替换后的文本
     */
    public String setPlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // 使用 PlaceholderAPI 替换占位符
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }
}
