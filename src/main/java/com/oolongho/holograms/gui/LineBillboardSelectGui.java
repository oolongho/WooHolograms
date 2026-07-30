package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LineBillboardSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final int lineIndex;

    public LineBillboardSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                                   String hologramName, int pageIndex, int lineIndex) {
        super("line_billboard_select", plugin.getMessages().get("gui.title-line-billboard-select"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.lineIndex = lineIndex;

        render();
    }

    private void render() {
        clearButtons();

        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            setButton(13, GuiButton.builder(Material.BARRIER)
                    .name(plugin.getMessages().getString("gui.btn-hologram-not-exists"))
                    .lore(Arrays.asList("", plugin.getMessages().getString("gui.lore-hologram-deleted"), "", plugin.getMessages().getString("gui.lore-click-back-list")))
                    .onClick(context -> guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0)))
                    .build());
            return;
        }

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex < 0 || lineIndex >= page.size()) {
            setButton(13, GuiButton.builder(Material.BARRIER)
                    .name(plugin.getMessages().getString("gui.btn-line-not-exists"))
                    .lore(Arrays.asList("", plugin.getMessages().getString("gui.lore-line-deleted"), "", plugin.getMessages().getString("gui.lore-click-back-detail")))
                    .onClick(context -> guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex)))
                    .build());
            return;
        }

        HologramLine line = page.getLine(lineIndex);
        Billboard currentBillboard = line.getBillboard() != null ? line.getBillboard() : hologram.getBillboard();
        boolean isOverriding = line.getBillboard() != null;

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList("", plugin.getMessages().getString("gui.lore-back-edit"), "", plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex)))
                .build());

        String currentDisplay = plugin.getMessages().getRaw(currentBillboard.getDisplayNameKey());
        setButton(4, GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.line-billboard.current-mode"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.line-billboard.current-display", "mode", currentDisplay),
                        isOverriding ? plugin.getMessages().getString("gui.line-billboard.override-line") : plugin.getMessages().getString("gui.line-billboard.follow-overall"),
                        ""
                ))
                .build());

        setButton(10, GuiButton.builder(Material.STONE_BUTTON)
                .name(plugin.getMessages().getString("gui.line-billboard.fixed-angle"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-billboard.fixed-angle-desc"),
                        "",
                        currentBillboard == Billboard.FIXED_ANGLE && isOverriding ? plugin.getMessages().getString("gui.lore-current-selected") : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-billboard-angle"),
                            ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    float angle = Float.parseFloat(input);
                                    setLineBillboard(player, Billboard.FIXED_ANGLE, angle);
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                    guiManager.openGui(player, new LineBillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                                }
                            });
                })
                .build());

        setButton(12, GuiButton.builder(Material.END_ROD)
                .name(plugin.getMessages().getString("gui.line-billboard.vertical"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-billboard.vertical-desc"),
                        "",
                        currentBillboard == Billboard.VERTICAL && isOverriding ? plugin.getMessages().getString("gui.lore-current-selected") : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> setLineBillboard(context.getPlayer(), Billboard.VERTICAL, 0))
                .build());

        setButton(14, GuiButton.builder(Material.RAIL)
                .name(plugin.getMessages().getString("gui.line-billboard.horizontal"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-billboard.horizontal-desc"),
                        "",
                        currentBillboard == Billboard.HORIZONTAL && isOverriding ? plugin.getMessages().getString("gui.lore-current-selected") : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> setLineBillboard(context.getPlayer(), Billboard.HORIZONTAL, 0))
                .build());

        setButton(16, GuiButton.builder(Material.END_CRYSTAL)
                .name(plugin.getMessages().getString("gui.line-billboard.center"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-billboard.center-desc"),
                        "",
                        currentBillboard == Billboard.CENTER && isOverriding ? plugin.getMessages().getString("gui.lore-current-selected") : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> setLineBillboard(context.getPlayer(), Billboard.CENTER, 0))
                .build());

        setButton(22, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.line-billboard.reset"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-billboard.reset-desc-1"),
                        plugin.getMessages().getString("gui.line-billboard.reset-desc-2"),
                        "",
                        isOverriding ? plugin.getMessages().getString("gui.lore-click-reset") : plugin.getMessages().getString("gui.line-billboard.already-follow")
                ))
                .onClick(context -> {
                    if (!isOverriding) return;
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        HologramPage p = h.getPage(pageIndex);
                        if (p != null && lineIndex < p.size()) {
                            HologramLine l = p.getLine(lineIndex);
                            if (l != null) {
                                l.setBillboard(null);
                                h.save();
                                h.showToNearby();
                                plugin.getMessages().send(player, "gui.msg-facing-reset");
                            }
                        }
                    }
                    guiManager.openGui(player, new LineBillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        fillBackground();
    }

    private void setLineBillboard(Player player, Billboard billboard, float angle) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h != null) {
            HologramPage p = h.getPage(pageIndex);
            if (p != null && lineIndex < p.size()) {
                HologramLine l = p.getLine(lineIndex);
                if (l != null) {
                    l.setBillboard(billboard);
                    if (billboard == Billboard.FIXED_ANGLE) {
                        l.setCustomYaw(angle);
                    }
                    h.save();
                    h.showToNearby();
                    plugin.getMessages().send(player, "gui.msg-line-billboard-set", "mode", plugin.getMessages().getRaw(billboard.getDisplayNameKey()));
                }
            }
        }
        guiManager.openGui(player, new LineBillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 23, 24, 25, 26};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
