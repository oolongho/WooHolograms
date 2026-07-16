package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetRangeCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetRangeCommand(WooHolograms plugin) {
        super("setrange", "设置显示范围", "/wh setrange <名称> <范围>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
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
            int range = Integer.parseInt(args[1]);
            if (range <= 0) {
                sender.sendMessage(ColorUtil.colorize("&c范围必须是正整数！"));
                return true;
            }

            hologram.setDisplayRange(range);
            hologram.save();

            sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的显示范围设置为 " + range + " 格！"));
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c范围必须是数字！"));
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
            List<String> ranges = Arrays.asList("16", "32", "48", "64", "128");
            return ranges.stream()
                    .filter(r -> r.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
