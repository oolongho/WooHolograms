package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 亮度设置 GUI
 * 用于设置全息图行的亮度等级
 *
 * @author oolongho
 */
public class BrightnessSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final int lineIndex;
    private final boolean selectingSkyLight;

    public BrightnessSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, int lineIndex, boolean selectingSkyLight) {
        super("brightness_select", ColorUtil.colorize("&8亮度设置"), 9);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.lineIndex = lineIndex;
        this.selectingSkyLight = selectingSkyLight;

        render();
    }

    private void render() {
        clearButtons();

        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            setButton(4, GuiButton.builder(Material.BARRIER)
                    .name("&f全息图不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该全息图已被删除",
                            "",
                            "&e点击返回"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex < 0 || lineIndex >= page.size()) {
            setButton(4, GuiButton.builder(Material.BARRIER)
                    .name("&f行不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该行已被删除",
                            "",
                            "&e点击返回"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                    })
                    .build());
            return;
        }

        HologramLine line = page.getLine(lineIndex);
        Brightness currentBrightness = line.getBrightness();
        int currentSkyLight = currentBrightness != null ? currentBrightness.getSkyLight() : -1;
        int currentBlockLight = currentBrightness != null ? currentBrightness.getBlockLight() : -1;

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回行编辑",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // 当前设置显示
        setButton(4, GuiButton.builder(Material.GLOWSTONE)
                .name("&f当前亮度设置")
                .lore(Arrays.asList(
                        "",
                        "&7天空光: &f" + (currentSkyLight >= 0 ? currentSkyLight : "默认"),
                        "&7方块光: &f" + (currentBlockLight >= 0 ? currentBlockLight : "默认"),
                        ""
                ))
                .build());

        // 选择天空光按钮
        setButton(6, GuiButton.builder(Material.SUNFLOWER)
                .name("&f设置天空光")
                .lore(Arrays.asList(
                        "&7当前: &f" + (currentSkyLight >= 0 ? currentSkyLight : "默认"),
                        "",
                        "&e点击设置天空光"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager,
                            hologramName, pageIndex, lineIndex, true));
                })
                .build());

        // 选择方块光按钮
        setButton(7, GuiButton.builder(Material.LANTERN)
                .name("&f设置方块光")
                .lore(Arrays.asList(
                        "&7当前: &f" + (currentBlockLight >= 0 ? currentBlockLight : "默认"),
                        "",
                        "&e点击设置方块光"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager,
                            hologramName, pageIndex, lineIndex, false));
                })
                .build());

        // 重置按钮
        setButton(8, GuiButton.builder(Material.BARRIER)
                .name("&f重置为默认")
                .lore(Arrays.asList(
                        "&7将亮度重置为默认值",
                        "",
                        "&e点击重置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        HologramPage p = h.getPage(pageIndex);
                        if (p != null && lineIndex < p.size()) {
                            HologramLine l = p.getLine(lineIndex);
                            if (l != null) {
                                l.setBrightness(null);
                                h.save();
                                h.showToNearby();
                                player.sendMessage(ColorUtil.colorize("&a已重置亮度为默认值！"));
                            }
                        }
                    }
                    guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // 亮度等级选择 (0-15)
        // 使用聊天输入方式设置亮度值
        setButton(2, GuiButton.builder(Material.OAK_SIGN)
                .name("&f输入亮度值")
                .lore(Arrays.asList(
                        "&7选择设置: &f" + (selectingSkyLight ? "天空光" : "方块光"),
                        "&7范围: &f0-15",
                        "",
                        "&e点击输入"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入亮度值 (0-15):",
                            ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    int value = Integer.parseInt(input);
                                    if (value < 0 || value > 15) {
                                        player.sendMessage(ColorUtil.colorize("&c亮度值必须在 0-15 之间！"));
                                    } else {
                                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                        if (h != null) {
                                            HologramPage p = h.getPage(pageIndex);
                                            if (p != null && lineIndex < p.size()) {
                                                HologramLine l = p.getLine(lineIndex);
                                                if (l != null) {
                                                    Brightness current = l.getBrightness();
                                                    int skyLight = selectingSkyLight ? value : (current != null ? current.getSkyLight() : value);
                                                    int blockLight = selectingSkyLight ? (current != null ? current.getBlockLight() : value) : value;
                                                    l.setBrightness(Brightness.of(skyLight, blockLight));
                                                    h.save();
                                                    h.showToNearby();
                                                    player.sendMessage(ColorUtil.colorize("&a已设置" + (selectingSkyLight ? "天空光" : "方块光") + "为 " + value + "！"));
                                                }
                                            }
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                }
                                guiManager.openGui(player, new BrightnessSelectGui(plugin, guiManager, chatInputManager,
                                        hologramName, pageIndex, lineIndex, selectingSkyLight));
                            });
                })
                .build());

        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        int[] backgroundSlots = {1, 3, 5};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
