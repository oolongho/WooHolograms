package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.TextAlignment;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class AlignmentSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int currentPageIndex;

    public AlignmentSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                              String hologramName, int currentPageIndex) {
        super("alignment_select", ColorUtil.colorize("&8对齐设置"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.currentPageIndex = currentPageIndex;

        render();
    }

    private void render() {
        clearButtons();

        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            setButton(13, GuiButton.builder(Material.BARRIER)
                    .name("&f全息图不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该全息图已被删除",
                            "",
                            "&e点击返回列表"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        TextAlignment currentAlignment = hologram.getAlignment();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回全息图详情",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(4, GuiButton.builder(Material.REPEATER)
                .name("&f当前对齐方式")
                .lore(Arrays.asList(
                        "",
                        "&7" + currentAlignment.getDisplayName(),
                        ""
                ))
                .build());

        setButton(11, GuiButton.builder(Material.ARROW)
                .name("&f左对齐")
                .lore(Arrays.asList(
                        "&7文本靠左显示",
                        "",
                        currentAlignment == TextAlignment.LEFT ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setAlignment(player, TextAlignment.LEFT);
                })
                .build());

        setButton(13, GuiButton.builder(Material.END_CRYSTAL)
                .name("&f居中")
                .lore(Arrays.asList(
                        "&7文本居中显示",
                        "",
                        currentAlignment == TextAlignment.CENTER ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setAlignment(player, TextAlignment.CENTER);
                })
                .build());

        setButton(15, GuiButton.builder(Material.ARROW)
                .name("&f右对齐")
                .lore(Arrays.asList(
                        "&7文本靠右显示",
                        "",
                        currentAlignment == TextAlignment.RIGHT ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setAlignment(player, TextAlignment.RIGHT);
                })
                .build());

        fillBackground();
    }

    private void setAlignment(Player player, TextAlignment alignment) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h != null) {
            h.setAlignment(alignment);
            h.save();
            h.refreshAllViewers();
            player.sendMessage(ColorUtil.colorize("&a已设置对齐方式为 " + alignment.getDisplayName() + "！"));
        }
        guiManager.openGui(player, new AlignmentSelectGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
