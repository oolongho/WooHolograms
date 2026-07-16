package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteActionCommand extends Subcommand {

    private final WooHolograms plugin;

    public DeleteActionCommand(WooHolograms plugin) {
        super("deleteaction", "删除点击动作", "/wh deleteaction <名称> <页码> <点击类型> <索引>", "wooholograms.edit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
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
            int actionIndex = Integer.parseInt(args[3]) - 1;

            HologramPage page = hologram.getPage(pageIndex);
            if (page != null) {
                List<Action> actions = page.getActions(clickType);
                if (actionIndex < 0 || actionIndex >= actions.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的动作索引！"));
                    return true;
                }

                page.removeAction(clickType, actionIndex);
                hologram.save();
                sender.sendMessage(ColorUtil.colorize("&a已删除第 " + (actionIndex + 1) + " 个动作！"));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.colorize("&c页码和索引必须是数字！"));
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
