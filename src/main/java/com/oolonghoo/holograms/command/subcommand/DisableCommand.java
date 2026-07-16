package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DisableCommand extends Subcommand {

    private final WooHolograms plugin;

    public DisableCommand(WooHolograms plugin) {
        super("disable", "禁用一个全息图", "/wh disable <名称>", "wooholograms.admin");
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

        if (!hologram.isEnabled()) {
            sender.sendMessage(ColorUtil.colorize("&e全息图 " + name + " 已经是禁用状态！"));
            return true;
        }

        hologram.setEnabled(false);
        hologram.save();
        hologram.hideFromAll();

        sender.sendMessage(ColorUtil.colorize("&a已禁用全息图 " + name + "！"));
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
