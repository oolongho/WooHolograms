package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetDoubleSidedCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetDoubleSidedCommand(WooHolograms plugin) {
        super("setdoublesided", "cmd.desc-setdoublesided", "cmd.usage-setdoublesided", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "cmd.setdoublesided-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        boolean doubleSided = Boolean.parseBoolean(args[1]);
        hologram.setDoubleSided(doubleSided);
        hologram.save();

        plugin.getMessages().send(sender, "cmd.setdoublesided-success", "name", name, "value", String.valueOf(doubleSided));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("true", "false").stream()
                    .filter(v -> v.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
