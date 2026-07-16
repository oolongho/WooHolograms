package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
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
        super("background_settings", ColorUtil.colorize("&8背景设置"), 36);
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
                            "&e点击返回"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        // 返回按钮
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

        // 当前背景设置信息
        Brightness brightness = hologram.getBrightness();
        setButton(4, GuiButton.builder(Material.BLACK_STAINED_GLASS_PANE)
                .name("&f当前背景设置")
                .lore(Arrays.asList(
                        "",
                        "&7透明度: &f" + hologram.getBackgroundAlpha() + " (0=透明, 255=不透明)",
                        "&7颜色: &f#" + String.format("%06X", hologram.getBackgroundColor()),
                        "&7彩虹渐变: " + (hologram.isChromaBackground() ? "&a启用" : "&c禁用"),
                        "&7亮度: &f" + (brightness != null ? "天空光=" + brightness.getSkyLight() + " 方块光=" + brightness.getBlockLight() : "默认"),
                        ""
                ))
                .build());

        // 背景透明度
        setButton(11, GuiButton.builder(Material.GRAY_DYE)
                .name("&f背景透明度")
                .lore(Arrays.asList(
                        "&7设置文本背景的透明度",
                        "&7当前: &f" + hologram.getBackgroundAlpha() + " (0=透明, 255=不透明)",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入背景透明度 (0-255, 0=完全透明, 255=完全不透明):",
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                try {
                                    int alpha = Integer.parseInt(input.trim());
                                    if (alpha < 0 || alpha > 255) {
                                        player.sendMessage(ColorUtil.colorize("&c透明度必须在 0-255 之间！"));
                                    } else {
                                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                        if (h != null) {
                                            h.setBackgroundAlpha(alpha);
                                            h.save();
                                            player.sendMessage(ColorUtil.colorize("&a已设置背景透明度为 " + alpha + "！"));
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                }
                                guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            });
                })
                .build());

        // 背景颜色
        setButton(13, GuiButton.builder(Material.GRAY_DYE)
                .name("&f背景颜色")
                .lore(Arrays.asList(
                        "&7设置文本背景颜色",
                        "&7当前: &f#" + String.format("%06X", hologram.getBackgroundColor()),
                        "",
                        "&7支持颜色名称: white, black, red, green,",
                        "&7blue, yellow, aqua, gray, dark_gray,",
                        "&7dark_red, dark_green, dark_blue,",
                        "&7dark_aqua, dark_purple, gold",
                        "&7或十六进制: #FF0000",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入背景颜色 (颜色名称如 red, 或十六进制如 #FF0000):",
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                input = input.trim();
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    int color = parseColor(input);
                                    if (color >= 0) {
                                        h.setBackgroundColor(color);
                                        h.save();
                                        player.sendMessage(ColorUtil.colorize("&a已设置背景颜色为 #" + String.format("%06X", color) + "！"));
                                    } else {
                                        player.sendMessage(ColorUtil.colorize("&c无效的颜色！请使用颜色名称或 #RRGGBB 格式。"));
                                    }
                                }
                                guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            });
                })
                .build());

        // Chroma 彩虹色（背景+发光合并切换）
        setButton(15, GuiButton.builder(Material.PRISMARINE_CRYSTALS)
                .name("&f彩虹渐变")
                .lore(Arrays.asList(
                        "&7彩虹渐变效果",
                        "&7当前: " + (hologram.isChromaBackground() ? "&a启用" : "&c禁用"),
                        "",
                        "&e点击切换"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        boolean newState = !h.isChromaBackground();
                        h.setChroma(newState);
                        h.save();
                        player.sendMessage(ColorUtil.colorize("&a彩虹渐变已" + (newState ? "启用" : "禁用") + "！"));
                    }
                    guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        // 亮度设置
        setButton(20, GuiButton.builder(Material.GLOWSTONE)
                .name("&f亮度")
                .lore(Arrays.asList(
                        "&7设置Display实体的亮度覆盖",
                        "&7天空光: &f" + (brightness != null ? brightness.getSkyLight() : "默认"),
                        "&7方块光: &f" + (brightness != null ? brightness.getBlockLight() : "默认"),
                        "",
                        "&7左键: &e输入亮度值 (天空光 方块光)",
                        "&7右键: &c重置为默认",
                        "",
                        "&7范围 0-15，输入 -1 重置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    org.bukkit.event.inventory.ClickType clickType = context.getClickType();

                    if (clickType == org.bukkit.event.inventory.ClickType.RIGHT) {
                        // 右键重置
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            h.setBrightness(null);
                            h.save();
                            player.sendMessage(ColorUtil.colorize("&a已重置亮度为默认值！"));
                        }
                        guiManager.openGui(player, new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    } else {
                        // 左键输入
                        player.closeInventory();

                        chatInputManager.requestInput(player, "&a请输入亮度值 (天空光 方块光，0-15):",
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
                                                player.sendMessage(ColorUtil.colorize("&a已重置亮度为默认值！"));
                                            }
                                        } else if (sky < 0 || sky > 15 || block < 0 || block > 15) {
                                            player.sendMessage(ColorUtil.colorize("&c亮度值必须在 0-15 之间！输入 -1 重置。"));
                                        } else {
                                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                            if (h != null) {
                                                h.setBrightness(Brightness.of(sky, block));
                                                h.save();
                                                player.sendMessage(ColorUtil.colorize("&a已设置亮度: 天空光=" + sky + " 方块光=" + block + "！"));
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！格式: 天空光 方块光"));
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
