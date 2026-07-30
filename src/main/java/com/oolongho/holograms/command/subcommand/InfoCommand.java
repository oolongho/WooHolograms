package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.LocationUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
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
        super("info", "cmd.desc-info", "cmd.usage-info", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessages().send(sender, "cmd.info-usage");
            return true;
        }

        String name = args[0];
        Hologram hologram = plugin.getHologramManager().getHologram(name);

        if (hologram == null) {
            plugin.getMessages().send(sender, "general.hologram-not-found", "name", name);
            return true;
        }

        plugin.getMessages().send(sender, "cmd.info-header");
        plugin.getMessages().send(sender, "cmd.info-name", "name", hologram.getName());
        plugin.getMessages().send(sender, "cmd.info-status",
                "state", hologram.isEnabled()
                        ? plugin.getMessages().getRaw("cmd.info-state-enabled")
                        : plugin.getMessages().getRaw("cmd.info-state-disabled"));
        plugin.getMessages().send(sender, "cmd.info-location", "location", LocationUtil.format(hologram.getLocation()));
        plugin.getMessages().send(sender, "cmd.info-pages", "pages", String.valueOf(hologram.getPageCount()));
        plugin.getMessages().send(sender, "cmd.info-display-range", "range", String.valueOf(hologram.getDisplayRange()));
        plugin.getMessages().send(sender, "cmd.info-update-range", "range", String.valueOf(hologram.getUpdateRange()));

        if (hologram.getPermission() != null && !hologram.getPermission().isEmpty()) {
            plugin.getMessages().send(sender, "cmd.info-permission", "permission", hologram.getPermission());
        }

        HologramPage page = hologram.getPage(0);
        if (page != null && page.size() > 0) {
            plugin.getMessages().send(sender, "cmd.info-content-title");
            int lineNum = 1;
            for (HologramLine line : page.getLines()) {
                String content = line.getContent();
                if (content.length() > 50) content = content.substring(0, 47) + "...";
                plugin.getMessages().send(sender, "cmd.info-content-line",
                        "index", String.valueOf(lineNum),
                        "content", content);
                lineNum++;
            }
        }

        plugin.getMessages().send(sender, "cmd.info-footer");
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
