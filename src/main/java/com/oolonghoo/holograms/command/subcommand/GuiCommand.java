package com.oolonghoo.holograms.command.subcommand;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.command.Subcommand;
import com.oolonghoo.holograms.gui.ChatInputManager;
import com.oolonghoo.holograms.gui.GuiManager;
import com.oolonghoo.holograms.gui.HologramDetailGui;
import com.oolonghoo.holograms.gui.HologramListGui;
import com.oolonghoo.holograms.gui.HologramListGui.SortType;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiCommand extends Subcommand {

    private final WooHolograms plugin;

    public GuiCommand(WooHolograms plugin) {
        super("gui", "打开GUI管理界面", "/wh gui [名称]", "wooholograms.admin");
        this.plugin = plugin;
        setPlayerOnly(true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        GuiManager guiManager = plugin.getGuiManager();
        ChatInputManager chatInputManager = plugin.getChatInputManager();

        if (args.length >= 1) {
            String hologramName = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(hologramName);

            if (hologram == null) {
                player.sendMessage(ColorUtil.colorize("&c全息图 " + hologramName + " 不存在！"));
                return true;
            }

            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
        } else {
            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0, SortType.NAME, player));
        }

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
