package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelpCommand extends Subcommand {

    private final WooHolograms plugin;
    private final Map<String, Subcommand> subcommandMap;

    public HelpCommand(WooHolograms plugin, Map<String, Subcommand> subcommandMap) {
        super("help", "cmd.desc-help", "cmd.usage-help", null);
        this.plugin = plugin;
        this.subcommandMap = subcommandMap;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getMessages().send(sender, "cmd.help-header");
        for (Subcommand sub : subcommandMap.values()) {
            plugin.getMessages().send(sender, "cmd.help-line",
                    "usage", plugin.getMessages().getRaw(sub.getUsageKey()),
                    "description", plugin.getMessages().getRaw(sub.getDescriptionKey()));
        }
        plugin.getMessages().send(sender, "cmd.help-footer");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
