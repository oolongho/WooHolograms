package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SwapPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public SwapPageCommand(WooHolograms plugin) {
        super("swappage", "交换两个页面的位置", "/wh swappage <名称> <页码1> <页码2>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        try {
            int page1 = Integer.parseInt(args[1]);
            int page2 = Integer.parseInt(args[2]);
            int pageCount = hologram.getPageCount();

            if (page1 < 1 || page1 > pageCount) {
                sender.sendMessage(ColorUtil.colorize("&c页码1必须在 1 到 " + pageCount + " 之间！"));
                return true;
            }
            if (page2 < 1 || page2 > pageCount) {
                sender.sendMessage(ColorUtil.colorize("&c页码2必须在 1 到 " + pageCount + " 之间！"));
                return true;
            }
            if (page1 == page2) {
                sender.sendMessage(ColorUtil.colorize("&c两个页码不能相同！"));
                return true;
            }

            if (hologram.swapPages(page1 - 1, page2 - 1)) {
                hologram.save();
                sender.sendMessage(ColorUtil.colorize("&a已交换全息图 " + name + " 的第 " + page1 + " 页和第 " + page2 + " 页！"));
            } else {
                sender.sendMessage(ColorUtil.colorize("&c交换页面失败！"));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c页码必须是数字！"));
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
