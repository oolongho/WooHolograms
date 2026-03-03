package com.oolonghoo.holograms.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 物品 NBT 数据工具类
 * 支持解析 NBT 格式和键值对格式
 *
 * @author oolongho
 */
public class ItemNbtUtil {

    private static final Pattern NBT_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    /**
     * 解析物品参数
     * 支持两种格式：
     * 1. NBT 格式: {CustomModelData:10000,display:{color:3847130}}
     * 2. 键值对格式: custom-model-data:10000 color:FF0000 name:&c红色胸甲
     *
     * @param input 输入字符串
     * @return 解析后的 NBT 数据
     */
    public static CompoundTag parseItemData(String input) {
        if (input == null || input.isEmpty()) {
            return new CompoundTag();
        }
        
        CompoundTag result = new CompoundTag();
        
        // 尝试解析 NBT 格式
        Matcher nbtMatcher = NBT_PATTERN.matcher(input);
        if (nbtMatcher.find()) {
            String nbtContent = nbtMatcher.group(1);
            parseNbtFormat(nbtContent, result);
        }
        
        // 解析键值对格式
        String remaining = nbtMatcher.find() ? input.substring(nbtMatcher.end()).trim() : input;
        parseKeyValueFormat(remaining, result);
        
        return result;
    }
    
    /**
     * 解析 NBT 格式
     */
    private static void parseNbtFormat(String content, CompoundTag result) {
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex > 0) {
                String key = pair.substring(0, colonIndex).trim();
                String value = pair.substring(colonIndex + 1).trim();
                
                // 处理嵌套 NBT
                if (value.startsWith("{")) {
                    CompoundTag nested = new CompoundTag();
                    parseNbtFormat(value.substring(1, value.length() - 1), nested);
                    result.put(key, nested);
                } else {
                    putValue(result, key, value);
                }
            }
        }
    }
    
    /**
     * 解析键值对格式
     */
    private static void parseKeyValueFormat(String content, CompoundTag result) {
        if (content == null || content.isEmpty()) {
            return;
        }
        
        String[] parts = content.split("\\s+");
        
        for (String part : parts) {
            int colonIndex = part.indexOf(':');
            if (colonIndex > 0) {
                String key = part.substring(0, colonIndex).toLowerCase().replace("-", "_");
                String value = part.substring(colonIndex + 1);
                
                switch (key) {
                    case "custom_model_data":
                    case "custommodeldata":
                    case "cmd":
                        result.putInt("CustomModelData", parseInt(value));
                        break;
                    case "color":
                        CompoundTag display = result.getCompound("display");
                        if (display.isEmpty()) {
                            result.put("display", display);
                        }
                        display.putInt("color", parseColor(value));
                        break;
                    case "name":
                        CompoundTag displayTag = result.getCompound("display");
                        if (displayTag.isEmpty()) {
                            result.put("display", displayTag);
                        }
                        displayTag.putString("Name", "{\"text\":\"" + ColorUtil.colorize(value) + "\"}");
                        break;
                    case "lore":
                        CompoundTag displayTag2 = result.getCompound("display");
                        if (displayTag2.isEmpty()) {
                            result.put("display", displayTag2);
                        }
                        ListTag loreList = new ListTag();
                        loreList.add(StringTag.valueOf("{\"text\":\"" + ColorUtil.colorize(value) + "\"}"));
                        displayTag2.put("Lore", loreList);
                        break;
                    case "glow":
                        if (value.isEmpty() || value.equalsIgnoreCase("true")) {
                            result.putBoolean("Ench", true);
                        }
                        break;
                    case "unbreakable":
                        if (value.isEmpty() || value.equalsIgnoreCase("true")) {
                            result.putBoolean("Unbreakable", true);
                        }
                        break;
                }
            } else {
                // 无值的参数（如 glow）
                switch (part.toLowerCase()) {
                    case "glow":
                        result.putBoolean("Ench", true);
                        break;
                    case "unbreakable":
                        result.putBoolean("Unbreakable", true);
                        break;
                }
            }
        }
    }
    
    /**
     * 解析整数值
     */
    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 解析颜色值
     * 支持：十进制、十六进制（0xRRGGBB 或 #RRGGBB）
     */
    private static int parseColor(String value) {
        try {
            if (value.startsWith("0x") || value.startsWith("0X")) {
                return Integer.parseInt(value.substring(2), 16);
            } else if (value.startsWith("#")) {
                return Integer.parseInt(value.substring(1), 16);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 将值放入 NBT 标签
     */
    private static void putValue(CompoundTag tag, String key, String value) {
        // 尝试解析为数字
        try {
            if (value.contains(".")) {
                tag.putDouble(key, Double.parseDouble(value));
            } else {
                tag.putInt(key, Integer.parseInt(value));
            }
            return;
        } catch (NumberFormatException ignored) {
        }
        
        // 布尔值
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            tag.putBoolean(key, Boolean.parseBoolean(value));
            return;
        }
        
        // 字符串
        tag.putString(key, value);
    }
    
    /**
     * 获取键值对格式的示例（用于 TAB 补全）
     */
    public static Map<String, String> getKeyValueExamples() {
        Map<String, String> examples = new HashMap<>();
        examples.put("custom-model-data:<值>", "自定义模型数据 (或 cmd:<值>)");
        examples.put("color:<RGB>", "皮革颜色 (如 color:FF0000 或 color:16711680)");
        examples.put("name:<名称>", "自定义名称 (支持颜色代码)");
        examples.put("lore:<描述>", "物品描述");
        examples.put("glow", "发光效果");
        examples.put("unbreakable", "无法破坏");
        return examples;
    }
}
