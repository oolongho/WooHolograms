package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import com.oolonghoo.holograms.util.LocationUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查看全息图信息命令
 * /wh info <名称>
 * 
 */
public class InfoCommand extends Subcommand {

    private final WooHolograms plugin;

    public InfoCommand(WooHolograms plugin) {
        super("info", "查看全息图详细信息", "/wh info <名称>", "wooholograms.admin", Arrays.asList("i"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("general.hologram-not-found", "name", name)));
            return true;
        }

        sender.sendMessage(ColorUtil.colorize("&e========== &6全息图信息 &e=========="));
        sender.sendMessage(ColorUtil.colorize("&e名称: &f" + hologram.getName()));
        sender.sendMessage(ColorUtil.colorize("&e状态: " + (hologram.isEnabled() ? "&a启用" : "&c禁用")));
        sender.sendMessage(ColorUtil.colorize("&e位置: &f" + LocationUtil.format(hologram.getLocation())));
        sender.sendMessage(ColorUtil.colorize("&e页数: &f" + hologram.getPageCount()));
        sender.sendMessage(ColorUtil.colorize("&e显示范围: &f" + hologram.getDisplayRange()));
        sender.sendMessage(ColorUtil.colorize("&e更新范围: &f" + hologram.getUpdateRange()));
        
        if (hologram.getPermission() != null && !hologram.getPermission().isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&e权限: &f" + hologram.getPermission()));
        }

        HologramPage page = hologram.getPage(0);
        if (page != null && page.size() > 0) {
            sender.sendMessage(ColorUtil.colorize("&e内容 (第1页):"));
            int lineNum = 1;
            for (HologramLine line : page.getLines()) {
                String content = line.getContent();
                if (content.length() > 50) content = content.substring(0, 47) + "...";
                sender.sendMessage(ColorUtil.colorize("  &7" + lineNum + ". &f" + content));
                lineNum++;
            }
        }

        sender.sendMessage(ColorUtil.colorize("&e===================================="));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getHologramManager().getHologramNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
