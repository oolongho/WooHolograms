package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;

/**
 * 数据导入 GUI
 * 从 HolographicDisplays、CMI 或 DecentHolograms 导入全息图数据
 */
public class ConvertGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;

    public ConvertGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager) {
        super("convert", plugin.getMessages().get("gui.title-convert"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;

        render();
    }

    private void render() {
        clearButtons();

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-list"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        // HolographicDisplays 导入
        setButton(11, GuiButton.builder(Material.OAK_SIGN)
                .name("<white>HolographicDisplays")
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.convert.hd-lore"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-import")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "wh convert hd");
                })
                .build());

        // CMI 导入
        setButton(13, GuiButton.builder(Material.OAK_SIGN)
                .name("<white>CMI")
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.convert.cmi-lore"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-import")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "wh convert cmi");
                })
                .build());

        // DecentHolograms 导入
        File dhDir = new File(plugin.getDataFolder().getParent(), "DecentHolograms/holograms");
        boolean dhExists = dhDir.exists() && dhDir.isDirectory();

        setButton(15, GuiButton.builder(Material.OAK_SIGN)
                .name("<white>DecentHolograms")
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.convert.dh-auto"),
                        dhExists ? plugin.getMessages().getString("gui.convert.dh-status-found")
                                 : plugin.getMessages().getString("gui.convert.dh-status-not-found"),
                        "",
                        dhExists ? plugin.getMessages().getString("gui.convert.dh-imported")
                                 : plugin.getMessages().getString("gui.convert.dh-not-found-hint")
                ))
                .build());

        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 27; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
}
