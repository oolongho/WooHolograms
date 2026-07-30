package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CopyCommand extends Subcommand {

    private final WooHolograms plugin;

    public CopyCommand(WooHolograms plugin) {
        super("copy", "cmd.desc-copy", "cmd.usage-copy", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "copy.usage");
            return true;
        }

        String sourceName = args[0];
        String targetName = args[1];

        if (!plugin.getHologramManager().containsHologram(sourceName)) {
            plugin.getMessages().send(sender, "copy.source-not-found", "source", sourceName);
            return true;
        }

        if (plugin.getHologramManager().containsHologram(targetName)) {
            plugin.getMessages().send(sender, "copy.target-exists", "target", targetName);
            return true;
        }

        Hologram target = plugin.getHologramManager().cloneHologram(sourceName, targetName, null, false);
        if (target == null) {
            plugin.getMessages().send(sender, "copy.failed");
            return true;
        }

        target.save();
        plugin.getMessages().send(sender, "copy.clone-success", "source", sourceName, "target", targetName);
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
