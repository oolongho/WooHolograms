package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
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
        super("near", "cmd.desc-near", "cmd.usage-near", "wooholograms.use");
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
                    plugin.getMessages().send(player, "cmd.near-invalid-range");
                    return true;
                }
            } catch (NumberFormatException e) {
                plugin.getMessages().send(player, "cmd.near-bad-range");
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
            plugin.getMessages().send(player, "cmd.near-empty", "range", String.valueOf(range));
            return true;
        }

        nearbyHolograms.sort(Map.Entry.comparingByValue());

        plugin.getMessages().send(player, "cmd.near-header", "range", String.valueOf(range));
        for (Map.Entry<Hologram, Double> entry : nearbyHolograms) {
            Hologram hologram = entry.getKey();
            double distance = entry.getValue();
            Location loc = hologram.getLocation();
            plugin.getMessages().send(player, "cmd.near-item",
                    "name", hologram.getName(),
                    "distance", String.format("%.1f", distance),
                    "x", String.format("%.1f", loc.getX()),
                    "y", String.format("%.1f", loc.getY()),
                    "z", String.format("%.1f", loc.getZ()));
        }
        plugin.getMessages().send(player, "cmd.near-footer", "count", String.valueOf(nearbyHolograms.size()));
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
