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

public class HeightCommand extends Subcommand {

    private final WooHolograms plugin;

    public HeightCommand(WooHolograms plugin) {
        super("height", "设置行高度", "/wh height <名称> <行号> <高度>", "wooholograms.edit");
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
            int lineNumber = Integer.parseInt(args[1]);
            double height = Double.parseDouble(args[2]);

            HologramPage page = hologram.getPage(0);
            if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                return true;
            }

            HologramLine line = page.getLine(lineNumber - 1);
            if (line != null) {
                line.setHeight(height);
                hologram.save();
                hologram.realignLines();

                sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的高度为 " + height + "！"));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c行号和高度必须是数字！"));
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
            String hologramName = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
            if (hologram != null && hologram.getPage(0) != null) {
                int lineCount = hologram.getPage(0).size();
                List<String> lineNumbers = new ArrayList<>();
                for (int i = 1; i <= lineCount; i++) {
                    lineNumbers.add(String.valueOf(i));
                }
                return lineNumbers.stream()
                        .filter(n -> n.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            return Arrays.asList("0.2", "0.25", "0.3", "0.5").stream()
                    .filter(v -> v.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
