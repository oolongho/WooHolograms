package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 朝向设置 GUI
 * 用于设置全息图的 Billboard 模式
 *
 * 
 */
public class BillboardSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;

    public BillboardSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                              String hologramName) {
        super("billboard_select", plugin.getMessages().get("gui.title-billboard-select"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;

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

        Billboard currentBillboard = hologram.getBillboard();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-detail"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());

        String currentDisplay = plugin.getMessages().getRaw(currentBillboard.getDisplayNameKey());
        if (currentBillboard == Billboard.FIXED_ANGLE) {
            if (hologram.getPitch() != null) {
                currentDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw-pitch",
                        "yaw", String.valueOf(hologram.getFacing()),
                        "pitch", String.valueOf(hologram.getPitch()));
            } else {
                currentDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw",
                        "yaw", String.valueOf(hologram.getFacing()));
            }
        }
        setButton(4, GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.billboard.current"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.billboard.current-value", "value", currentDisplay),
                        ""
                ))
                .build());

        setButton(10, GuiButton.builder(Material.STONE_BUTTON)
                .name(plugin.getMessages().getString("gui.billboard.fixed"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.billboard.fixed-lore"),
                        plugin.getMessages().getString("gui.billboard.fixed-require"),
                        "",
                        currentBillboard == Billboard.FIXED_ANGLE
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.billboard-angle"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        try {
                            String[] parts = input.trim().split("\\s+");
                            float yaw = Float.parseFloat(parts[0]);
                            Float pitch = null;
                            if (parts.length >= 2) {
                                pitch = Float.parseFloat(parts[1]);
                                if (pitch < -90 || pitch > 90) {
                                    plugin.getMessages().send(player, "gui.msg-pitch-range");
                                    guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                                    return;
                                }
                            }
                            setBillboard(player, Billboard.FIXED_ANGLE, yaw, pitch);
                        } catch (NumberFormatException e) {
                            plugin.getMessages().send(player, "gui.msg-angle-must-be-number");
                            guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                        }
                    });
                })
                .build());

        setButton(12, GuiButton.builder(Material.END_ROD)
                .name(plugin.getMessages().getString("gui.billboard.vertical"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.billboard.vertical-lore"),
                        plugin.getMessages().getString("gui.billboard.vertical-fixed"),
                        "",
                        currentBillboard == Billboard.VERTICAL
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.VERTICAL, 0, null);
                })
                .build());

        setButton(14, GuiButton.builder(Material.RAIL)
                .name(plugin.getMessages().getString("gui.billboard.horizontal"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.billboard.horizontal-lore"),
                        plugin.getMessages().getString("gui.billboard.horizontal-fixed"),
                        "",
                        currentBillboard == Billboard.HORIZONTAL
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.HORIZONTAL, 0, null);
                })
                .build());

        setButton(16, GuiButton.builder(Material.END_CRYSTAL)
                .name(plugin.getMessages().getString("gui.billboard.center"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.billboard.center-lore"),
                        plugin.getMessages().getString("gui.billboard.center-default"),
                        "",
                        currentBillboard == Billboard.CENTER
                                ? plugin.getMessages().getString("gui.lore-current-selected")
                                : plugin.getMessages().getString("gui.lore-click-select")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.CENTER, 0, null);
                })
                .build());

        fillBackground();
    }

    private void setBillboard(Player player, Billboard billboard, float facing, Float pitch) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h != null) {
            h.setBillboard(billboard);
            if (billboard == Billboard.FIXED_ANGLE) {
                h.setFacing(facing);
                if (pitch != null) {
                    h.setPitch(pitch);
                }
                // pitch == null 时保留原值（不调用 setPitch）
            }
            h.save();
            h.showToNearby();

            String modeDisplay = plugin.getMessages().getRaw(billboard.getDisplayNameKey());
            if (billboard == Billboard.FIXED_ANGLE) {
                Float effectivePitch = pitch != null ? pitch : h.getPitch();
                if (effectivePitch != null) {
                    modeDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw-pitch",
                            "yaw", String.valueOf(facing),
                            "pitch", String.valueOf(effectivePitch));
                } else {
                    modeDisplay += plugin.getMessages().getString("gui.billboard.angle-yaw",
                            "yaw", String.valueOf(facing));
                }
            }
            plugin.getMessages().send(player, "gui.msg-billboard-set", "mode", modeDisplay);
        }
        guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
