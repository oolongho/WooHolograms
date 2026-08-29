package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetIntervalCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetIntervalCommand(WooHolograms plugin) {
        super("setinterval", "cmd.desc-setinterval", "cmd.usage-setinterval", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "cmd.setinterval-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        try {
            int interval = Integer.parseInt(args[1]);
            if (interval < 0 || interval > 1200) {
                plugin.getMessages().send(sender, "cmd.setinterval-range");
                return true;
            }

            hologram.setUpdateInterval(interval);
            hologram.save();

            plugin.getMessages().send(sender, "cmd.setinterval-set",
                    "name", name, "interval", String.valueOf(interval));
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "cmd.setinterval-number");
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
            List<String> intervals = Arrays.asList("0", "10", "20", "40", "60", "100");
            return intervals.stream()
                    .filter(i -> i.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}
