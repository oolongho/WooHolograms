package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插入行命令
 * /wh insertline <名称> <行号> <内容>
 * 
 */
public class InsertLineCommand extends Subcommand {

    private final WooHolograms plugin;

    public InsertLineCommand(WooHolograms plugin) {
        super("insertline", "在指定位置插入一行", "/wh insertline <名称> <行号> <内容>", "wooholograms.edit", Arrays.asList("il"));
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
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        int lineNumber;
        try {
            lineNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.invalid-number")));
            return true;
        }

        HologramPage page = hologram.getPage(0);
        if (page == null) {
            page = hologram.addPage();
        }

        if (lineNumber < 1 || lineNumber > page.size() + 1) {
            sender.sendMessage(ColorUtil.colorize("&c行号必须在 1 到 " + (page.size() + 1) + " 之间！"));
            return true;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        page.addLine(content);
        
        // 如果不是插入到末尾，需要交换位置
        if (lineNumber <= page.size() - 1) {
            for (int i = page.size() - 1; i > lineNumber - 1; i--) {
                page.swapLines(i, i - 1);
            }
        }
        hologram.save();

        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("edit.line-inserted", "line", String.valueOf(lineNumber))));
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
                HologramPage page = hologram.getPage(0);
                if (page != null) {
                    List<String> lines = new ArrayList<>();
                    for (int i = 1; i <= page.size() + 1; i++) lines.add(String.valueOf(i));
                    return lines.stream().filter(l -> l.startsWith(args[1])).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
