package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.util.ColorUtil;
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
        super("convert", ColorUtil.colorize("&8数据导入"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;

        render();
    }

    private void render() {
        clearButtons();

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回全息图列表", "", "&e点击返回"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        // HolographicDisplays 导入
        setButton(11, GuiButton.builder(Material.OAK_SIGN)
                .name("&fHolographicDisplays")
                .lore(Arrays.asList(
                        "&7从 HolographicDisplays 导入数据",
                        "",
                        "&e点击导入"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "wh convert hd");
                })
                .build());

        // CMI 导入
        setButton(13, GuiButton.builder(Material.OAK_SIGN)
                .name("&fCMI")
                .lore(Arrays.asList(
                        "&7从 CMI 导入数据",
                        "",
                        "&e点击导入"
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
                .name("&fDecentHolograms")
                .lore(Arrays.asList(
                        "&7DecentHolograms 数据会自动导入",
                        "&7状态: " + (dhExists ? "&a检测到数据目录" : "&c未检测到数据目录"),
                        "",
                        dhExists ? "&a插件启动时已自动导入" : "&7请确保 DecentHolograms 数据目录存在"
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
