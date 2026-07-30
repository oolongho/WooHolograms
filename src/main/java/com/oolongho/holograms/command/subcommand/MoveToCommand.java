package com.oolongho.holograms.command.subcommand;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;

/**
 * 移动全息图到指定位置命令
 * /wh moveto <名称> <x> <y> <z> [世界]
 * 
 */
public class MoveToCommand extends Subcommand {

    private final WooHolograms plugin;

    public MoveToCommand(WooHolograms plugin) {
        super("moveto", "cmd.desc-moveto", "cmd.usage-moveto", "wooholograms.move");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.getMessages().send(sender, "move.to-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "general.invalid-number");
            return true;
        }

        World world;
        if (args.length > 4) {
            world = Bukkit.getWorld(args[4]);
            if (world == null) {
                plugin.getMessages().send(sender, "general.world-not-found", "world", args[4]);
                return true;
            }
        } else if (sender instanceof Player player) {
            world = player.getWorld();
        } else {
            world = hologram.getLocation().getWorld();
        }

        if (world == null) {
            plugin.getMessages().send(sender, "general.world-undefined");
            return true;
        }

        final World finalWorld = world;
        Location newLocation = new Location(finalWorld, x, y, z);
        hologram.teleport(newLocation);
        hologram.save();

        plugin.getMessages().send(sender, "move.to-success",
                "name", name,
                "world", finalWorld.getName(),
                "x", String.valueOf(x),
                "y", String.valueOf(y),
                "z", String.valueOf(z));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 5) {
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[4].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
