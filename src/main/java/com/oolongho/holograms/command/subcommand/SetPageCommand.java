package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置玩家当前页命令
 * /wh setpage <名称> <页码>
 * 
 */
public class SetPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetPageCommand(WooHolograms plugin) {
        super("setpage", "cmd.desc-setpage", "cmd.usage-setpage", "wooholograms.use");
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            plugin.getMessages().send(player, "cmd.setpage-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(player, "general.hologram-not-found", "name", name);
            return true;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessages().send(player, "general.invalid-number");
            return true;
        }

        if (pageNumber < 1 || pageNumber > hologram.getPageCount()) {
            plugin.getMessages().send(player, "general.page-out-of-range", "max", String.valueOf(hologram.getPageCount()));
            return true;
        }

        if (hologram.switchPage(player, pageNumber - 1)) {
            plugin.getMessages().send(player, "page.switch-success", "page", String.valueOf(pageNumber));
        } else {
            plugin.getMessages().send(player, "page.switch-failed");
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
