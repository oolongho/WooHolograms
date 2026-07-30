package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddActionCommand extends Subcommand {

    private final WooHolograms plugin;

    public AddActionCommand(WooHolograms plugin) {
        super("addaction", "cmd.desc-addaction", "cmd.usage-addaction", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.getMessages().send(sender, "action.usage");
            plugin.getMessages().send(sender, "action.click-types-help");
            plugin.getMessages().send(sender, "action.action-types-help");
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
            ActionType actionType = ActionType.getByName(args[3]);

            if (actionType == null) {
                plugin.getMessages().send(sender, "action.invalid-action-type");
                return true;
            }

            String data = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
            Action action = new Action(actionType, data, clickType);

            HologramPage page = hologram.getPage(pageIndex);
            if (page != null) {
                page.addAction(clickType, action);
                hologram.save();
                plugin.getMessages().send(sender, "action.added-to-page", "page", String.valueOf(pageIndex + 1));
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
        } else if (args.length == 4) {
            return Arrays.asList("message", "command", "console", "sound", "teleport", "connect", "next_page", "prev_page", "page").stream()
                    .filter(a -> a.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
