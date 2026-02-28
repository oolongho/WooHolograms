package com.oolonghoo.holograms.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 颜色工具类
 * 处理颜色代码转换，支持传统颜色代码、十六进制颜色和 MiniMessage 格式
 * 
 * @author oolongho
 * @since 1.0.0
 */
public class ColorUtil {

    // MiniMessage 实例，支持所有标准标签
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // 支持渐变的 MiniMessage 实例
    private static final MiniMessage MINI_MESSAGE_WITH_GRADIENT = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.rainbow())
                    .resolver(StandardTags.reset())
                    .resolver(StandardTags.newline())
                    .resolver(StandardTags.translatable())
                    .resolver(StandardTags.keybind())
                    .resolver(StandardTags.insertion())
                    .resolver(StandardTags.clickEvent())
                    .resolver(StandardTags.hoverEvent())
                    .resolver(StandardTags.font())
                    .build())
            .build();

    // Legacy 序列化器
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private static final LegacyComponentSerializer SECTION_SERIALIZER =
            LegacyComponentSerializer.legacySection();

    // 十六进制颜色模式：&#RRGGBB
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    // 传统颜色代码模式
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("[&§]([0-9a-fA-Fk-oK-OrR])");

    /**
     * 将颜色代码转换为实际颜色
     * 支持 & 和 § 颜色代码、十六进制颜色和 MiniMessage 格式
     * 
     * @param text 原始文本
     * @return 转换后的文本
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 检查是否包含 MiniMessage 标签
        if (containsMiniMessageTags(text)) {
            return processMiniMessage(text);
        }

        // 处理传统颜色代码
        text = ChatColor.translateAlternateColorCodes('&', text);

        // 处理十六进制颜色
        text = translateHexColors(text);

        return text;
    }

    /**
     * 检查文本是否包含 MiniMessage 标签
     * 
     * @param text 文本
     * @return 是否包含 MiniMessage 标签
     */
    private static boolean containsMiniMessageTags(String text) {
        if (text == null) {
            return false;
        }

        // 检查常见的 MiniMessage 标签
        return text.contains("<") && text.contains(">") && (
                text.contains("<gradient:") ||
                text.contains("<rainbow") ||
                text.contains("<color:") ||
                text.contains("<#") ||
                text.contains("<click:") ||
                text.contains("<hover:") ||
                text.contains("<bold>") ||
                text.contains("<italic>") ||
                text.contains("<underlined>") ||
                text.contains("<strikethrough>") ||
                text.contains("<obfuscated>")
        );
    }

    /**
     * 处理 MiniMessage 格式文本
     * 
     * @param text 原始文本
     * @return 转换后的文本
     */
    private static String processMiniMessage(String text) {
        try {
            // 先处理传统颜色代码
            text = ChatColor.translateAlternateColorCodes('&', text);

            // 解析 MiniMessage 为 Component
            Component component = MINI_MESSAGE_WITH_GRADIENT.deserialize(text);

            // 序列化为 Legacy 格式
            return SECTION_SERIALIZER.serialize(component);
        } catch (Exception e) {
            // 如果 MiniMessage 解析失败，回退到传统处理
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }

    /**
     * 转换十六进制颜色代码
     * 格式: &#RRGGBB
     * 
     * @param text 原始文本
     * @return 转换后的文本
     */
    private static String translateHexColors(String text) {
        if (text == null) {
            return "";
        }

        Matcher matcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            // 转换为 MiniMessage 格式
            matcher.appendReplacement(result, "<color:#" + hex + ">");
        }
        matcher.appendTail(result);

        String processedText = result.toString();

        // 如果包含转换后的 MiniMessage 颜色标签，需要解析
        if (processedText.contains("<color:#")) {
            try {
                Component component = MINI_MESSAGE.deserialize(processedText);
                return SECTION_SERIALIZER.serialize(component);
            } catch (Exception e) {
                return processedText;
            }
        }

        return processedText;
    }

    /**
     * 移除所有颜色代码
     * 
     * @param text 原始文本
     * @return 无颜色文本
     */
    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 移除 & 颜色代码
        text = text.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");

        // 移除 § 颜色代码
        text = text.replaceAll("§[0-9a-fA-Fk-oK-OrR]", "");

        // 移除十六进制颜色
        text = HEX_COLOR_PATTERN.matcher(text).replaceAll("");

        // 移除 MiniMessage 标签
        text = stripMiniMessageTags(text);

        return text;
    }

    /**
     * 移除 MiniMessage 标签
     * 
     * @param text 原始文本
     * @return 无标签文本
     */
    private static String stripMiniMessageTags(String text) {
        if (text == null) {
            return "";
        }

        try {
            Component component = MINI_MESSAGE.deserialize(text);
            return PlainTextComponentSerializer.plainText().serialize(component);
        } catch (Exception e) {
            // 如果解析失败，使用正则移除
            return text.replaceAll("<[^>]+>", "");
        }
    }

    /**
     * 将文本转换为 Adventure Component
     * 支持 MiniMessage 格式
     * 
     * @param text 原始文本
     * @return Component
     */
    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 检查是否包含 MiniMessage 标签
        if (containsMiniMessageTags(text)) {
            try {
                return MINI_MESSAGE_WITH_GRADIENT.deserialize(text);
            } catch (Exception e) {
                // 回退到 Legacy 解析
            }
        }

        // 处理传统颜色代码和十六进制颜色
        String processed = colorize(text);
        return LEGACY_SERIALIZER.deserialize(processed);
    }

    /**
     * 将 Component 转换为字符串
     * 
     * @param component Component
     * @return 字符串
     */
    public static String fromComponent(Component component) {
        if (component == null) {
            return "";
        }
        return SECTION_SERIALIZER.serialize(component);
    }

    /**
     * 将 Component 转换为 MiniMessage 格式字符串
     * 
     * @param component Component
     * @return MiniMessage 格式字符串
     */
    public static String toMiniMessageString(Component component) {
        if (component == null) {
            return "";
        }
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * 解析 MiniMessage 格式文本
     * 
     * @param text MiniMessage 格式文本
     * @return 解析后的 Component
     */
    public static Component parseMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE_WITH_GRADIENT.deserialize(text);
    }

    /**
     * 解析渐变色文本
     * 使用 MiniMessage 的渐变语法
     * 
     * @param text 包含渐变标签的文本
     * @return 解析后的文本
     */
    public static String parseGradient(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            Component component = MINI_MESSAGE_WITH_GRADIENT.deserialize(text);
            return SECTION_SERIALIZER.serialize(component);
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * 创建渐变色文本
     * 
     * @param text 文本内容
     * @param fromColor 起始颜色（十六进制，如 "FF0000"）
     * @param toColor 结束颜色（十六进制，如 "0000FF"）
     * @return 渐变色文本
     */
    public static String createGradient(String text, String fromColor, String toColor) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String gradientText = "<gradient:#" + fromColor + ":#" + toColor + ">" + text + "</gradient>";
        return parseGradient(gradientText);
    }

    /**
     * 创建多色渐变文本
     * 
     * @param text 文本内容
     * @param colors 颜色数组（十六进制）
     * @return 渐变色文本
     */
    public static String createMultiGradient(String text, String... colors) {
        if (text == null || text.isEmpty() || colors == null || colors.length < 2) {
            return text != null ? text : "";
        }

        StringBuilder gradientBuilder = new StringBuilder("<gradient:");
        for (int i = 0; i < colors.length; i++) {
            gradientBuilder.append("#").append(colors[i]);
            if (i < colors.length - 1) {
                gradientBuilder.append(":");
            }
        }
        gradientBuilder.append(">").append(text).append("</gradient>");

        return parseGradient(gradientBuilder.toString());
    }

    /**
     * 创建彩虹渐变文本
     * 
     * @param text 文本内容
     * @return 彩虹渐变文本
     */
    public static String createRainbow(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String rainbowText = "<rainbow>" + text + "</rainbow>";
        return parseGradient(rainbowText);
    }

    /**
     * 解析十六进制颜色为 Component
     * 
     * @param text 包含 &#RRGGBB 格式的文本
     * @return 解析后的 Component
     */
    public static Component parseHexColors(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 转换 &#RRGGBB 为 MiniMessage 格式
        String processed = HEX_COLOR_PATTERN.matcher(text).replaceAll("<color:#$1>");

        return MINI_MESSAGE.deserialize(processed);
    }

    /**
     * 检查文本是否包含颜色代码
     * 
     * @param text 文本
     * @return 是否包含颜色代码
     */
    public static boolean containsColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return LEGACY_COLOR_PATTERN.matcher(text).find() ||
               HEX_COLOR_PATTERN.matcher(text).find() ||
               containsMiniMessageTags(text);
    }

    /**
     * 获取文本的纯文本长度（不含颜色代码）
     * 
     * @param text 文本
     * @return 纯文本长度
     */
    public static int getPlainTextLength(String text) {
        return stripColor(text).length();
    }

    /**
     * 将传统颜色代码转换为 MiniMessage 格式
     * 
     * @param text 传统格式文本
     * @return MiniMessage 格式文本
     */
    public static String legacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 颜色映射
        text = text.replace("&0", "<black>");
        text = text.replace("&1", "<dark_blue>");
        text = text.replace("&2", "<dark_green>");
        text = text.replace("&3", "<dark_aqua>");
        text = text.replace("&4", "<dark_red>");
        text = text.replace("&5", "<dark_purple>");
        text = text.replace("&6", "<gold>");
        text = text.replace("&7", "<gray>");
        text = text.replace("&8", "<dark_gray>");
        text = text.replace("&9", "<blue>");
        text = text.replace("&a", "<green>");
        text = text.replace("&b", "<aqua>");
        text = text.replace("&c", "<red>");
        text = text.replace("&d", "<light_purple>");
        text = text.replace("&e", "<yellow>");
        text = text.replace("&f", "<white>");

        // 格式映射
        text = text.replace("&k", "<obfuscated>");
        text = text.replace("&l", "<bold>");
        text = text.replace("&m", "<strikethrough>");
        text = text.replace("&n", "<underlined>");
        text = text.replace("&o", "<italic>");
        text = text.replace("&r", "<reset>");

        // 处理 § 符号
        text = text.replace("§0", "<black>");
        text = text.replace("§1", "<dark_blue>");
        text = text.replace("§2", "<dark_green>");
        text = text.replace("§3", "<dark_aqua>");
        text = text.replace("§4", "<dark_red>");
        text = text.replace("§5", "<dark_purple>");
        text = text.replace("§6", "<gold>");
        text = text.replace("§7", "<gray>");
        text = text.replace("§8", "<dark_gray>");
        text = text.replace("§9", "<blue>");
        text = text.replace("§a", "<green>");
        text = text.replace("§b", "<aqua>");
        text = text.replace("§c", "<red>");
        text = text.replace("§d", "<light_purple>");
        text = text.replace("§e", "<yellow>");
        text = text.replace("§f", "<white>");
        text = text.replace("§k", "<obfuscated>");
        text = text.replace("§l", "<bold>");
        text = text.replace("§m", "<strikethrough>");
        text = text.replace("§n", "<underlined>");
        text = text.replace("§o", "<italic>");
        text = text.replace("§r", "<reset>");

        return text;
    }
}
