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
 * 删除全息图命令
 * /wh delete <名称>
 * 
 */
public class DeleteCommand extends Subcommand {

    private final WooHolograms plugin;

    public DeleteCommand(WooHolograms plugin) {
        super("delete", "删除一个全息图", "/wh delete <名称>", "wooholograms.delete", Arrays.asList("del", "remove", "rm"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("delete.usage")));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        plugin.getHologramManager().deleteHologram(name);
        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("delete.success", "name", name)));
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
