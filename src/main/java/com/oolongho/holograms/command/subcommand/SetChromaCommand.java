package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
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
        super("setchroma", "cmd.desc-setchroma", "cmd.usage-setchroma", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "cmd.setchroma-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        try {
            // 判断参数格式：是否有行号
            Integer lineNumber = tryParseInt(args[1]);
            if (lineNumber != null) {
                // 行级别设置: /wh setchroma <名称> <行号> <type> <value>
                if (args.length < 4) {
                    plugin.getMessages().send(sender, "cmd.setchroma-usage");
                    return true;
                }

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    plugin.getMessages().send(sender, "general.line-invalid");
                    return true;
                }

                String type = args[2].toLowerCase();
                boolean value = Boolean.parseBoolean(args[3]);
                HologramLine line = page.getLine(lineNumber - 1);
                if (line == null) {
                    plugin.getMessages().send(sender, "general.line-invalid");
                    return true;
                }

                if (type.equals("background") || type.equals("bg")) {
                    line.setChromaBackground(value);
                    hologram.save();
                    hologram.refreshAllViewers();
                    plugin.getMessages().send(sender, "display.chroma-background-set-line",
                            "line", String.valueOf(lineNumber), "value", String.valueOf(value));
                } else if (type.equals("glow")) {
                    line.setChromaGlow(value);
                    hologram.save();
                    hologram.refreshAllViewers();
                    plugin.getMessages().send(sender, "display.chroma-glow-set-line",
                            "line", String.valueOf(lineNumber), "value", String.valueOf(value));
                } else {
                    plugin.getMessages().send(sender, "display.chroma-type-invalid");
                }
            } else {
                // 全息图级别设置: /wh setchroma <名称> <type> <value>
                String type = args[1].toLowerCase();
                boolean value = Boolean.parseBoolean(args[2]);

                if (type.equals("background") || type.equals("bg")) {
                    hologram.setChromaBackground(value);
                    hologram.save();
                    plugin.getMessages().send(sender, "display.chroma-background-set", "value", String.valueOf(value));
                } else if (type.equals("glow")) {
                    hologram.setChromaGlow(value);
                    hologram.save();
                    plugin.getMessages().send(sender, "display.chroma-glow-set", "value", String.valueOf(value));
                } else {
                    plugin.getMessages().send(sender, "display.chroma-type-invalid");
                }
            }
        } catch (Exception e) {
            plugin.getMessages().send(sender, "display.chroma-error", "error", e.getMessage());
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
