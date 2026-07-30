package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetFacingCommand extends Subcommand {

    private final WooHolograms plugin;

    public SetFacingCommand(WooHolograms plugin) {
        super("setfacing", "cmd.desc-setfacing", "cmd.usage-setfacing", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().send(sender, "cmd.setfacing-usage");
            plugin.getMessages().send(sender, "cmd.setfacing-help-mode");
            plugin.getMessages().send(sender, "cmd.setfacing-help-yaw");
            plugin.getMessages().send(sender, "cmd.setfacing-help-pitch");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        Billboard billboard = Billboard.fromId(args[1].toLowerCase());

        if (billboard == Billboard.FIXED_ANGLE && args.length > 2) {
            try {
                float yaw = Float.parseFloat(args[2]);
                hologram.setFacing(yaw);
            } catch (NumberFormatException e) {
                plugin.getMessages().send(sender, "cmd.setfacing-yaw-number");
                return true;
            }
            // 可选 pitch 参数
            if (args.length > 3) {
                try {
                    float pitch = Float.parseFloat(args[3]);
                    if (pitch < -90 || pitch > 90) {
                        plugin.getMessages().send(sender, "gui.msg-pitch-range");
                        return true;
                    }
                    hologram.setPitch(pitch);
                } catch (NumberFormatException e) {
                    plugin.getMessages().send(sender, "cmd.setfacing-pitch-number");
                    return true;
                }
            }
            // args.length == 3 时保留原 pitch（不调用 setPitch）
        }

        hologram.setBillboard(billboard);
        hologram.save();

        String modeDisplay = plugin.getMessages().getRaw(billboard.getDisplayNameKey());
        if (billboard == Billboard.FIXED_ANGLE) {
            if (hologram.getPitch() != null) {
                modeDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw-pitch",
                        "yaw", String.valueOf(hologram.getFacing()),
                        "pitch", String.valueOf(hologram.getPitch()));
            } else {
                modeDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw",
                        "yaw", String.valueOf(hologram.getFacing()));
            }
        }
        plugin.getMessages().send(sender, "cmd.setfacing-success", "name", name, "mode", modeDisplay);

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
        } else if (args.length == 4 && args[1].equalsIgnoreCase("fixed_angle")) {
            return Arrays.asList("-90", "-45", "0", "45", "90").stream()
                    .filter(a -> a.startsWith(args[3]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
