package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetPermissionCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetPermissionCommand(WooHolograms plugin) {
        super("setpermission", "cmd.desc-setpermission", "cmd.usage-setpermission", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessages().send(sender, "cmd.setpermission-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        String permission = args.length > 1 ? args[1] : null;
        hologram.setPermission(permission);
        hologram.save();

        if (permission == null || permission.isEmpty()) {
            plugin.getMessages().send(sender, "cmd.setpermission-clear", "name", name);
        } else {
            plugin.getMessages().send(sender, "cmd.setpermission-set", "name", name, "permission", permission);
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
