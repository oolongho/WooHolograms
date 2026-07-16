package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置 Display Entity 阴影属性
 * 用法:
 *   /wh setshadow <名称> <半径> <强度>          - 设置全息图级别阴影
 *   /wh setshadow <名称> <行号> <半径> <强度>   - 设置行级别阴影
 */
public class SetShadowCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetShadowCommand(WooHolograms plugin) {
        super("setshadow", "设置阴影", "/wh setshadow <名称> [行号] <半径> <强度>", "wooholograms.edit");
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

        // 判断是否指定了行号
        if (args.length >= 4) {
            // 行级别设置
            int lineNumber;
            try {
                lineNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号必须是整数！"));
                return true;
            }

            float radius, strength;
            try {
                radius = Float.parseFloat(args[2]);
                strength = Float.parseFloat(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c阴影值必须是数字！"));
                return true;
            }

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                HologramLine line = page.getLine(lineNumber - 1);
                if (line != null) {
                    line.setShadowRadius(radius);
                    line.setShadowStrength(strength);
                    hologram.save();
                    hologram.refreshAllViewers();
                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的阴影为 (半径: " + radius + ", 强度: " + strength + ")！"));
                }
            } else {
                // 全息图级别设置
                float radius, strength;
                try {
                    radius = Float.parseFloat(args[1]);
                    strength = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&c阴影值必须是数字！"));
                    return true;
                }

                hologram.setShadowRadius(radius);
                hologram.setShadowStrength(strength);
                hologram.save();
                sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的阴影为 (半径: " + radius + ", 强度: " + strength + ")！"));
            }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("1");
            suggestions.add("0");
            suggestions.add("0.5");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return Arrays.asList("0", "0.5", "1", "2").stream()
                    .filter(v -> v.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
