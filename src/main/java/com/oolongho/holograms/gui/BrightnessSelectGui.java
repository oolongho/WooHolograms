package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 亮度设置 GUI
 * 用于设置全息图行的亮度等级
 *
 * 
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
        super("brightness_select", plugin.getMessages().get("gui.title-brightness-select"), 9);
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
                    .name(plugin.getMessages().getString("gui.btn-hologram-not-exists"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.lore-hologram-deleted"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-back")
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
                    .name(plugin.getMessages().getString("gui.btn-line-not-exists"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.lore-line-deleted"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-back")
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
        String skyLightDisplay = currentSkyLight >= 0 ? String.valueOf(currentSkyLight) : plugin.getMessages().getRaw("gui.brightness.default");
        String blockLightDisplay = currentBlockLight >= 0 ? String.valueOf(currentBlockLight) : plugin.getMessages().getRaw("gui.brightness.default");

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-edit"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // 当前设置显示
        setButton(4, GuiButton.builder(Material.GLOWSTONE)
                .name(plugin.getMessages().getString("gui.brightness.current"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.brightness.sky-light", "value", skyLightDisplay),
                        plugin.getMessages().getString("gui.brightness.block-light", "value", blockLightDisplay),
                        ""
                ))
                .build());

        // 选择天空光按钮
        setButton(6, GuiButton.builder(Material.SUNFLOWER)
                .name(plugin.getMessages().getString("gui.brightness.set-sky"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.brightness.current-value", "value", skyLightDisplay),
                        "",
                        plugin.getMessages().getString("gui.brightness.click-set-sky")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager,
                            hologramName, pageIndex, lineIndex, true));
                })
                .build());

        // 选择方块光按钮
        setButton(7, GuiButton.builder(Material.LANTERN)
                .name(plugin.getMessages().getString("gui.brightness.set-block"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.brightness.current-value", "value", blockLightDisplay),
                        "",
                        plugin.getMessages().getString("gui.brightness.click-set-block")
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager,
                            hologramName, pageIndex, lineIndex, false));
                })
                .build());

        // 重置按钮
        setButton(8, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.brightness.reset"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.brightness.reset-lore"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-reset")
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
                                h.updateDisplayPropertiesAllViewers();
                                plugin.getMessages().send(player, "gui.msg-brightness-reset");
                            }
                        }
                    }
                    guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // 亮度等级选择 (0-15)
        // 使用聊天输入方式设置亮度值
        setButton(2, GuiButton.builder(Material.OAK_SIGN)
                .name(plugin.getMessages().getString("gui.brightness.input-value"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.brightness.select-setting", "value",
                                plugin.getMessages().getRaw(selectingSkyLight ? "gui.brightness.sky" : "gui.brightness.block")),
                        plugin.getMessages().getString("gui.brightness.range"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-input")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.brightness-value"),
                            ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    int value = Integer.parseInt(input);
                                    if (value < 0 || value > 15) {
                                        plugin.getMessages().send(player, "gui.msg-brightness-range");
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
                                                    h.updateDisplayPropertiesAllViewers();
                                                    plugin.getMessages().send(player, "gui.msg-brightness-set-alt",
                                                            "type", plugin.getMessages().getRaw(selectingSkyLight ? "gui.brightness.sky" : "gui.brightness.block"),
                                                            "value", String.valueOf(value));
                                                }
                                            }
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-input-invalid-number");
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
