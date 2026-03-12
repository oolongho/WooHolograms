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
 * 添加行命令
 * /wh addline <名称> <内容>
 * 
 */
public class AddLineCommand extends Subcommand {

    private final WooHolograms plugin;

    public AddLineCommand(WooHolograms plugin) {
        super("addline", "向全息图添加一行", "/wh addline <名称> <内容>", "wooholograms.edit", Arrays.asList("al"));
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

        String content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        HologramPage page = hologram.getPage(0);
        if (page == null) {
            page = hologram.addPage();
        }
        
        page.addLine(content);
        hologram.save();

        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("edit.line-added")));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // 为内容参数提供补全
        if (args.length >= 2) {
            String currentInput = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            return TabCompleteUtil.getLineContentCompletions(currentInput);
        }
        
        return new ArrayList<>();
    }
}
