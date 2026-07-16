package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommand extends Subcommand {

    private final WooHolograms plugin;
    private final Map<String, Subcommand> subcommandMap;

    public HelpCommand(WooHolograms plugin, Map<String, Subcommand> subcommandMap) {
        super("help", "显示帮助信息", "/wh help", null);
        this.plugin = plugin;
        this.subcommandMap = subcommandMap;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(ColorUtil.colorize("&e========== &6WooHolograms 帮助 &e=========="));
        for (Subcommand sub : subcommandMap.values()) {
            sender.sendMessage(ColorUtil.colorize("&e" + sub.getUsage() + " &7- " + sub.getDescription()));
        }
        sender.sendMessage(ColorUtil.colorize("&e===================================="));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
