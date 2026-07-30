package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnableCommand extends Subcommand {

    private final WooHolograms plugin;

    public EnableCommand(WooHolograms plugin) {
        super("enable", "cmd.desc-enable", "cmd.usage-enable", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessages().send(sender, "cmd.enable-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        if (hologram.isEnabled()) {
            plugin.getMessages().send(sender, "cmd.enable-already", "name", name);
            return true;
        }

        hologram.setEnabled(true);
        hologram.save();
        hologram.showToNearby();

        plugin.getMessages().send(sender, "cmd.enable-success", "name", name);
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
