package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetRangeCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetRangeCommand(WooHolograms plugin) {
        super("setrange", "cmd.desc-setrange", "cmd.usage-setrange", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "cmd.setrange-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        try {
            int range = Integer.parseInt(args[1]);
            if (range <= 0) {
                plugin.getMessages().send(sender, "cmd.setrange-positive");
                return true;
            }

            hologram.setDisplayRange(range);
            hologram.save();

            plugin.getMessages().send(sender, "cmd.setrange-set", "name", name, "range", String.valueOf(range));
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "cmd.setrange-number");
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
