package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OffsetCommand extends Subcommand {

    private final WooHolograms plugin;

    public OffsetCommand(WooHolograms plugin) {
        super("offset", "cmd.desc-offset", "cmd.usage-offset", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 5) {
            plugin.getMessages().send(sender, "cmd.offset-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-exists", "name", name);
            return true;
        }

        try {
            int lineNumber = Integer.parseInt(args[1]);
            double offsetX = Double.parseDouble(args[2]);
            double offsetY = Double.parseDouble(args[3]);
            double offsetZ = Double.parseDouble(args[4]);

            HologramPage page = hologram.getPage(0);
            if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                plugin.getMessages().send(sender, "general.line-invalid");
                return true;
            }

            HologramLine line = page.getLine(lineNumber - 1);
            if (line != null) {
                line.setOffset(offsetX, offsetY, offsetZ);
                hologram.save();

                plugin.getMessages().send(sender, "cmd.offset-success",
                        "line", String.valueOf(lineNumber),
                        "ox", String.valueOf(offsetX),
                        "oy", String.valueOf(offsetY),
                        "oz", String.valueOf(offsetZ));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "cmd.offset-must-be-number");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String hologramName = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
            if (hologram != null && hologram.getPage(0) != null) {
                int lineCount = hologram.getPage(0).size();
                List<String> lineNumbers = new ArrayList<>();
                for (int i = 1; i <= lineCount; i++) {
                    lineNumbers.add(String.valueOf(i));
                }
                return lineNumbers.stream()
                        .filter(n -> n.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length >= 3 && args.length <= 5) {
            return Arrays.asList("0", "0.25", "0.5", "1", "-0.25", "-0.5", "-1").stream()
                    .filter(v -> v.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
