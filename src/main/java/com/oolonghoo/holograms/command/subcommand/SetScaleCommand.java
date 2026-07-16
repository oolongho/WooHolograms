package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置 Display Entity 缩放属性
 * 用法:
 *   /wh setscale <名称> <x> <y> <z>          - 设置全息图级别缩放
 *   /wh setscale <名称> <行号> <x> <y> <z>   - 设置行级别缩放
 */
public class SetScaleCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetScaleCommand(WooHolograms plugin) {
        super("setscale", "设置缩放", "/wh setscale <名称> [行号] <x> <y> <z>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
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
        if (args.length >= 5) {
                // 行级别设置
                int lineNumber;
                try {
                    lineNumber = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&c行号必须是整数！"));
                    return true;
                }

                float x, y, z;
                try {
                    x = Float.parseFloat(args[2]);
                    y = Float.parseFloat(args[3]);
                    z = Float.parseFloat(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&c缩放值必须是数字！"));
                    return true;
                }

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                HologramLine line = page.getLine(lineNumber - 1);
                if (line != null) {
                    line.setScale(x, y, z);
                    hologram.save();
                    hologram.refreshAllViewers();
                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的缩放为 (" + x + ", " + y + ", " + z + ")！"));
                }
            } else {
                // 全息图级别设置
                float x, y, z;
                try {
                    x = Float.parseFloat(args[1]);
                    y = Float.parseFloat(args[2]);
                    z = Float.parseFloat(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.colorize("&c缩放值必须是数字！"));
                    return true;
                }

                hologram.setScale(x, y, z);
                hologram.save();
                sender.sendMessage(ColorUtil.colorize("&a已设置全息图 " + name + " 的缩放为 (" + x + ", " + y + ", " + z + ")！"));
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
            // 可能是行号或x值
            List<String> suggestions = new ArrayList<>();
            suggestions.add("1");
            suggestions.add("0.5");
            suggestions.add("2");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        } else if (args.length >= 3 && args.length <= 5) {
            return Arrays.asList("0.5", "1", "1.5", "2").stream()
                    .filter(v -> v.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
