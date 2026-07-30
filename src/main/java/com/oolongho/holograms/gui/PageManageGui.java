package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 页面管理 GUI
 * 用于管理全息图的页面
 * 
 */
public class PageManageGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private boolean sortMode = false;
    private int selectedPage = -1;

    public PageManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName) {
        super("page_manage", plugin.getMessages().get("gui.title-page-manage", "name", hologramName), 54);
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
            setButton(22, GuiButton.builder(Material.BARRIER)
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

        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name(plugin.getMessages().getString("gui.page-manage.hologram-name", "name", hologramName))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.page-manage.total-pages", "count", String.valueOf(hologram.getPageCount())),
                        ""
                ))
                .build());
        
        int pageCount = hologram.getPageCount();
        for (int i = 0; i < pageCount && i < 27; i++) {
            int slot = 9 + i;
            if (slot >= 36) break;
            
            final int pageIndex = i;
            HologramPage page = hologram.getPage(i);
            int lineCount = page != null ? page.size() : 0;
            
            Material buttonMaterial = Material.BOOK;
            if (sortMode && selectedPage == i) {
                buttonMaterial = Material.LIME_STAINED_GLASS_PANE;
            }
            
            String previewText = "";
            if (page != null && page.size() > 0) {
                String firstLine = page.getLine(0).getContent();
                if (firstLine.length() > 20) {
                    previewText = firstLine.substring(0, 20) + "...";
                } else {
                    previewText = firstLine;
                }
            }
            
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(plugin.getMessages().getString("gui.page-manage.line-count", "count", String.valueOf(lineCount)));
            if (!previewText.isEmpty()) {
                lore.add(plugin.getMessages().getString("gui.page-manage.first-line", "text", previewText));
            }
            lore.add("");
            if (sortMode) {
                if (selectedPage == -1) {
                    lore.add(plugin.getMessages().getString("gui.page-manage.click-select"));
                } else if (selectedPage == i) {
                    lore.add(plugin.getMessages().getString("gui.page-manage.click-swap-other"));
                } else {
                    lore.add(plugin.getMessages().getString("gui.page-manage.click-swap-this"));
                }
            } else {
                lore.add(plugin.getMessages().getString("gui.page-manage.left-click-view"));
                lore.add(plugin.getMessages().getString("gui.page-manage.right-click-delete"));
            }
            
            setButton(slot, GuiButton.builder(buttonMaterial)
                    .name(plugin.getMessages().getString(
                            sortMode && selectedPage == i ? "gui.page-manage.page-selected" : "gui.page-manage.page-normal",
                            "page", String.valueOf(i + 1)))
                    .lore(lore)
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        
                        if (sortMode) {
                            // 排序模式逻辑
                            if (selectedPage == -1) {
                                // 选中第一个页面
                                selectedPage = pageIndex;
                                plugin.getMessages().send(player, "gui.msg-page-swap-selected", "page", String.valueOf(pageIndex + 1));
                                render();
                                guiManager.openGui(player, this);
                            } else if (selectedPage != pageIndex) {
                                // 交换两个页面
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    if (h.swapPages(selectedPage, pageIndex)) {
                                        h.save();
                                        h.showToNearby();
                                        plugin.getMessages().send(player, "gui.msg-page-swap-success",
                                                "p1", String.valueOf(selectedPage + 1), "p2", String.valueOf(pageIndex + 1));
                                    } else {
                                        plugin.getMessages().send(player, "gui.msg-page-swap-failed");
                                    }
                                }
                                selectedPage = -1;
                                render();
                                guiManager.openGui(player, this);
                            }
                        } else {
                            // 普通模式逻辑
                            if (context.getClickType().isRightClick()) {
                                if (pageCount <= 1) {
                                    plugin.getMessages().send(player, "gui.msg-page-keep-one");
                                    return;
                                }
                                guiManager.openGui(player, ConfirmGui.createDeletePageConfirm(plugin, hologramName, pageIndex + 1, confirmed -> {
                                    if (confirmed) {
                                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                        if (h != null) {
                                            h.removePage(pageIndex);
                                            h.save();
                                            h.showToNearby();
                                            plugin.getMessages().send(player, "gui.msg-page-deleted", "page", String.valueOf(pageIndex + 1));
                                        }
                                        guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                    } else {
                                        guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                    }
                                }));
                            } else {
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                            }
                        }
                    })
                    .build());
        }
        
        setButton(45, GuiButton.builder(Material.EMERALD)
                .name(plugin.getMessages().getString("gui.page-manage.add-page"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.page-manage.add-page-desc"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-add")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        HologramPage newPage = h.addPage();
                        if (newPage != null) {
                            h.save();
                            plugin.getMessages().send(player, "gui.msg-page-add-success", "count", String.valueOf(h.getPageCount()));
                            guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                        } else {
                            plugin.getMessages().send(player, "gui.msg-page-add-failed");
                        }
                    }
                })
                .build());
        
        setButton(46, GuiButton.builder(sortMode ? Material.LIME_STAINED_GLASS_PANE : Material.HOPPER)
                .name(plugin.getMessages().getString(sortMode ? "gui.page-manage.exit-sort" : "gui.page-manage.sort-mode"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString(sortMode ? "gui.page-manage.exit-sort-desc" : "gui.page-manage.enter-sort-desc"),
                        plugin.getMessages().getString(sortMode ? "gui.page-manage.exit-sort-action" : "gui.page-manage.enter-sort-action"),
                        "",
                        plugin.getMessages().getString(sortMode ? "gui.lore-click-exit" : "gui.lore-click-enter")
                ))
                .onClick(context -> {
                    sortMode = !sortMode;
                    selectedPage = -1;
                    render();
                    guiManager.openGui(context.getPlayer(), this);
                })
                .build());
        
        setButton(49, GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.page-manage.quick-jump"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.page-manage.quick-jump-desc"),
                        "",
                        plugin.getMessages().getString("gui.page-manage.click-jump")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.page-number"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        try {
                            int pageNum = Integer.parseInt(input);
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                if (pageNum < 1 || pageNum > h.getPageCount()) {
                                    plugin.getMessages().send(player, "gui.msg-page-out-of-range", "max", String.valueOf(h.getPageCount()));
                                    guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                } else {
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageNum - 1));
                                }
                            }
                        } catch (NumberFormatException e) {
                            plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                            guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                        }
                    });
                })
                .build());
        
        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44, 47, 48, 50, 51, 52, 53};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
