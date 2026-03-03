package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import com.oolonghoo.holograms.util.TabCompleteUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置行命令
 * /wh setline <名称> <行号> <内容>
 * 
 * @author oolongho
 */
public class SetLineCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetLineCommand(WooHolograms plugin) {
        super("setline", "设置全息图的一行文本", "/wh setline <名称> <行号> <内容>", "wooholograms.edit", Arrays.asList("sl"));
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
        if (page == null || page.size() == 0) {
            sender.sendMessage(ColorUtil.colorize("&c此全息图没有内容！"));
            return true;
        }

        if (lineNumber < 1 || lineNumber > page.size()) {
            sender.sendMessage(ColorUtil.colorize("&c行号必须在 1 到 " + page.size() + " 之间！"));
            return true;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        page.setLine(lineNumber - 1, content);
        hologram.save();

        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("edit.line-set", "line", String.valueOf(lineNumber))));
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
                    for (int i = 1; i <= page.size(); i++) lines.add(String.valueOf(i));
                    return lines.stream().filter(l -> l.startsWith(args[1])).collect(Collectors.toList());
                }
            }
        }
        
        // 为内容参数提供补全
        if (args.length >= 3) {
            String currentInput = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            return TabCompleteUtil.getLineContentCompletions(currentInput);
        }
        
        return new ArrayList<>();
    }
}
