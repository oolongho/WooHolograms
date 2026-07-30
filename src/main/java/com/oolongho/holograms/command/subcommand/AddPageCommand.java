package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public AddPageCommand(WooHolograms plugin) {
        super("addpage", "cmd.desc-addpage", "cmd.usage-addpage", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessages().send(sender, "cmd.addpage-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        HologramPage page = hologram.addPage();
        if (page == null) {
            plugin.getMessages().send(sender, "page.add-failed");
            return true;
        }

        if (args.length > 1) {
            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            page.addLine(text);
        }

        hologram.save();
        plugin.getMessages().send(sender, "page.added", "count", String.valueOf(hologram.getPageCount()));
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
