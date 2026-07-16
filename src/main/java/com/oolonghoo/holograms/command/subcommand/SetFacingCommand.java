package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetFacingCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetFacingCommand(WooHolograms plugin) {
        super("setfacing", "设置全息图朝向", "/wh setfacing <名称> <模式> [角度]", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            sender.sendMessage(ColorUtil.colorize("&7模式: fixed_angle(固定角度), horizontal(水平跟随), vertical(垂直跟随), all(完全跟随)"));
            sender.sendMessage(ColorUtil.colorize("&7角度: 仅 fixed_angle 模式需要，0-360度"));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        Billboard billboard = Billboard.fromId(args[1].toLowerCase());

        if (billboard == Billboard.FIXED_ANGLE && args.length > 2) {
            try {
                float facing = Float.parseFloat(args[2]);
                hologram.setFacing(facing);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c角度必须是数字！"));
                return true;
            }
        }

        hologram.setBillboard(billboard);
        hologram.save();

        String modeDisplay = billboard.getDisplayName();
        if (billboard == Billboard.FIXED_ANGLE) {
            modeDisplay += " (" + hologram.getFacing() + "度)";
        }
        sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的朝向设置为 " + modeDisplay + "！"));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("fixed_angle", "horizontal", "vertical", "all").stream()
                    .filter(m -> m.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[1].equalsIgnoreCase("fixed_angle")) {
            return Arrays.asList("0", "45", "90", "180", "270", "360").stream()
                    .filter(a -> a.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
