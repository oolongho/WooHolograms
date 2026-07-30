package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 背景设置 GUI
 * 用于设置全息图的背景透明度、颜色、Chroma效果和亮度
 */
public class BackgroundSettingsGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int currentPageIndex;

    public BackgroundSettingsGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                                  String hologramName, int currentPageIndex) {
        super("background_settings", plugin.getMessages().get("gui.title-background-settings"), 36);
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
                            plugin.getMessages().getString("gui.lore-click-back-list")))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-detail"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        // 当前背景设置信息
        Brightness brightness = hologram.getBrightness();
        setButton(4, GuiButton.builder(Material.BLACK_STAINED_GLASS_PANE)
                .name(plugin.getMessages().getString("gui.background.btn-current"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.background.lore-alpha",
                                "alpha", String.valueOf(hologram.getBackgroundAlpha())),
                        plugin.getMessages().getString("gui.background.lore-color",
                                "color", String.format("%06X", hologram.getBackgroundColor())),
                        plugin.getMessages().getString("gui.background.lore-chroma",
                                "state", plugin.getMessages().getRaw(hologram.isChromaBackground() ? "state-enabled" : "state-disabled")),
                        plugin.getMessages().getString("gui.background.lore-brightness",
                                "sky", brightness != null ? String.valueOf(brightness.getSkyLight()) : "",
                                "block", brightness != null ? String.valueOf(brightness.getBlockLight()) : "",
                                "default", plugin.getMessages().getString("gui.background.default")),
                        ""
                ))
                .build());

        // 背景透明度
        setButton(11, GuiButton.builder(Material.GRAY_DYE)
                .name(plugin.getMessages().getString("gui.background.btn-alpha"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.background.lore-alpha-desc"),
                        plugin.getMessages().getString("gui.background.lore-alpha-current",
                                "alpha", String.valueOf(hologram.getBackgroundAlpha())),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.bg-alpha"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                try {
                                    int alpha = Integer.parseInt(input.trim());
                                    if (alpha < 0 || alpha > 255) {
                                        plugin.getMessages().send(player, "gui.msg-bg-alpha-range");
                                    } else {
                                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                        if (h != null) {
                                            h.setBackgroundAlpha(alpha);
                                            h.save();
                                            plugin.getMessages().send(player, "gui.msg-bg-alpha-set", "alpha", String.valueOf(alpha));
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                }
                                guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            });
                })
                .build());

        // 背景颜色
        setButton(13, GuiButton.builder(Material.GRAY_DYE)
                .name(plugin.getMessages().getString("gui.background.btn-color"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.background.lore-color-desc"),
                        plugin.getMessages().getString("gui.background.lore-color-current",
                                "color", String.format("%06X", hologram.getBackgroundColor())),
                        "",
                        plugin.getMessages().getString("gui.background.lore-color-names"),
                        plugin.getMessages().getString("gui.background.lore-color-hex"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.bg-color"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                input = input.trim();
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    int color = parseColor(input);
                                    if (color >= 0) {
                                        h.setBackgroundColor(color);
                                        h.save();
                                        plugin.getMessages().send(player, "gui.msg-bg-color-set", "color", "#" + String.format("%06X", color));
                                    } else {
                                        plugin.getMessages().send(player, "gui.msg-bg-color-invalid");
                                    }
                                }
                                guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            });
                })
                .build());

        // Chroma 彩虹色（背景+发光合并切换）
        setButton(15, GuiButton.builder(Material.PRISMARINE_CRYSTALS)
                .name(plugin.getMessages().getString("gui.background.btn-chroma"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.background.lore-chroma-desc"),
                        plugin.getMessages().getString("gui.background.lore-chroma-current",
                                "state", plugin.getMessages().getRaw(hologram.isChromaBackground() ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-toggle")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        boolean newState = !h.isChromaBackground();
                        h.setChroma(newState);
                        h.save();
                        plugin.getMessages().send(player, "gui.msg-bg-rainbow-toggle", "state",
                                plugin.getMessages().getRaw(newState ? "state-enabled" : "state-disabled"));
                    }
                    guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        // 亮度设置
        setButton(20, GuiButton.builder(Material.GLOWSTONE)
                .name(plugin.getMessages().getString("gui.background.btn-brightness"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.background.lore-brightness-desc"),
                        plugin.getMessages().getString("gui.background.lore-brightness-sky",
                                "sky", brightness != null ? String.valueOf(brightness.getSkyLight()) : plugin.getMessages().getString("gui.background.default")),
                        plugin.getMessages().getString("gui.background.lore-brightness-block",
                                "block", brightness != null ? String.valueOf(brightness.getBlockLight()) : plugin.getMessages().getString("gui.background.default")),
                        "",
                        plugin.getMessages().getString("gui.background.lore-brightness-left"),
                        plugin.getMessages().getString("gui.background.lore-brightness-right"),
                        "",
                        plugin.getMessages().getString("gui.background.lore-brightness-range")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    org.bukkit.event.inventory.ClickType clickType = context.getClickType();

                    if (clickType == org.bukkit.event.inventory.ClickType.RIGHT) {
                        // 右键重置
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            h.setBrightness(null);
                            h.save();
                            plugin.getMessages().send(player, "gui.msg-brightness-reset");
                        }
                        guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    } else {
                        // 左键输入
                        player.closeInventory();

                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.bg-brightness"),
                                ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                    try {
                                        String[] parts = input.trim().split("\\s+");
                                        int sky = Integer.parseInt(parts[0]);
                                        int block = parts.length > 1 ? Integer.parseInt(parts[1]) : sky;

                                        if (sky == -1 || block == -1) {
                                            // -1 重置
                                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                            if (h != null) {
                                                h.setBrightness(null);
                                                h.save();
                                                plugin.getMessages().send(player, "gui.msg-brightness-reset");
                                            }
                                        } else if (sky < 0 || sky > 15 || block < 0 || block > 15) {
                                            plugin.getMessages().send(player, "gui.msg-brightness-range");
                                        } else {
                                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                            if (h != null) {
                                                h.setBrightness(Brightness.of(sky, block));
                                                h.save();
                                                plugin.getMessages().send(player, "gui.msg-brightness-set",
                                                        "sky", String.valueOf(sky), "block", String.valueOf(block));
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number-format");
                                    }
                                    guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                                });
                    }
                })
                .build());

        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 36; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }

    private static int parseColor(String input) {
        if (input == null || input.isEmpty()) return -1;

        if (input.startsWith("#")) {
            try {
                return Integer.parseInt(input.substring(1), 16) & 0xFFFFFF;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return switch (input.toLowerCase()) {
            case "black" -> 0x000000;
            case "white" -> 0xFFFFFF;
            case "red" -> 0xFF0000;
            case "green" -> 0x00FF00;
            case "blue" -> 0x0000FF;
            case "yellow" -> 0xFFFF00;
            case "aqua", "cyan" -> 0x00FFFF;
            case "gray", "grey" -> 0x808080;
            case "dark_gray", "dark_grey" -> 0x404040;
            case "dark_red" -> 0xAA0000;
            case "dark_green" -> 0x00AA00;
            case "dark_blue" -> 0x0000AA;
            case "dark_aqua", "dark_cyan" -> 0x00AAAA;
            case "dark_purple", "purple" -> 0xAA00AA;
            case "gold", "orange" -> 0xFFAA00;
            default -> -1;
        };
    }
}
