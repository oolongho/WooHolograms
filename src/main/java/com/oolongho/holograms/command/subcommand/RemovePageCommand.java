package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除页命令
 * /wh removepage <名称> <页码>
 * 
 */
public class RemovePageCommand extends Subcommand {

    private final WooHolograms plugin;

    public RemovePageCommand(WooHolograms plugin) {
        super("removepage", "cmd.desc-removepage", "cmd.usage-removepage", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "cmd.removepage-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "general.invalid-number");
            return true;
        }

        if (pageNumber < 1 || pageNumber > hologram.getPageCount()) {
            plugin.getMessages().send(sender, "general.page-out-of-range", "max", String.valueOf(hologram.getPageCount()));
            return true;
        }

        if (hologram.getPageCount() <= 1) {
            plugin.getMessages().send(sender, "page.remove-failed");
            return true;
        }

        hologram.removePage(pageNumber - 1);
        hologram.save();

        plugin.getMessages().send(sender, "page.removed",
                "name", name,
                "page", String.valueOf(pageNumber),
                "count", String.valueOf(hologram.getPageCount()));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            if (hologram != null) {
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= hologram.getPageCount(); i++) pages.add(String.valueOf(i));
                return pages.stream().filter(p -> p.startsWith(args[1])).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
