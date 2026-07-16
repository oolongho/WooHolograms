package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置 Display Entity 发光颜色
 * 用法:
 *   /wh setglowcolor <名称> <颜色>          - 设置全息图级别发光颜色
 *   /wh setglowcolor <名称> <行号> <颜色>   - 设置行级别发光颜色
 * 颜色格式: #RRGGBB 或 reset(重置) 或 颜色名称(red, green, blue 等)
 */
public class SetGlowColorCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetGlowColorCommand(WooHolograms plugin) {
        super("setglowcolor", "设置发光颜色", "/wh setglowcolor <名称> [行号] <颜色|#RRGGBB|reset>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            sender.sendMessage(ColorUtil.colorize("&7颜色格式: #RRGGBB 或 reset(重置) 或颜色名称(red, green, blue, yellow, white, black, aqua, purple, gold)"));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        try {
            if (args.length >= 3) {
                // 判断第二个参数是行号还是颜色
                Integer lineNumber = tryParseInt(args[1]);
                if (lineNumber != null) {
                    // 行级别设置
                    HologramPage page = hologram.getPage(0);
                    if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                        sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                        return true;
                    }

                    Integer color = parseColor(args[2]);
                    HologramLine line = page.getLine(lineNumber - 1);
                    if (line != null) {
                        line.setGlowColor(color);
                        hologram.save();
                        hologram.refreshAllViewers();
                        if (color != null) {
                            sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的发光颜色为 " + args[2] + "！"));
                        } else {
                            sender.sendMessage(ColorUtil.colorize("&a已重置第 " + lineNumber + " 行的发光颜色！"));
                        }
                    }
                } else {
                    // 全息图级别设置（args[1] 是颜色）
                    Integer color = parseColor(args[1]);
                    hologram.setGlowColor(color != null ? color : -1);
                    hologram.save();
                    if (color != null) {
                        sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的发光颜色为 " + args[1] + "！"));
                    } else {
                        sender.sendMessage(ColorUtil.colorize("&a已重置全息图 " + name + " 的发光颜色！"));
                    }
                }
            } else {
                // 全息图级别设置
                Integer color = parseColor(args[1]);
                hologram.setGlowColor(color != null ? color : -1);
                hologram.save();
                if (color != null) {
                    sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的发光颜色为 " + args[1] + "！"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&a已重置全息图 " + name + " 的发光颜色！"));
                }
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ColorUtil.colorize("&c无效的颜色格式！使用 #RRGGBB 或颜色名称(red, green, blue 等)"));
        }

        return true;
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析颜色字符串为 ARGB 整数
     * @return ARGB 颜色值，reset 返回 null
     */
    private Integer parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return null;
        }

        if (colorStr.equalsIgnoreCase("reset") || colorStr.equalsIgnoreCase("none") || colorStr.equals("-1")) {
            return null;
        }

        // #RRGGBB 格式
        if (colorStr.startsWith("#")) {
            String hex = colorStr.substring(1);
            if (hex.length() != 6 || !hex.matches("[0-9a-fA-F]{6}")) {
                throw new IllegalArgumentException("Invalid hex color length: expected 6 digits after #, got " + hex.length());
            }
            int rgb = Integer.parseInt(hex, 16);
            return rgb & 0xFFFFFF;
        }

        // 颜色名称
        return switch (colorStr.toLowerCase()) {
            case "red" -> 0xFF0000;
            case "green" -> 0x00FF00;
            case "blue" -> 0x0000FF;
            case "yellow" -> 0xFFFF00;
            case "white" -> 0xFFFFFF;
            case "black" -> 0x000000;
            case "aqua", "cyan" -> 0x00FFFF;
            case "purple" -> 0xAA00FF;
            case "gold" -> 0xFFAA00;
            case "pink" -> 0xFF00FF;
            case "orange" -> 0xFF8800;
            default -> throw new IllegalArgumentException("Unknown color: " + colorStr);
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            // 可能是行号或颜色
            suggestions.add("1");
            suggestions.add("reset");
            suggestions.add("#FF0000");
            suggestions.add("red");
            suggestions.add("green");
            suggestions.add("blue");
            suggestions.add("yellow");
            suggestions.add("white");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("reset");
            suggestions.add("#FF0000");
            suggestions.add("red");
            suggestions.add("green");
            suggestions.add("blue");
            suggestions.add("yellow");
            suggestions.add("white");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
