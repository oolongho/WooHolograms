package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 创建全息图命令
 * /wh create <名称>
 * 
 */
public class CreateCommand extends Subcommand {

    private final WooHolograms plugin;

    public CreateCommand(WooHolograms plugin) {
        super("create", "cmd.desc-create", "cmd.usage-create", "wooholograms.create");
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            plugin.getMessages().send(player, "create.usage");
            return true;
        }

        String name = args[0];

        if (!name.matches("[\\w\\-\\p{L}]+")) {
            plugin.getMessages().send(player, "create.invalid-name", "name", name);
            return true;
        }

        if (plugin.getHologramManager().containsHologram(name)) {
            plugin.getMessages().send(player, "general.hologram-exists", "name", name);
            return true;
        }

        Location location = player.getLocation();
        Hologram hologram = plugin.getHologramManager().createHologram(name, location);

        if (hologram == null) {
            plugin.getMessages().send(player, "create.failed");
            return true;
        }

        if (args.length > 1) {
            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            HologramPage page = hologram.getPage(0);
            if (page != null) {
                page.addLine(text);
            }
        }

        plugin.getMessages().send(player, "create.success", "name", name);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
