package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SwapPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public SwapPageCommand(WooHolograms plugin) {
        super("swappage", "cmd.desc-swappage", "cmd.usage-swappage", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "cmd.swappage-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        try {
            int page1 = Integer.parseInt(args[1]);
            int page2 = Integer.parseInt(args[2]);
            int pageCount = hologram.getPageCount();

            if (page1 < 1 || page1 > pageCount) {
                plugin.getMessages().send(sender, "cmd.swappage-range1", "max", String.valueOf(pageCount));
                return true;
            }
            if (page2 < 1 || page2 > pageCount) {
                plugin.getMessages().send(sender, "cmd.swappage-range2", "max", String.valueOf(pageCount));
                return true;
            }
            if (page1 == page2) {
                plugin.getMessages().send(sender, "cmd.swappage-same");
                return true;
            }

            if (hologram.swapPages(page1 - 1, page2 - 1)) {
                hologram.save();
                plugin.getMessages().send(sender, "cmd.swappage-success",
                        "name", name, "p1", String.valueOf(page1), "p2", String.valueOf(page2));
            } else {
                plugin.getMessages().send(sender, "cmd.swappage-failed");
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "cmd.swappage-number");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 || args.length == 3) {
            String hologramName = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
            if (hologram != null) {
                int pageCount = hologram.getPageCount();
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= pageCount; i++) {
                    pages.add(String.valueOf(i));
                }
                return pages.stream()
                        .filter(p -> p.startsWith(args[args.length - 1]))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
