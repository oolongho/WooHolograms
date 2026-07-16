package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CopyCommand extends Subcommand {

    private final WooHolograms plugin;

    public CopyCommand(WooHolograms plugin) {
        super("copy", "克隆一个全息图", "/wh copy <源名称> <目标名称>", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String sourceName = args[0];
        String targetName = args[1];

        if (!plugin.getHologramManager().containsHologram(sourceName)) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + sourceName + " 不存在！"));
            return true;
        }

        if (plugin.getHologramManager().containsHologram(targetName)) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + targetName + " 已存在！"));
            return true;
        }

        Hologram target = plugin.getHologramManager().cloneHologram(sourceName, targetName, null, false);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize("&c克隆全息图失败！"));
            return true;
        }

        target.save();
        sender.sendMessage(ColorUtil.colorize("&a成功克隆全息图 " + sourceName + " 到 " + targetName + "！"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
