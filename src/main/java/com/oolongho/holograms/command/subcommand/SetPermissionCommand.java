package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetPermissionCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetPermissionCommand(WooHolograms plugin) {
        super("setpermission", "设置查看权限", "/wh setpermission <名称> [权限]", "wooholograms.admin");
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

        String permission = args.length > 1 ? args[1] : null;
        hologram.setPermission(permission);
        hologram.save();

        if (permission == null || permission.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&a已清除 " + name + " 的查看权限！"));
        } else {
            sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的查看权限设置为 " + permission + "！"));
        }

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
