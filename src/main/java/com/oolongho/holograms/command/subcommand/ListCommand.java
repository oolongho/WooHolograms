package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * 列出全息图命令
 * /wh list [页码]
 * 
 */
public class ListCommand extends Subcommand {

    private final WooHolograms plugin;
    private static final int ITEMS_PER_PAGE = 10;

    public ListCommand(WooHolograms plugin) {
        super("list", "cmd.desc-list", "cmd.usage-list", "wooholograms.list");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                plugin.getMessages().send(sender, "general.invalid-number");
                return true;
            }
        }

        List<Hologram> holograms = new ArrayList<>(plugin.getHologramManager().getHolograms());
        int total = holograms.size();
        int maxPage = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));

        if (page > maxPage) page = maxPage;

        plugin.getMessages().send(sender, "list.header");

        if (holograms.isEmpty()) {
            plugin.getMessages().send(sender, "list.empty");
        } else {
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, total);

            for (int i = startIndex; i < endIndex; i++) {
                Hologram hologram = holograms.get(i);
                String worldName = hologram.getLocation() != null && hologram.getLocation().getWorld() != null
                        ? hologram.getLocation().getWorld().getName() : "Unknown";

                plugin.getMessages().send(sender, "list.line",
                        "name", hologram.getName(),
                        "world", worldName,
                        "x", String.format("%.1f", hologram.getLocation().getX()),
                        "y", String.format("%.1f", hologram.getLocation().getY()),
                        "z", String.format("%.1f", hologram.getLocation().getZ()));
            }

            plugin.getMessages().send(sender, "list.footer", "count", String.valueOf(total));
            if (maxPage > 1) {
                plugin.getMessages().send(sender, "list.page-info",
                        "page", String.valueOf(page),
                        "total", String.valueOf(maxPage));
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> pages = new ArrayList<>();
            int maxPage = Math.max(1, (int) Math.ceil((double) plugin.getHologramManager().getHologramCount() / ITEMS_PER_PAGE));
            for (int i = 1; i <= maxPage; i++) pages.add(String.valueOf(i));
            return pages;
        }
        return new ArrayList<>();
    }
}
