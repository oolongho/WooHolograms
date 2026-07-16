package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddPageCommand extends Subcommand {

    private final WooHolograms plugin;

    public AddPageCommand(WooHolograms plugin) {
        super("addpage", "添加一个新页面", "/wh addpage <名称> [内容]", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        HologramPage page = hologram.addPage();
        if (page == null) {
            sender.sendMessage(ColorUtil.colorize("&c添加页面失败！"));
            return true;
        }

        if (args.length > 1) {
            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            page.addLine(text);
        }

        hologram.save();
        sender.sendMessage(ColorUtil.colorize("&a已添加第 " + hologram.getPageCount() + " 页！"));
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
