package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnableCommand extends Subcommand {

    private final WooHolograms plugin;

    public EnableCommand(WooHolograms plugin) {
        super("enable", "启用一个全息图", "/wh enable <名称>", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        if (hologram.isEnabled()) {
            sender.sendMessage(ColorUtil.colorize("&e全息图 " + name + " 已经是启用状态！"));
            return true;
        }

        hologram.setEnabled(true);
        hologram.save();
        hologram.showToNearby();

        sender.sendMessage(ColorUtil.colorize("&a已启用全息图 " + name + "！"));
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
