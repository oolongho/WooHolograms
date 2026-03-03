package com.oolonghoo.holograms.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量替换工具类
 * 支持在行内容中使用变量
 *
 * @author oolongho
 */
public class PlaceholderUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}|%([^%]+)%");
    
    private static final Map<String, Function<Player, String>> BUILTIN_PLACEHOLDERS = new HashMap<>();
    
    static {
        BUILTIN_PLACEHOLDERS.put("player", Player::getName);
        BUILTIN_PLACEHOLDERS.put("player_name", Player::getName);
        BUILTIN_PLACEHOLDERS.put("player_uuid", p -> p.getUniqueId().toString());
        BUILTIN_PLACEHOLDERS.put("player_displayname", p -> p.getDisplayName());
        BUILTIN_PLACEHOLDERS.put("player_x", p -> String.valueOf(p.getLocation().getBlockX()));
        BUILTIN_PLACEHOLDERS.put("player_y", p -> String.valueOf(p.getLocation().getBlockY()));
        BUILTIN_PLACEHOLDERS.put("player_z", p -> String.valueOf(p.getLocation().getBlockZ()));
        BUILTIN_PLACEHOLDERS.put("player_world", p -> p.getWorld().getName());
        BUILTIN_PLACEHOLDERS.put("player_health", p -> String.valueOf((int) p.getHealth()));
        BUILTIN_PLACEHOLDERS.put("player_max_health", p -> String.valueOf((int) p.getMaxHealth()));
        BUILTIN_PLACEHOLDERS.put("player_level", p -> String.valueOf(p.getLevel()));
        BUILTIN_PLACEHOLDERS.put("player_exp", p -> String.valueOf((int) (p.getExp() * 100)));
        BUILTIN_PLACEHOLDERS.put("player_food", p -> String.valueOf(p.getFoodLevel()));
        BUILTIN_PLACEHOLDERS.put("player_gamemode", p -> p.getGameMode().name());
    }
    
    /**
     * 替换文本中的变量
     *
     * @param text   原始文本
     * @param player 玩家（用于获取玩家相关变量）
     * @return 替换后的文本
     */
    public static String replace(String text, Player player) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String replacement = resolvePlaceholder(placeholder.toLowerCase(), player);
            matcher.appendReplacement(result, replacement != null ? replacement : matcher.group());
        }
        
        matcher.appendTail(result);
        
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result.toString());
        }
        
        return result.toString();
    }
    
    /**
     * 解析单个变量
     *
     * @param placeholder 变量名（不含括号）
     * @param player      玩家
     * @return 替换值，如果无法解析则返回 null
     */
    private static String resolvePlaceholder(String placeholder, Player player) {
        Function<Player, String> resolver = BUILTIN_PLACEHOLDERS.get(placeholder);
        if (resolver != null && player != null) {
            return resolver.apply(player);
        }
        return null;
    }
    
    /**
     * 检查文本是否包含变量
     *
     * @param text 文本
     * @return 是否包含变量
     */
    public static boolean containsPlaceholder(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }
    
    /**
     * 获取所有内置变量列表（用于 TAB 补全）
     *
     * @return 变量列表
     */
    public static Map<String, String> getBuiltinPlaceholders() {
        Map<String, String> result = new HashMap<>();
        result.put("{player}", "玩家名称");
        result.put("{player_name}", "玩家名称");
        result.put("{player_uuid}", "玩家 UUID");
        result.put("{player_displayname}", "玩家显示名称");
        result.put("{player_x}", "玩家 X 坐标");
        result.put("{player_y}", "玩家 Y 坐标");
        result.put("{player_z}", "玩家 Z 坐标");
        result.put("{player_world}", "玩家所在世界");
        result.put("{player_health}", "玩家生命值");
        result.put("{player_max_health}", "玩家最大生命值");
        result.put("{player_level}", "玩家等级");
        result.put("{player_exp}", "玩家经验值百分比");
        result.put("{player_food}", "玩家饥饿值");
        result.put("{player_gamemode}", "玩家游戏模式");
        return result;
    }
}
