package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
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
        super("billboard_select", ColorUtil.colorize("&8朝向设置"), 27);
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

        Billboard currentBillboard = hologram.getBillboard();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回全息图详情",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());

        String currentDisplay = currentBillboard.getDisplayName();
        if (currentBillboard == Billboard.FIXED_ANGLE) {
            currentDisplay += " (yaw=" + hologram.getFacing() + "度";
            if (hologram.getPitch() != null) {
                currentDisplay += ", pitch=" + hologram.getPitch() + "度";
            }
            currentDisplay += ")";
        }
        setButton(4, GuiButton.builder(Material.COMPASS)
                .name("&f当前朝向模式")
                .lore(Arrays.asList(
                        "",
                        "&7" + currentDisplay,
                        ""
                ))
                .build());

        setButton(10, GuiButton.builder(Material.STONE_BUTTON)
                .name("&f固定角度")
                .lore(Arrays.asList(
                        "&7使用固定角度朝向",
                        "&7需要设置具体角度值",
                        "",
                        currentBillboard == Billboard.FIXED_ANGLE ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入固定角度 (yaw 0-360 [pitch -90~90])，用空格分隔:",
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        try {
                            String[] parts = input.trim().split("\\s+");
                            float yaw = Float.parseFloat(parts[0]);
                            Float pitch = null;
                            if (parts.length >= 2) {
                                pitch = Float.parseFloat(parts[1]);
                                if (pitch < -90 || pitch > 90) {
                                    player.sendMessage(ColorUtil.colorize("&c垂直角度必须在 -90 到 90 之间！"));
                                    guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                                    return;
                                }
                            }
                            setBillboard(player, Billboard.FIXED_ANGLE, yaw, pitch);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c角度必须是数字！"));
                            guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                        }
                    });
                })
                .build());

        setButton(12, GuiButton.builder(Material.END_ROD)
                .name("&f垂直跟随")
                .lore(Arrays.asList(
                        "&7垂直方向跟随玩家视角",
                        "&7水平方向固定",
                        "",
                        currentBillboard == Billboard.VERTICAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.VERTICAL, 0, null);
                })
                .build());

        setButton(14, GuiButton.builder(Material.RAIL)
                .name("&f水平跟随")
                .lore(Arrays.asList(
                        "&7水平方向跟随玩家视角",
                        "&7垂直方向固定",
                        "",
                        currentBillboard == Billboard.HORIZONTAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.HORIZONTAL, 0, null);
                })
                .build());

        setButton(16, GuiButton.builder(Material.END_CRYSTAL)
                .name("&f完全跟随")
                .lore(Arrays.asList(
                        "&7完全跟随玩家视角",
                        "&7默认模式",
                        "",
                        currentBillboard == Billboard.CENTER ? "&a当前选择" : "&e点击选择"
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

            String modeDisplay = billboard.getDisplayName();
            if (billboard == Billboard.FIXED_ANGLE) {
                modeDisplay += " (yaw=" + facing + "度";
                if (pitch != null) {
                    modeDisplay += ", pitch=" + pitch + "度";
                } else if (h.getPitch() != null) {
                    modeDisplay += ", pitch=" + h.getPitch() + "度";
                }
                modeDisplay += ")";
            }
            player.sendMessage(ColorUtil.colorize("&a已设置朝向模式为 " + modeDisplay + "！"));
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
