package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 移动全息图到指定位置命令
 * /wh moveto <名称> <x> <y> <z> [世界]
 * 
 * @author oolongho
 */
public class MoveToCommand extends Subcommand {

    private final WooHolograms plugin;

    public MoveToCommand(WooHolograms plugin) {
        super("moveto", "将全息图移动到指定位置", "/wh moveto <名称> <x> <y> <z> [世界]", "wooholograms.move", Arrays.asList("mvt"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.invalid-number")));
            return true;
        }

        World world;
        if (args.length > 4) {
            world = Bukkit.getWorld(args[4]);
            if (world == null) {
                sender.sendMessage(ColorUtil.colorize("&c世界 " + args[4] + " 不存在！"));
                return true;
            }
        } else {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                world = hologram.getLocation().getWorld();
            }
        }

        Location newLocation = new Location(world, x, y, z);
        hologram.teleport(newLocation);
        hologram.save();

        sender.sendMessage(ColorUtil.colorize("&a已将全息图 " + name + " 移动到 " + world.getName() + " (" + x + ", " + y + ", " + z + ")"));
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
