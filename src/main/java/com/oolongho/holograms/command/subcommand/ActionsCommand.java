package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionsCommand extends Subcommand {

    private final WooHolograms plugin;

    public ActionsCommand(WooHolograms plugin) {
        super("actions", "cmd.desc-actions", "cmd.usage-actions", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessages().send(sender, "action.usage-actions");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        try {
            int pageIndex = Integer.parseInt(args[1]) - 1;
            if (pageIndex < 0 || pageIndex >= hologram.getPageCount()) {
                plugin.getMessages().send(sender, "general.invalid-page");
                return true;
            }

            ClickType clickType = ClickType.fromId(args[2]);
            HologramPage page = hologram.getPage(pageIndex);

            if (page != null) {
                List<Action> actions = page.getActions(clickType);
                plugin.getMessages().send(sender, "action.list-title",
                        "name", name,
                        "page", String.valueOf(pageIndex + 1),
                        "click", plugin.getMessages().getRaw(clickType.getDescriptionKey()));

                if (actions.isEmpty()) {
                    plugin.getMessages().send(sender, "action.list-empty");
                } else {
                    for (int i = 0; i < actions.size(); i++) {
                        Action action = actions.get(i);
                        plugin.getMessages().send(sender, "action.list-format",
                                "index", String.valueOf(i + 1),
                                "action", action.toString());
                    }
                }
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().send(sender, "general.must-be-number");
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
            if (hologram != null) {
                int pageCount = hologram.getPageCount();
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= pageCount; i++) {
                    pages.add(String.valueOf(i));
                }
                return pages.stream()
                        .filter(p -> p.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            return Arrays.asList("left", "right", "shift_left", "shift_right").stream()
                    .filter(c -> c.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
