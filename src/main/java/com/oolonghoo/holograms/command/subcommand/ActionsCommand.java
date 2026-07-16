package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionsCommand extends Subcommand {

    private final WooHolograms plugin;

    public ActionsCommand(WooHolograms plugin) {
        super("actions", "列出点击动作", "/wh actions <名称> <页码> <点击类型>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
            return true;
        }

        try {
            int pageIndex = Integer.parseInt(args[1]) - 1;
            if (pageIndex < 0 || pageIndex >= hologram.getPageCount()) {
                sender.sendMessage(ColorUtil.colorize("&c无效的页码！"));
                return true;
            }

            ClickType clickType = ClickType.fromId(args[2]);
            HologramPage page = hologram.getPage(pageIndex);

            if (page != null) {
                List<Action> actions = page.getActions(clickType);
                sender.sendMessage(ColorUtil.colorize("&e========== &6" + name + " 第" + (pageIndex + 1) + "页 " + clickType.getDescription() + "动作 &e=========="));

                if (actions.isEmpty()) {
                    sender.sendMessage(ColorUtil.colorize("&7没有动作"));
                } else {
                    for (int i = 0; i < actions.size(); i++) {
                        Action action = actions.get(i);
                        sender.sendMessage(ColorUtil.colorize("&e" + (i + 1) + ". &f" + action.toString()));
                    }
                }
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c页码必须是数字！"));
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
