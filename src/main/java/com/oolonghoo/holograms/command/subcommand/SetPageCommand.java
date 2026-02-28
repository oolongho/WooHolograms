package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置玩家当前页命令
 * /wh setpage <名称> <页码>
 * 
 * @author oolongho
 */
public class SetPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetPageCommand(WooHolograms plugin) {
        super("setpage", "设置你查看的全息图页码", "/wh setpage <名称> <页码>", "wooholograms.use", Arrays.asList("sp"));
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.invalid-number")));
            return true;
        }

        if (pageNumber < 1 || pageNumber > hologram.getPageCount()) {
            player.sendMessage(ColorUtil.colorize("&c页码必须在 1 到 " + hologram.getPageCount() + " 之间！"));
            return true;
        }

        if (hologram.switchPage(player, pageNumber - 1)) {
            player.sendMessage(ColorUtil.colorize("&a已切换到第 " + pageNumber + " 页。"));
        } else {
            player.sendMessage(ColorUtil.colorize("&c切换页面失败！"));
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
