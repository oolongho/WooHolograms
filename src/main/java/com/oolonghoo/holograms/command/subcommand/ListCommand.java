package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 列出全息图命令
 * /wh list [页码]
 * 
 * @author oolongho
 */
public class ListCommand extends Subcommand {

    private final WooHolograms plugin;
    private static final int ITEMS_PER_PAGE = 10;

    public ListCommand(WooHolograms plugin) {
        super("list", "列出所有全息图", "/wh list [页码]", "wooholograms.list", Arrays.asList("ls"));
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
                sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.invalid-number")));
                return true;
            }
        }

        List<Hologram> holograms = new ArrayList<>(plugin.getHologramManager().getHolograms());
        int total = holograms.size();
        int maxPage = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));

        if (page > maxPage) page = maxPage;

        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().get("list.header")));

        if (holograms.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().get("list.empty")));
        } else {
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, total);

            for (int i = startIndex; i < endIndex; i++) {
                Hologram hologram = holograms.get(i);
                String worldName = hologram.getLocation() != null && hologram.getLocation().getWorld() != null
                        ? hologram.getLocation().getWorld().getName() : "Unknown";

                sender.sendMessage(ColorUtil.colorize("&e" + hologram.getName() + " &7- 世界: " + worldName + 
                        ", 位置: " + String.format("%.1f, %.1f, %.1f", 
                        hologram.getLocation().getX(), hologram.getLocation().getY(), hologram.getLocation().getZ())));
            }

            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().get("list.footer", "count", String.valueOf(total))));
            if (maxPage > 1) {
                sender.sendMessage(ColorUtil.colorize("&e第 " + page + " 页 / 共 " + maxPage + " 页"));
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
