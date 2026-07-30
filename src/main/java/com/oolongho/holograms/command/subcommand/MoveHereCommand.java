package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 移动全息图到玩家位置命令
 * /wh movehere <名称>
 * 
 */
public class MoveHereCommand extends Subcommand {

    private final WooHolograms plugin;

    public MoveHereCommand(WooHolograms plugin) {
        super("movehere", "cmd.desc-movehere", "cmd.usage-movehere", "wooholograms.move");
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            plugin.getMessages().send(player, "move.usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(player, "general.hologram-not-found", "name", name);
            return true;
        }

        hologram.teleport(player.getLocation());
        hologram.save();

        plugin.getMessages().send(player, "move.success", "name", name);
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
