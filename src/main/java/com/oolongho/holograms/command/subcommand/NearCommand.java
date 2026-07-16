package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NearCommand extends Subcommand {

    private final WooHolograms plugin;

    public NearCommand(WooHolograms plugin) {
        super("near", "查找附近的全息图", "/wh near [范围]", "wooholograms.use");
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        int range = 50;
        if (args.length >= 1) {
            try {
                range = Integer.parseInt(args[0]);
                if (range <= 0) {
                    player.sendMessage(ColorUtil.colorize("&c范围必须是正整数！"));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ColorUtil.colorize("&c无效的范围！"));
                return true;
            }
        }

        Location playerLoc = player.getLocation();
        List<Map.Entry<Hologram, Double>> nearbyHolograms = new ArrayList<>();

        for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
            Location holoLoc = hologram.getLocation();
            if (holoLoc == null || holoLoc.getWorld() == null) {
                continue;
            }
            if (!holoLoc.getWorld().equals(playerLoc.getWorld())) {
                continue;
            }
            double distance = playerLoc.distance(holoLoc);
            if (distance <= range) {
                nearbyHolograms.add(new AbstractMap.SimpleEntry<>(hologram, distance));
            }
        }

        if (nearbyHolograms.isEmpty()) {
            player.sendMessage(ColorUtil.colorize("&e附近 " + range + " 格内没有全息图。"));
            return true;
        }

        nearbyHolograms.sort(Map.Entry.comparingByValue());

        player.sendMessage(ColorUtil.colorize("&e========== &6附近全息图 (" + range + "格) &e=========="));
        for (Map.Entry<Hologram, Double> entry : nearbyHolograms) {
            Hologram hologram = entry.getKey();
            double distance = entry.getValue();
            Location loc = hologram.getLocation();
            player.sendMessage(ColorUtil.colorize("&e" + hologram.getName() +
                    " &7- 距离: " + String.format("%.1f", distance) + " 格" +
                    ", 位置: " + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ())));
        }
        player.sendMessage(ColorUtil.colorize("&e总计: &f" + nearbyHolograms.size() + " 个全息图"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> ranges = Arrays.asList("10", "25", "50", "100", "200");
            return ranges.stream()
                    .filter(r -> r.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
