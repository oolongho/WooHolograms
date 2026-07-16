package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetIntervalCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetIntervalCommand(WooHolograms plugin) {
        super("setinterval", "设置更新间隔", "/wh setinterval <名称> <tick>", "wooholograms.edit");
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
            int interval = Integer.parseInt(args[1]);
            if (interval <= 0) {
                sender.sendMessage(ColorUtil.colorize("&c间隔必须是正整数！"));
                return true;
            }

            hologram.setUpdateInterval(interval);
            hologram.save();

            sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的更新间隔设置为 " + interval + " tick！"));
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c间隔必须是数字！"));
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
            List<String> intervals = Arrays.asList("1", "5", "10", "20", "40", "60");
            return intervals.stream()
                    .filter(i -> i.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
