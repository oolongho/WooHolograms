package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
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
        super("setscale", "cmd.desc-setscale", "cmd.usage-setscale", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.getMessages().send(sender, "cmd.setscale-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        // 判断是否指定了行号
        if (args.length >= 5) {
            // 行级别设置
            int lineNumber;
            try {
                lineNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                plugin.getMessages().send(sender, "cmd.setscale-line-number");
                return true;
            }

            float x, y, z;
            try {
                x = Float.parseFloat(args[2]);
                y = Float.parseFloat(args[3]);
                z = Float.parseFloat(args[4]);
            } catch (NumberFormatException e) {
                plugin.getMessages().send(sender, "cmd.setscale-value-number");
                return true;
            }

            HologramPage page = hologram.getPage(0);
            if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                plugin.getMessages().send(sender, "general.line-invalid");
                return true;
            }

            HologramLine line = page.getLine(lineNumber - 1);
            if (line != null) {
                line.setScale(x, y, z);
                hologram.save();
                hologram.refreshAllViewers();
                plugin.getMessages().send(sender, "cmd.setscale-set-line",
                        "line", String.valueOf(lineNumber),
                        "x", String.valueOf(x),
                        "y", String.valueOf(y),
                        "z", String.valueOf(z));
            }
        } else {
            // 全息图级别设置
            float x, y, z;
            try {
                x = Float.parseFloat(args[1]);
                y = Float.parseFloat(args[2]);
                z = Float.parseFloat(args[3]);
            } catch (NumberFormatException e) {
                plugin.getMessages().send(sender, "cmd.setscale-value-number");
                return true;
            }

            hologram.setScale(x, y, z);
            hologram.save();
            plugin.getMessages().send(sender, "cmd.setscale-set",
                    "name", name,
                    "x", String.valueOf(x),
                    "y", String.valueOf(y),
                    "z", String.valueOf(z));
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
