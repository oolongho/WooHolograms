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
 * 移动全息图到玩家位置命令
 * /wh movehere <名称>
 * 
 * @author oolongho
 */
public class MoveHereCommand extends Subcommand {

    private final WooHolograms plugin;

    public MoveHereCommand(WooHolograms plugin) {
        super("movehere", "将全息图移动到你的位置", "/wh movehere <名称>", "wooholograms.move", Arrays.asList("mv"));
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        hologram.teleport(player.getLocation());
        hologram.save();
        
        player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("move.success", "name", name)));
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
