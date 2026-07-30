package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.TextAlignment;
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
        super("alignment_select", plugin.getMessages().get("gui.title-alignment-select"), 27);
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
                    .name(plugin.getMessages().getString("gui.btn-hologram-not-exists"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.lore-hologram-deleted"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-back-list")
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        TextAlignment currentAlignment = hologram.getAlignment();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-detail"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(4, GuiButton.builder(Material.REPEATER)
                .name(plugin.getMessages().getString("gui.alignment.current"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.alignment.current-value", "value", plugin.getMessages().getRaw(currentAlignment.getDisplayNameKey())),
                        ""
                ))
                .build());

        setButton(11, GuiButton.builder(Material.ARROW)
                .name(plugin.getMessages().getString("gui.alignment.left"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.alignment.left-lore"),
                        "",
                        currentAlignment == TextAlignment.LEFT
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setAlignment(player, TextAlignment.LEFT);
                })
                .build());

        setButton(13, GuiButton.builder(Material.END_CRYSTAL)
                .name(plugin.getMessages().getString("gui.alignment.center"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.alignment.center-lore"),
                        "",
                        currentAlignment == TextAlignment.CENTER
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setAlignment(player, TextAlignment.CENTER);
                })
                .build());

        setButton(15, GuiButton.builder(Material.ARROW)
                .name(plugin.getMessages().getString("gui.alignment.right"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.alignment.right-lore"),
                        "",
                        currentAlignment == TextAlignment.RIGHT
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
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
            plugin.getMessages().send(player, "gui.msg-alignment-set", "alignment", plugin.getMessages().getRaw(alignment.getDisplayNameKey()));
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
