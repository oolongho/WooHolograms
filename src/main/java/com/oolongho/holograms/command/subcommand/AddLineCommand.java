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
 * 添加行命令
 * /wh addline <名称> <内容>
 * 
 */
public class AddLineCommand extends Subcommand {

    private final WooHolograms plugin;

    public AddLineCommand(WooHolograms plugin) {
        super("addline", "cmd.desc-addline", "cmd.usage-addline", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "line.usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        HologramPage page = hologram.getPage(0);
        if (page == null) {
            page = hologram.addPage();
        }

        page.addLine(content);
        hologram.save();

        plugin.getMessages().send(sender, "edit.line-added");
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
