package com.oolonghoo.holograms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本工具类
 * 处理文本相关的操作
 * 
 */
public class TextUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    /**
     * 替换文本中的占位符
     * 
     * @param text 原始文本
     * @param replacements 替换内容（键值对）
     * @return 替换后的文本
     */
    public static String replace(String text, String... replacements) {
        if (text == null || replacements == null) {
            return text;
        }
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            text = text.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        
        return text;
    }

    /**
     * 分割文本为多行
     * 
     * @param text 原始文本
     * @param maxLength 每行最大长度
     * @return 分割后的行列表
     */
    public static List<String> splitText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return lines;
        }
        
        if (text.length() <= maxLength) {
            lines.add(text);
            return lines;
        }
        
        StringBuilder currentLine = new StringBuilder();
        int currentLength = 0;
        
        for (String word : text.split(" ")) {
            int wordLength = stripColor(word).length();
            
            if (currentLength + wordLength + 1 > maxLength && currentLength > 0) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
                currentLength = 0;
            }
            
            if (currentLength > 0) {
                currentLine.append(" ");
                currentLength++;
            }
            
            currentLine.append(word);
            currentLength += wordLength;
        }
        
        if (currentLength > 0) {
            lines.add(currentLine.toString().trim());
        }
        
        return lines;
    }

    /**
     * 移除颜色代码
     * 
     * @param text 原始文本
     * @return 无颜色文本
     */
    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        
        // 移除 & 颜色代码
        text = text.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
        
        // 移除 § 颜色代码
        text = text.replaceAll("§[0-9a-fA-Fk-oK-OrR]", "");
        
        // 移除 hex 颜色
        text = HEX_PATTERN.matcher(text).replaceAll("");
        
        return text;
    }

    /**
     * 获取文本的实际长度（不包含颜色代码）
     * 
     * @param text 文本
     * @return 实际长度
     */
    public static int getLength(String text) {
        return stripColor(text).length();
    }

    /**
     * 重复字符串
     * 
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        
        return sb.toString();
    }

    /**
     * 居中文本
     * 
     * @param text 文本
     * @param width 总宽度
     * @return 居中后的文本
     */
    public static String center(String text, int width) {
        int textLength = getLength(text);
        
        if (textLength >= width) {
            return text;
        }
        
        int padding = (width - textLength) / 2;
        return repeat(" ", padding) + text;
    }

    /**
     * 检查字符串是否为空或空白
     * 
     * @param str 字符串
     * @return 是否为空或空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为空
     * 
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 截断文本
     * 
     * @param text 文本
     * @param maxLength 最大长度
     * @param suffix 后缀（如 "..."）
     * @return 截断后的文本
     */
    public static String truncate(String text, int maxLength, String suffix) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength - suffix.length()) + suffix;
    }
}
