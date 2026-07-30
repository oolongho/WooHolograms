package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.TabCompleteUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设置行命令
 * /wh setline <名称> <行号> <内容>
 * 
 */
public class SetLineCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetLineCommand(WooHolograms plugin) {
        super("setline", "cmd.desc-setline", "cmd.usage-setline", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "cmd.setline-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        int lineNumber;
        try {
            lineNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "general.invalid-number");
            return true;
        }

        HologramPage page = hologram.getPage(0);
        if (page == null || page.size() == 0) {
            plugin.getMessages().send(sender, "general.no-content");
            return true;
        }

        if (lineNumber < 1 || lineNumber > page.size()) {
            plugin.getMessages().send(sender, "general.line-out-of-range", "max", String.valueOf(page.size()));
            return true;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        page.setLine(lineNumber - 1, content);
        hologram.save();

        plugin.getMessages().send(sender, "edit.line-set", "line", String.valueOf(lineNumber));
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
