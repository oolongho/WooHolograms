package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.hologram.HologramType;
import com.oolongho.holograms.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HologramDetailGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private int currentPageIndex;
    private int pageGroupIndex;
    private static final int LINES_PER_PAGE = 27;
    private static final int PAGES_PER_GROUP = 4;

    public HologramDetailGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName, int pageIndex) {
        this(plugin, guiManager, chatInputManager, hologramName, pageIndex, 0);
    }

    public HologramDetailGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName, int pageIndex, int pageGroupIndex) {
        super("hologram_detail", plugin.getMessages().get("gui.title-detail", "name", hologramName), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.currentPageIndex = pageIndex;
        this.pageGroupIndex = pageGroupIndex;
        
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
                            plugin.getMessages().getString("gui.lore-click-back-list")))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        if (currentPageIndex >= hologram.getPageCount()) {
            currentPageIndex = Math.max(0, hologram.getPageCount() - 1);
        }

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-back-list"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-list"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        renderPageButtons(hologram);

        setButton(8, GuiButton.builder(Material.NAME_TAG)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-page-manage"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-state",
                                "state", plugin.getMessages().getRaw(hologram.isEnabled() ? "state-enabled" : "state-disabled")),
                        plugin.getMessages().getString("gui.hologram-detail.lore-total-pages",
                                "pages", String.valueOf(hologram.getPageCount())),
                        plugin.getMessages().getString("gui.hologram-detail.lore-current-page",
                                "page", String.valueOf(currentPageIndex + 1)),
                        plugin.getMessages().getString("gui.hologram-detail.lore-display-range",
                                "range", String.valueOf(hologram.getDisplayRange())),
                        plugin.getMessages().getString("gui.hologram-detail.lore-permission",
                                "permission", hologram.getPermission() != null ? hologram.getPermission() : plugin.getMessages().getString("gui.hologram-detail.none")),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-click-manage-page")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                })
                .build());
        
        HologramPage page = hologram.getPage(currentPageIndex);
        if (page != null) {
            int lineCount = page.size();
            int startLine = 0;
            int endLine = Math.min(LINES_PER_PAGE, lineCount);
            
            for (int i = startLine; i < endLine; i++) {
                int slot = 9 + i;
                if (slot >= 36) break;
                
                HologramLine line = page.getLine(i);
                if (line != null) {
                    setButton(slot, createLineButton(i, line));
                }
            }
        }
        
        renderBottomButtons(hologram);
    }
    
    private void renderPageButtons(Hologram hologram) {
        int pageCount = hologram.getPageCount();
        
        // 计算当前页面所在的组
        int currentGroup = currentPageIndex / PAGES_PER_GROUP;
        if (pageGroupIndex != currentGroup) {
            pageGroupIndex = currentGroup;
        }
        
        // 计算当前组的起始页面索引
        int groupStartPage = pageGroupIndex * PAGES_PER_GROUP;
        
        // 渲染上一组按钮（槽位1）
        if (pageGroupIndex > 0) {
            setButton(1, GuiButton.builder(Material.SPECTRAL_ARROW)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-prev-group"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.hologram-detail.lore-group-range",
                                    "start", String.valueOf((pageGroupIndex) * PAGES_PER_GROUP + 1),
                                    "end", String.valueOf(Math.min((pageGroupIndex) * PAGES_PER_GROUP + PAGES_PER_GROUP, pageCount))),
                            "",
                            plugin.getMessages().getString("gui.lore-click-switch")))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(),
                                new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName,
                                        Math.max(0, groupStartPage - PAGES_PER_GROUP), pageGroupIndex - 1));
                    })
                    .build());
        } else {
            setButton(1, GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-no-more-pages"))
                    .build());
        }

        // 渲染页面按钮（槽位2-5，共4个）
        for (int i = 0; i < PAGES_PER_GROUP; i++) {
            int slot = 2 + i;
            int pageIndex = groupStartPage + i;

            if (pageIndex < pageCount) {
                HologramPage page = hologram.getPage(pageIndex);
                int lineCount = page != null ? page.size() : 0;
                int actionCount = 0;
                if (page != null) {
                    for (ClickType clickType : ClickType.values()) {
                        actionCount += page.getActions(clickType).size();
                    }
                }

                boolean isCurrentPage = (pageIndex == currentPageIndex);
                Material material = isCurrentPage ? Material.FILLED_MAP : Material.MAP;

                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-page-lines",
                        "lines", String.valueOf(lineCount)));
                lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-page-actions",
                        "actions", String.valueOf(actionCount)));
                lore.add("");
                if (isCurrentPage) {
                    lore.add(plugin.getMessages().getString("gui.lore-current-editing"));
                } else {
                    lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-click-switch-page"));
                }

                GuiButton.Builder builder = GuiButton.builder(material)
                        .name(plugin.getMessages().getString(isCurrentPage ? "gui.hologram-detail.btn-page-current" : "gui.hologram-detail.btn-page",
                                "page", String.valueOf(pageIndex + 1)))
                        .lore(lore);

                if (isCurrentPage) {
                    builder.glow();
                }

                final int targetPageIndex = pageIndex;
                if (!isCurrentPage) {
                    builder.onClick(context -> {
                        guiManager.openGui(context.getPlayer(),
                                new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, targetPageIndex, pageGroupIndex));
                    });
                }

                setButton(slot, builder.build());
            } else {
                // 空页面槽位
                setButton(slot, GuiButton.builder(Material.MAP)
                        .name(plugin.getMessages().getString("gui.hologram-detail.btn-empty-page",
                                "page", String.valueOf(pageIndex + 1)))
                        .lore(Arrays.asList(
                                "",
                                plugin.getMessages().getString("gui.hologram-detail.lore-empty-page"),
                                "",
                                plugin.getMessages().getString("gui.hologram-detail.lore-empty-page-hint")))
                        .build());
            }
        }

        // 渲染下一组按钮（槽位6）
        int totalPages = hologram.getPageCount();
        boolean hasNextGroup = (pageGroupIndex + 1) * PAGES_PER_GROUP < totalPages;

        if (hasNextGroup) {
            int nextGroupStart = (pageGroupIndex + 1) * PAGES_PER_GROUP + 1;
            int nextGroupEnd = Math.min((pageGroupIndex + 2) * PAGES_PER_GROUP, totalPages);
            setButton(6, GuiButton.builder(Material.SPECTRAL_ARROW)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-next-group"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.hologram-detail.lore-group-range",
                                    "start", String.valueOf(nextGroupStart),
                                    "end", String.valueOf(nextGroupEnd)),
                            "",
                            plugin.getMessages().getString("gui.lore-click-switch")))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(),
                                new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName,
                                        (pageGroupIndex + 1) * PAGES_PER_GROUP, pageGroupIndex + 1));
                    })
                    .build());
        } else {
            setButton(6, GuiButton.builder(Material.LIME_DYE)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-add-page"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.hologram-detail.lore-add-page"),
                            plugin.getMessages().getString("gui.hologram-detail.lore-add-page-current",
                                    "pages", String.valueOf(pageCount)),
                            "",
                            plugin.getMessages().getString("gui.lore-click-add")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        HologramPage newPage = hologram.addPage();
                        if (newPage != null) {
                            hologram.save();
                            plugin.getMessages().send(player, "gui.msg-page-add-success", "count", String.valueOf(hologram.getPageCount()));
                            int newPageIndex = hologram.getPageCount() - 1;
                            int newGroup = newPageIndex / PAGES_PER_GROUP;
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, newPageIndex, newGroup));
                        } else {
                            plugin.getMessages().send(player, "gui.msg-page-add-failed");
                        }
                    })
                    .build());
        }

        // 删除页面按钮（槽位7）
        if (pageCount > 1) {
            setButton(7, GuiButton.builder(Material.RED_DYE)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-delete-page"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.hologram-detail.lore-delete-page",
                                    "page", String.valueOf(currentPageIndex + 1)),
                            "",
                            plugin.getMessages().getString("gui.hologram-detail.lore-irreversible"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-delete")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        guiManager.openGui(player, ConfirmGui.createDeletePageConfirm(plugin, hologramName, currentPageIndex, confirmed -> {
                            if (confirmed) {
                                hologram.removePage(currentPageIndex);
                                hologram.save();
                                int newPageIndex = Math.min(currentPageIndex, hologram.getPageCount() - 1);
                                newPageIndex = Math.max(0, newPageIndex);
                                plugin.getMessages().send(player, "gui.msg-page-remove-success", "count", String.valueOf(hologram.getPageCount()));
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, newPageIndex));
                            } else {
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex, pageGroupIndex));
                            }
                        }));
                    })
                    .build());
        } else {
            setButton(7, GuiButton.builder(Material.GRAY_DYE)
                    .name(plugin.getMessages().getString("gui.hologram-detail.btn-cannot-delete"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.hologram-detail.lore-keep-one-page")))
                    .build());
        }
    }
    
    private void renderBottomButtons(Hologram hologram) {
        // === Upper row (36-44): Common operations ===
        setButton(36, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-add-line"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-add-line"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-add")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-text"),
                            ChatInputManager.InputType.LINE_TEXT, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            HologramPage p = h.getPage(currentPageIndex);
                            if (p != null) {
                                p.addLine(input);
                                h.save();
                                h.showToNearby();
                                plugin.getMessages().send(player, "gui.msg-line-add-success");
                            }
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());

        setButton(37, GuiButton.builder(Material.WRITABLE_BOOK)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-insert-line"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-insert-line"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-insert-line-format"),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-click-input")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-insert"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h == null) {
                            plugin.getMessages().send(player, "gui.msg-hologram-not-exists");
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }

                        int spaceIndex = input.indexOf(' ');
                        if (spaceIndex <= 0) {
                            plugin.getMessages().send(player, "gui.msg-format-error-line");
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }

                        try {
                            int lineNum = Integer.parseInt(input.substring(0, spaceIndex));
                            String content = input.substring(spaceIndex + 1);

                            HologramPage p = h.getPage(currentPageIndex);
                            if (p == null) {
                                plugin.getMessages().send(player, "gui.msg-page-not-exists");
                            } else if (lineNum < 1 || lineNum > p.size() + 1) {
                                plugin.getMessages().send(player, "gui.msg-line-number-range", "max", String.valueOf(p.size() + 1));
                            } else {
                                p.insertLine(lineNum - 1, content);
                                h.save();
                                h.showToNearby();
                                plugin.getMessages().send(player, "gui.msg-line-insert-success", "line", String.valueOf(lineNum));
                            }
                        } catch (NumberFormatException e) {
                            plugin.getMessages().send(player, "gui.msg-line-number-must-be-number");
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());

        setButton(38, GuiButton.builder(hologram.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(plugin.getMessages().getString(hologram.isEnabled() ? "gui.hologram-detail.btn-disable" : "gui.hologram-detail.btn-enable"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-current-state",
                                "state", plugin.getMessages().getRaw(hologram.isEnabled() ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-toggle")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    boolean newState = !hologram.isEnabled();
                    hologram.setEnabled(newState);
                    hologram.save();

                    if (newState) {
                        hologram.showToNearby();
                        plugin.getMessages().send(player, "gui.msg-enable");
                    } else {
                        hologram.hideFromAll();
                        plugin.getMessages().send(player, "gui.msg-disable");
                    }

                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(39, GuiButton.builder(Material.ENDER_PEARL)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-teleport"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-teleport"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-teleport")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.teleport")) {
                        plugin.getMessages().send(player, "gui.msg-no-permission");
                        return;
                    }
                    Location loc = hologram.getLocation();
                    if (loc != null && loc.getWorld() != null) {
                        SchedulerUtil.teleportAsync(player, loc);
                        plugin.getMessages().send(player, "gui.msg-teleport-success");
                    } else {
                        plugin.getMessages().send(player, "gui.msg-teleport-invalid");
                    }
                })
                .build());

        setButton(40, GuiButton.builder(Material.RECOVERY_COMPASS)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-move-here"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-move-here"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-move")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.movehere")) {
                        plugin.getMessages().send(player, "gui.msg-no-permission");
                        return;
                    }
                    hologram.setLocation(player.getLocation());
                    hologram.save();
                    hologram.showToNearby();
                    plugin.getMessages().send(player, "gui.msg-move-success");
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(41, GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-move-hologram"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-move-hologram"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-open")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramMoveGui(plugin, guiManager, chatInputManager, hologramName));
                })
                .build());

        setButton(42, GuiButton.builder(Material.SLIME_BALL)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-clone"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-clone"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-clone")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.target-name"),
                            ChatInputManager.InputType.TARGET_NAME, hologramName, input -> {
                        if (plugin.getHologramManager().containsHologram(input)) {
                            plugin.getMessages().send(player, "gui.msg-clone-exists", "name", input);
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }

                        Hologram target = plugin.getHologramManager().cloneHologram(hologramName, input, null, false);

                        if (target != null) {
                            target.save();
                            plugin.getMessages().send(player, "gui.msg-clone-success", "name", input);
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, input, 0));
                        } else {
                            plugin.getMessages().send(player, "gui.msg-clone-failed");
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        }
                    });
                })
                .build());

        setButton(44, GuiButton.builder(Material.COMMAND_BLOCK)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-action-manage"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-action-manage"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-manage")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        // === Lower row (45-53): Display settings ===
        setButton(45, GuiButton.builder(Material.REPEATER)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-alignment"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-alignment"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-alignment-current",
                                "alignment", plugin.getMessages().getRaw(hologram.getAlignment().getDisplayNameKey())),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new AlignmentSelectGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(46, GuiButton.builder(Material.RAIL)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-line-spacing"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-line-spacing",
                                "spacing", String.valueOf(hologram.getLineHeight())),
                        plugin.getMessages().getString("gui.hologram-detail.lore-line-width",
                                "width", String.valueOf(hologram.getLineWidth())),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-left-set-spacing"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-right-set-width")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType().isRightClick()) {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-width"), input -> {
                            try {
                                int width = Integer.parseInt(input.trim());
                                if (width < 1 || width > 999) {
                                    plugin.getMessages().send(player, "gui.msg-width-range");
                                } else {
                                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                    if (h != null) {
                                        h.setLineWidth(width);
                                        h.save();
                                        plugin.getMessages().send(player, "gui.msg-width-set", "width", String.valueOf(width));
                                    }
                                }
                            } catch (NumberFormatException e) {
                                plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                            }
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        });
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-height"),
                                ChatInputManager.InputType.LINE_HEIGHT, hologramName, input -> {
                                    try {
                                        double height = Double.parseDouble(input);
                                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                        if (h != null) {
                                            h.setLineHeight(height);
                                            h.save();
                                            h.realignLines();
                                            h.showToNearby();
                                            plugin.getMessages().send(player, "gui.msg-height-set", "height", String.valueOf(height));
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                    }
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                                });
                    }
                })
                .build());

        setButton(47, GuiButton.builder(Material.WHITE_BANNER)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-double-sided"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-double-sided"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-double-sided-current",
                                "state", plugin.getMessages().getRaw(hologram.isDoubleSided() ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-toggle")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    hologram.setDoubleSided(!hologram.isDoubleSided());
                    hologram.save();
                    hologram.showToNearby();
                    plugin.getMessages().send(player, "gui.msg-double-sided", "state",
                            plugin.getMessages().getRaw(hologram.isDoubleSided() ? "state-enabled" : "state-disabled"));
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        Billboard billboard = hologram.getBillboard();
        String facingDisplay = plugin.getMessages().getRaw(billboard.getDisplayNameKey());
        if (billboard == Billboard.FIXED_ANGLE) {
            String pitchPart = hologram.getPitch() != null
                    ? plugin.getMessages().getString("gui.hologram-detail.facing-pitch",
                            "pitch", String.valueOf(hologram.getPitch()))
                    : "";
            facingDisplay += plugin.getMessages().getString("gui.hologram-detail.facing-yaw",
                    "yaw", String.valueOf(hologram.getFacing()), "pitch", pitchPart);
        }
        setButton(48, GuiButton.builder(Material.SPYGLASS)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-facing"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-facing"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-facing-current",
                                "facing", facingDisplay),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                })
                .build());

        setButton(49, GuiButton.builder(Material.ITEM_FRAME)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-scale"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-scale"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-scale-current",
                                "scale", String.format("%.1f, %.1f, %.1f", hologram.getScaleX(), hologram.getScaleY(), hologram.getScaleZ())),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-scale-left"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-scale-right"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType().isRightClick()) {
                        hologram.setScale(1.0f, 1.0f, 1.0f);
                        hologram.save();
                        hologram.showToNearby();
                        plugin.getMessages().send(player, "gui.msg-scale-reset");
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.scale"),
                                ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                    try {
                                        String[] parts = input.trim().split("\\s+");
                                        if (parts.length != 3) {
                                            plugin.getMessages().send(player, "gui.msg-format-error-xyz");
                                        } else {
                                            float x = Float.parseFloat(parts[0]);
                                            float y = Float.parseFloat(parts[1]);
                                            float z = Float.parseFloat(parts[2]);
                                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                            if (h != null) {
                                                h.setScale(x, y, z);
                                                h.save();
                                                h.showToNearby();
                                                plugin.getMessages().send(player, "gui.msg-scale-set",
                                                        "x", String.format("%.1f", x),
                                                        "y", String.format("%.1f", y),
                                                        "z", String.format("%.1f", z));
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                    }
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                                });
                    }
                })
                .build());

        String glowColorDisplay = hologram.getGlowColor() != -1 ? "#" + String.format("%06X", hologram.getGlowColor() & 0xFFFFFF) : plugin.getMessages().getString("gui.hologram-detail.default");
        setButton(50, GuiButton.builder(Material.GLOWSTONE_DUST)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-glow-color"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color-current",
                                "color", glowColorDisplay),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color-left"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color-right"),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color-format"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-glow-color-reset"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType().isRightClick()) {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            h.setGlowColor(-1);
                            h.save();
                            plugin.getMessages().send(player, "gui.msg-glow-reset");
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.glow-color"),
                                ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                    input = input.trim();
                                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                    if (h != null) {
                                        if (input.equalsIgnoreCase("reset")) {
                                            h.setGlowColor(-1);
                                            h.save();
                                            plugin.getMessages().send(player, "gui.msg-glow-reset");
                                        } else {
                                            int color = parseColor(input);
                                            if (color >= 0) {
                                                h.setGlowColor(color);
                                                h.save();
                                                plugin.getMessages().send(player, "gui.msg-glow-set", "color", "#" + String.format("%06X", color));
                                            } else {
                                                plugin.getMessages().send(player, "gui.msg-glow-color-invalid");
                                            }
                                        }
                                    }
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                                });
                    }
                })
                .build());

        setButton(51, GuiButton.builder(Material.BLACK_STAINED_GLASS_PANE)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-background"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-background"),
                        plugin.getMessages().getString("gui.background.lore-alpha",
                                "alpha", String.valueOf(hologram.getBackgroundAlpha())),
                        plugin.getMessages().getString("gui.background.lore-color",
                                "color", String.format("%06X", hologram.getBackgroundColor())),
                        plugin.getMessages().getString("gui.background.lore-chroma",
                                "state", plugin.getMessages().getRaw(hologram.isChromaBackground() ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BackgroundSettingsGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());

        setButton(52, GuiButton.builder(Material.ENDER_EYE)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-range-permission"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-display-range",
                                "range", String.valueOf(hologram.getDisplayRange())),
                        plugin.getMessages().getString("gui.hologram-detail.lore-update-range",
                                "range", String.valueOf(hologram.getUpdateRange())),
                        plugin.getMessages().getString("gui.hologram-detail.lore-permission",
                                "permission", hologram.getPermission() != null ? hologram.getPermission() : plugin.getMessages().getString("gui.hologram-detail.none")),
                        "",
                        plugin.getMessages().getString("gui.hologram-detail.lore-left-set-display-range"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-shift-set-update-range"),
                        plugin.getMessages().getString("gui.hologram-detail.lore-right-set-permission")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    if (context.getClickType().isRightClick()) {
                        // 右键: 设置权限
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.permission"),
                                ChatInputManager.InputType.PERMISSION, hologramName, input -> {
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                if (input.equalsIgnoreCase("clear")) {
                                    h.setPermission(null);
                                    plugin.getMessages().send(player, "gui.msg-permission-clear");
                                } else {
                                    h.setPermission(input);
                                    plugin.getMessages().send(player, "gui.msg-permission-set", "permission", input);
                                }
                                h.save();
                            }
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        });
                    } else if (context.getClickType().isShiftClick()) {
                        // Shift+左键: 设置更新范围
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.update-range"),
                                ChatInputManager.InputType.GENERIC, hologramName, input -> {
                            try {
                                int range = Integer.parseInt(input);
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    h.setUpdateRange(range);
                                    h.save();
                                    plugin.getMessages().send(player, "gui.msg-update-range-set", "range", String.valueOf(range));
                                }
                            } catch (NumberFormatException e) {
                                plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                            }
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        });
                    } else {
                        // 左键: 设置显示范围
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.display-range"),
                                ChatInputManager.InputType.GENERIC, hologramName, input -> {
                            try {
                                int range = Integer.parseInt(input);
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    h.setDisplayRange(range);
                                    h.save();
                                    plugin.getMessages().send(player, "gui.msg-display-range-set", "range", String.valueOf(range));
                                }
                            } catch (NumberFormatException e) {
                                plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                            }
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        });
                    }
                })
                .build());

        setButton(53, GuiButton.builder(Material.REDSTONE_BLOCK)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-delete-hologram"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-detail.lore-delete-hologram"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-delete")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.delete")) {
                        plugin.getMessages().send(player, "gui.msg-no-permission");
                        return;
                    }
                    guiManager.openGui(player, ConfirmGui.createDeleteConfirm(plugin, hologramName, confirmed -> {
                        if (confirmed) {
                            plugin.getHologramManager().deleteHologram(hologramName);
                            plugin.getMessages().send(player, "gui.msg-delete-success", "name", hologramName);
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
                        } else {
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        }
                    }));
                })
                .build());

        fillLastTwoRows();
    }
    
    private void fillLastTwoRows() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 36; i < 54; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }

    private GuiButton createLineButton(int lineIndex, HologramLine line) {
        HologramType type = line.getType();
        Material material;
        String typeDisplay;

        switch (type) {
            case ICON:
                material = Material.ITEM_FRAME;
                typeDisplay = plugin.getMessages().getString("gui.line-type.icon");
                break;
            case HEAD:
                material = Material.PLAYER_HEAD;
                typeDisplay = plugin.getMessages().getString("gui.line-type.head");
                break;
            case SMALLHEAD:
                material = Material.PLAYER_HEAD;
                typeDisplay = plugin.getMessages().getString("gui.line-type.smallhead");
                break;
            case ENTITY:
                material = Material.ZOMBIE_HEAD;
                typeDisplay = plugin.getMessages().getString("gui.line-type.entity");
                break;
            case BLOCK:
                material = Material.STONE;
                typeDisplay = plugin.getMessages().getString("gui.line-type.block");
                break;
            case TEXT:
            default:
                material = Material.PAPER;
                typeDisplay = plugin.getMessages().getString("gui.line-type.text");
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-line-type", "type", typeDisplay));
        lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-line-content",
                "content", line.getContent().length() > 30 ? line.getContent().substring(0, 30) + "..." : line.getContent()));
        lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-line-offset",
                "offset", String.format("%.2f, %.2f, %.2f", line.getOffsetX(), line.getOffsetY(), line.getOffsetZ())));
        lore.add(plugin.getMessages().getString("gui.hologram-detail.lore-line-height",
                "height", String.valueOf(line.getHeight())));
        lore.add("");
        lore.add(plugin.getMessages().getString("gui.lore-click-edit"));

        // X/Z 偏移非零的行会分裂为独立 TextGroup（独立背景与 Interaction），用附魔光效标识
        GuiButton.Builder builder = GuiButton.builder(material)
                .name(plugin.getMessages().getString("gui.hologram-detail.btn-line",
                        "index", String.valueOf(lineIndex + 1)))
                .lore(lore)
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex, lineIndex));
                });
        if (line.getOffsetX() != 0.0 || line.getOffsetZ() != 0.0) {
            builder.glow();
        }
        return builder.build();
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
