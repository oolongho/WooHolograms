package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
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
        super("create", "创建一个新的全息图", "/wh create <名称> [内容]", "wooholograms.create", Arrays.asList("c", "new"));
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("create.usage")));
            return true;
        }

        String name = args[0];

        if (plugin.getHologramManager().containsHologram(name)) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-exists", "name", name)));
            return true;
        }

        Location location = player.getLocation();
        Hologram hologram = plugin.getHologramManager().createHologram(name, location);

        if (hologram == null) {
            player.sendMessage(ColorUtil.colorize("&c创建全息图失败！"));
            return true;
        }

        if (args.length > 1) {
            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            HologramPage page = hologram.getPage(0);
            if (page != null) {
                page.addLine(text);
            }
        }

        player.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("create.success", "name", name)));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
