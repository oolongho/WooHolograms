package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除页命令
 * /wh removepage <名称> <页码>
 * 
 * @author oolongho
 */
public class RemovePageCommand extends Subcommand {

    private final WooHolograms plugin;

    public RemovePageCommand(WooHolograms plugin) {
        super("removepage", "删除全息图的一页", "/wh removepage <名称> <页码>", "wooholograms.edit", Arrays.asList("rp", "delpage"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.invalid-number")));
            return true;
        }

        if (pageNumber < 1 || pageNumber > hologram.getPageCount()) {
            sender.sendMessage(ColorUtil.colorize("&c页码必须在 1 到 " + hologram.getPageCount() + " 之间！"));
            return true;
        }

        if (hologram.getPageCount() <= 1) {
            sender.sendMessage(ColorUtil.colorize("&c无法删除最后一页！"));
            return true;
        }

        hologram.removePage(pageNumber - 1);
        hologram.save();

        sender.sendMessage(ColorUtil.colorize("&a已删除全息图 " + name + " 的第 " + pageNumber + " 页！当前共 " + hologram.getPageCount() + " 页。"));
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
