package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置 Chroma 彩虹色效果
 * 用法:
 *   /wh setchroma <名称> background <true|false>   - 设置全息图级别 Chroma 背景
 *   /wh setchroma <名称> glow <true|false>         - 设置全息图级别 Chroma 发光
 *   /wh setchroma <名称> <行号> background <true|false> - 设置行级别 Chroma 背景
 *   /wh setchroma <名称> <行号> glow <true|false>       - 设置行级别 Chroma 发光
 */
public class SetChromaCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetChromaCommand(WooHolograms plugin) {
        super("setchroma", "设置Chroma彩虹色效果", "/wh setchroma <名称> [行号] <background|glow> <true|false>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        try {
            // 判断参数格式：是否有行号
            Integer lineNumber = tryParseInt(args[1]);
            if (lineNumber != null) {
                // 行级别设置: /wh setchroma <名称> <行号> <type> <value>
                if (args.length < 4) {
                    sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
                    return true;
                }

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                String type = args[2].toLowerCase();
                boolean value = Boolean.parseBoolean(args[3]);
                HologramLine line = page.getLine(lineNumber - 1);
                if (line == null) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                if (type.equals("background") || type.equals("bg")) {
                    line.setChromaBackground(value);
                    hologram.save();
                    hologram.refreshAllViewers();
                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的 Chroma 背景为 " + value + "！"));
                } else if (type.equals("glow")) {
                    line.setChromaGlow(value);
                    hologram.save();
                    hologram.refreshAllViewers();
                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的 Chroma 发光为 " + value + "！"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&c类型必须是 background 或 glow！"));
                }
            } else {
                // 全息图级别设置: /wh setchroma <名称> <type> <value>
                String type = args[1].toLowerCase();
                boolean value = Boolean.parseBoolean(args[2]);

                if (type.equals("background") || type.equals("bg")) {
                    hologram.setChromaBackground(value);
                    hologram.save();
                    sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的 Chroma 背景为 " + value + "！"));
                } else if (type.equals("glow")) {
                    hologram.setChromaGlow(value);
                    hologram.save();
                    sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的 Chroma 发光为 " + value + "！"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&c类型必须是 background 或 glow！"));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(ColorUtil.colorize("&c设置 Chroma 时出错: " + e.getMessage()));
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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("background");
            suggestions.add("glow");
            suggestions.add("1");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // 如果 args[1] 是行号，则提示类型
            Integer lineNum = tryParseInt(args[1]);
            if (lineNum != null) {
                List<String> suggestions = new ArrayList<>();
                suggestions.add("background");
                suggestions.add("glow");
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                // args[1] 是类型，提示布尔值
                List<String> suggestions = new ArrayList<>();
                suggestions.add("true");
                suggestions.add("false");
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 4) {
            // args[1] 是行号, args[2] 是类型, 提示布尔值
            List<String> suggestions = new ArrayList<>();
            suggestions.add("true");
            suggestions.add("false");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
