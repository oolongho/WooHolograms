package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends Subcommand {

    private final WooHolograms plugin;

    public ReloadCommand(WooHolograms plugin) {
        super("reload", "重新加载插件配置", "/wh reload", "wooholograms.admin");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfigManager().reload();
        plugin.getMessages().reload();
        plugin.getStorage().reload();
        plugin.getHologramManager().reload();

        sender.sendMessage(ColorUtil.colorize("&a配置已重新加载！"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
