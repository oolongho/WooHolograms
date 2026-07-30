package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends Subcommand {

    private final WooHolograms plugin;

    public ReloadCommand(WooHolograms plugin) {
        super("reload", "cmd.desc-reload", "cmd.usage-reload", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().reload();
        plugin.getMessages().reload();
        plugin.getStorage().reload();
        plugin.getHologramManager().reload();

        plugin.getMessages().send(sender, "cmd.reload-success");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
