package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.hologram.HologramType;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    private static final int MAX_PAGE_GROUPS = 100;

    public HologramDetailGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName, int pageIndex) {
        this(plugin, guiManager, chatInputManager, hologramName, pageIndex, 0);
    }

    public HologramDetailGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName, int pageIndex, int pageGroupIndex) {
        super("hologram_detail", ColorUtil.colorize("&8全息图: " + hologramName), 54);
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
        
        if (currentPageIndex >= hologram.getPageCount()) {
            currentPageIndex = Math.max(0, hologram.getPageCount() - 1);
        }
        
        setButton(0, GuiButton.builder(Material.SPECTRAL_ARROW)
                .name("&f返回列表")
                .lore(Arrays.asList(
                        "&7返回全息图列表",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());
        
        renderPageButtons(hologram);
        
        setButton(8, GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologram.getName())
                .lore(Arrays.asList(
                        "",
                        "&7状态: " + (hologram.isEnabled() ? "&a启用" : "&c禁用"),
                        "&7总页面: &f" + hologram.getPageCount(),
                        "&7当前页: &f" + (currentPageIndex + 1),
                        "&7显示范围: &f" + hologram.getDisplayRange() + " 格",
                        "&7权限: &f" + (hologram.getPermission() != null ? hologram.getPermission() : "无"),
                        ""
                ))
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
                    setButton(slot, createLineButton(hologram, i, line));
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
                    .name("&f上一组页面")
                    .lore(Arrays.asList(
                            "",
                            "&7切换到第 " + ((pageGroupIndex) * PAGES_PER_GROUP + 1) + " - " + 
                                    Math.min((pageGroupIndex) * PAGES_PER_GROUP + PAGES_PER_GROUP, pageCount) + " 页",
                            "",
                            "&e点击切换"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), 
                                new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 
                                        Math.max(0, groupStartPage - PAGES_PER_GROUP), pageGroupIndex - 1));
                    })
                    .build());
        } else {
            setButton(1, GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("&7没有更多页面")
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
                lore.add("&7行数: &f" + lineCount);
                lore.add("&7动作数: &f" + actionCount);
                lore.add("");
                if (isCurrentPage) {
                    lore.add("&a当前编辑中");
                } else {
                    lore.add("&e点击切换到此页");
                }
                
                GuiButton.Builder builder = GuiButton.builder(material)
                        .name((isCurrentPage ? "&a" : "&f") + "页面 " + (pageIndex + 1))
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
                        .name("&7空页面 " + (pageIndex + 1))
                        .lore(Arrays.asList(
                                "",
                                "&7尚未创建",
                                "",
                                "&7点击添加页面按钮创建"
                        ))
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
                    .name("&f下一组页面")
                    .lore(Arrays.asList(
                            "",
                            "&7切换到第 " + nextGroupStart + " - " + nextGroupEnd + " 页",
                            "",
                            "&e点击切换"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), 
                                new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 
                                        (pageGroupIndex + 1) * PAGES_PER_GROUP, pageGroupIndex + 1));
                    })
                    .build());
        } else {
            setButton(6, GuiButton.builder(Material.LIME_DYE)
                    .name("&a添加页面")
                    .lore(Arrays.asList(
                            "&7在末尾添加新页面",
                            "&7当前: &f" + pageCount + " 页",
                            "",
                            "&e点击添加"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        HologramPage newPage = hologram.addPage();
                        if (newPage != null) {
                            hologram.save();
                            player.sendMessage(ColorUtil.colorize("&a已添加新页面！当前共 " + hologram.getPageCount() + " 页。"));
                            int newPageIndex = hologram.getPageCount() - 1;
                            int newGroup = newPageIndex / PAGES_PER_GROUP;
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, newPageIndex, newGroup));
                        } else {
                            player.sendMessage(ColorUtil.colorize("&c添加页面失败！"));
                        }
                    })
                    .build());
        }
        
        // 删除页面按钮（槽位7）
        if (pageCount > 1) {
            setButton(7, GuiButton.builder(Material.RED_DYE)
                    .name("&c删除当前页")
                    .lore(Arrays.asList(
                            "&7删除当前页面 (第 " + (currentPageIndex + 1) + " 页)",
                            "",
                            "&c此操作不可撤销！",
                            "",
                            "&e点击删除"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        guiManager.openGui(player, ConfirmGui.create("&c确认删除页面?", confirmed -> {
                            if (confirmed) {
                                hologram.removePage(currentPageIndex);
                                hologram.save();
                                int newPageIndex = Math.min(currentPageIndex, hologram.getPageCount() - 1);
                                newPageIndex = Math.max(0, newPageIndex);
                                player.sendMessage(ColorUtil.colorize("&a已删除页面！当前共 " + hologram.getPageCount() + " 页。"));
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, newPageIndex));
                            } else {
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex, pageGroupIndex));
                            }
                        }));
                    })
                    .build());
        } else {
            setButton(7, GuiButton.builder(Material.GRAY_DYE)
                    .name("&7无法删除")
                    .lore(Arrays.asList(
                            "&7至少需要保留一个页面"
                    ))
                    .build());
        }
    }
    
    private void renderBottomButtons(Hologram hologram) {
        setButton(36, GuiButton.builder(Material.PAPER)
                .name("&f添加行")
                .lore(Arrays.asList(
                        "&7在末尾添加新行",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入行文本 (支持颜色代码):", 
                            ChatInputManager.InputType.LINE_TEXT, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            HologramPage p = h.getPage(currentPageIndex);
                            if (p != null) {
                                p.addLine(input);
                                h.save();
                                h.showToNearby();
                                player.sendMessage(ColorUtil.colorize("&a已添加新行！"));
                            }
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(37, GuiButton.builder(hologram.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&f" + (hologram.isEnabled() ? "禁用" : "启用"))
                .lore(Arrays.asList(
                        "&7当前状态: " + (hologram.isEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&e点击切换"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    boolean newState = !hologram.isEnabled();
                    hologram.setEnabled(newState);
                    hologram.save();
                    
                    if (newState) {
                        hologram.showToNearby();
                        player.sendMessage(ColorUtil.colorize("&a已启用全息图！"));
                    } else {
                        hologram.hideFromAll();
                        player.sendMessage(ColorUtil.colorize("&c已禁用全息图！"));
                    }
                    
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());
        
        setButton(38, GuiButton.builder(Material.ENDER_PEARL)
                .name("&f传送")
                .lore(Arrays.asList(
                        "&7传送到全息图位置",
                        "",
                        "&e点击传送"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Location loc = hologram.getLocation();
                    if (loc != null && loc.getWorld() != null) {
                        player.teleport(loc);
                        player.sendMessage(ColorUtil.colorize("&a已传送到全息图位置！"));
                    } else {
                        player.sendMessage(ColorUtil.colorize("&c全息图位置无效！"));
                    }
                })
                .build());
        
        setButton(39, GuiButton.builder(Material.RECOVERY_COMPASS)
                .name("&f移动到此处")
                .lore(Arrays.asList(
                        "&7将全息图移动到你的位置",
                        "",
                        "&e点击移动"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    hologram.setLocation(player.getLocation());
                    hologram.save();
                    hologram.showToNearby();
                    player.sendMessage(ColorUtil.colorize("&a已将全息图移动到当前位置！"));
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());
        
        Billboard billboard = hologram.getBillboard();
        String facingDisplay = billboard.getDisplayName();
        if (billboard == Billboard.FIXED_ANGLE) {
            facingDisplay += " (" + hologram.getFacing() + "度)";
        }
        setButton(40, GuiButton.builder(Material.COMPASS)
                .name("&f朝向设置")
                .lore(Arrays.asList(
                        "&7设置全息图的朝向模式",
                        "&7当前: &f" + facingDisplay,
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName));
                })
                .build());
        
        setButton(41, GuiButton.builder(Material.WHITE_BANNER)
                .name("&f双面显示")
                .lore(Arrays.asList(
                        "&7设置全息图是否双面可见",
                        "&7当前: " + (hologram.isDoubleSided() ? "&a启用" : "&c禁用"),
                        "",
                        "&e点击切换"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    hologram.setDoubleSided(!hologram.isDoubleSided());
                    hologram.save();
                    hologram.showToNearby();
                    player.sendMessage(ColorUtil.colorize("&a双面显示已" + (hologram.isDoubleSided() ? "启用" : "禁用") + "！"));
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());
        
        setButton(42, GuiButton.builder(Material.SLIME_BALL)
                .name("&f克隆")
                .lore(Arrays.asList(
                        "&7克隆此全息图",
                        "",
                        "&e点击克隆"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入目标全息图名称:", 
                            ChatInputManager.InputType.TARGET_NAME, hologramName, input -> {
                        if (plugin.getHologramManager().containsHologram(input)) {
                            player.sendMessage(ColorUtil.colorize("&c全息图 " + input + " 已存在！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }
                        
                        Hologram source = plugin.getHologramManager().getHologram(hologramName);
                        Hologram target = plugin.getHologramManager().createHologram(input, source.getLocation());
                        
                        if (target != null) {
                            target.setDisplayRange(source.getDisplayRange());
                            target.setUpdateRange(source.getUpdateRange());
                            target.setUpdateInterval(source.getUpdateInterval());
                            target.setPermission(source.getPermission());
                            target.setDefaultVisibleState(source.isDefaultVisibleState());
                            target.setDownOrigin(source.isDownOrigin());
                            target.setFacing(source.getFacing());
                            
                            for (int i = 0; i < source.getPageCount(); i++) {
                                HologramPage sourcePage = source.getPage(i);
                                HologramPage targetPage = i == 0 ? target.getPage(0) : target.addPage();
                                
                                if (sourcePage != null && targetPage != null) {
                                    for (HologramLine sourceLine : sourcePage.getLines()) {
                                        HologramLine targetLine = targetPage.addLine(sourceLine.getContent());
                                        if (targetLine != null) {
                                            targetLine.setOffsetX(sourceLine.getOffsetX());
                                            targetLine.setOffsetY(sourceLine.getOffsetY());
                                            targetLine.setOffsetZ(sourceLine.getOffsetZ());
                                            targetLine.setHeight(sourceLine.getHeight());
                                            targetLine.setPermission(sourceLine.getPermission());
                                        }
                                    }
                                    
                                    for (ClickType clickType : ClickType.values()) {
                                        for (Action action : sourcePage.getActions(clickType)) {
                                            targetPage.addAction(clickType, action);
                                        }
                                    }
                                }
                            }
                            
                            target.save();
                            target.showToNearby();
                            player.sendMessage(ColorUtil.colorize("&a成功克隆到 " + input + "！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, input, 0));
                        } else {
                            player.sendMessage(ColorUtil.colorize("&c克隆失败！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        }
                    });
                })
                .build());
        
        setButton(43, GuiButton.builder(Material.ENDER_PEARL)
                .name("&f移动到坐标")
                .lore(Arrays.asList(
                        "&7将全息图移动到指定坐标",
                        "&7格式: x y z [世界]",
                        "",
                        "&e点击输入"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入坐标 (x y z [世界]):", 
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h == null) {
                            player.sendMessage(ColorUtil.colorize("&c全息图不存在！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }
                        
                        String[] parts = input.split(" ");
                        if (parts.length < 3) {
                            player.sendMessage(ColorUtil.colorize("&c格式错误！请输入: x y z [世界]"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }
                        
                        try {
                            double x = Double.parseDouble(parts[0]);
                            double y = Double.parseDouble(parts[1]);
                            double z = Double.parseDouble(parts[2]);
                            World world = parts.length > 3 ? Bukkit.getWorld(parts[3]) : h.getLocation().getWorld();
                            
                            if (world == null) {
                                player.sendMessage(ColorUtil.colorize("&c世界不存在！"));
                            } else {
                                Location loc = new Location(world, x, y, z, h.getLocation().getYaw(), h.getLocation().getPitch());
                                h.setLocation(loc);
                                h.save();
                                h.showToNearby();
                                player.sendMessage(ColorUtil.colorize("&a已移动到 " + world.getName() + " (" + x + ", " + y + ", " + z + ")！"));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c坐标格式错误！"));
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(44, GuiButton.builder(Material.WRITABLE_BOOK)
                .name("&f插入行")
                .lore(Arrays.asList(
                        "&7在指定位置插入行",
                        "&7格式: 行号 内容",
                        "",
                        "&e点击输入"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入 (行号 内容):", 
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h == null) {
                            player.sendMessage(ColorUtil.colorize("&c全息图不存在！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }
                        
                        int spaceIndex = input.indexOf(' ');
                        if (spaceIndex <= 0) {
                            player.sendMessage(ColorUtil.colorize("&c格式错误！请输入: 行号 内容"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                            return;
                        }
                        
                        try {
                            int lineNum = Integer.parseInt(input.substring(0, spaceIndex));
                            String content = input.substring(spaceIndex + 1);
                            
                            HologramPage p = h.getPage(currentPageIndex);
                            if (p == null) {
                                player.sendMessage(ColorUtil.colorize("&c页面不存在！"));
                            } else if (lineNum < 1 || lineNum > p.size() + 1) {
                                player.sendMessage(ColorUtil.colorize("&c行号必须在 1 到 " + (p.size() + 1) + " 之间！"));
                            } else {
                                p.insertLine(lineNum - 1, content);
                                h.save();
                                h.showToNearby();
                                player.sendMessage(ColorUtil.colorize("&a已在第 " + lineNum + " 行插入内容！"));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c行号必须是数字！"));
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(46, GuiButton.builder(Material.REDSTONE_BLOCK)
                .name("&f删除全息图")
                .lore(Arrays.asList(
                        "&7删除此全息图",
                        "",
                        "&e点击删除"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.createDeleteConfirm(hologramName, confirmed -> {
                        if (confirmed) {
                            plugin.getHologramManager().deleteHologram(hologramName);
                            player.sendMessage(ColorUtil.colorize("&a已删除全息图 " + hologramName + "！"));
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
                        } else {
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                        }
                    }));
                })
                .build());
        
        setButton(47, GuiButton.builder(Material.ENDER_EYE)
                .name("&f设置范围")
                .lore(Arrays.asList(
                        "&7当前范围: &f" + hologram.getDisplayRange() + " 格",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入显示范围 (格):", 
                            ChatInputManager.InputType.DISPLAY_RANGE, hologramName, input -> {
                        try {
                            int range = Integer.parseInt(input);
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                h.setDisplayRange(range);
                                h.save();
                                player.sendMessage(ColorUtil.colorize("&a已设置显示范围为 " + range + " 格！"));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(48, GuiButton.builder(Material.TRIPWIRE_HOOK)
                .name("&f设置权限")
                .lore(Arrays.asList(
                        "&7当前权限: &f" + (hologram.getPermission() != null ? hologram.getPermission() : "无"),
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入权限节点 (输入 clear 清除):", 
                            ChatInputManager.InputType.PERMISSION, hologramName, input -> {
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            if (input.equalsIgnoreCase("clear")) {
                                h.setPermission(null);
                                player.sendMessage(ColorUtil.colorize("&a已清除权限！"));
                            } else {
                                h.setPermission(input);
                                player.sendMessage(ColorUtil.colorize("&a已设置权限为 " + input + "！"));
                            }
                            h.save();
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(50, GuiButton.builder(Material.CLOCK)
                .name("&f设置间隔")
                .lore(Arrays.asList(
                        "&7当前间隔: &f" + hologram.getUpdateInterval() + " tick",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入更新间隔 (tick):", 
                            ChatInputManager.InputType.UPDATE_INTERVAL, hologramName, input -> {
                        try {
                            int interval = Integer.parseInt(input);
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                h.setUpdateInterval(interval);
                                h.save();
                                player.sendMessage(ColorUtil.colorize("&a已设置更新间隔为 " + interval + " tick！"));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                        }
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                    });
                })
                .build());
        
        setButton(51, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&f动作管理")
                .lore(Arrays.asList(
                        "&7管理点击动作",
                        "",
                        "&e点击管理"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex));
                })
                .build());
        
        fillLastTwoRows();
    }
    
    private void fillLastTwoRows() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 45; i < 54; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }

    private GuiButton createLineButton(Hologram hologram, int lineIndex, HologramLine line) {
        HologramType type = line.getType();
        Material material;
        String typeDisplay;
        
        switch (type) {
            case ICON:
                material = Material.ITEM_FRAME;
                typeDisplay = "&b物品图标";
                break;
            case HEAD:
                material = Material.PLAYER_HEAD;
                typeDisplay = "&d玩家头颅";
                break;
            case SMALLHEAD:
                material = Material.PLAYER_HEAD;
                typeDisplay = "&d小型头颅";
                break;
            case ENTITY:
                material = Material.ZOMBIE_HEAD;
                typeDisplay = "&c实体显示";
                break;
            case NEXT:
                material = Material.ARROW;
                typeDisplay = "&a下一页按钮";
                break;
            case PREV:
                material = Material.ARROW;
                typeDisplay = "&e上一页按钮";
                break;
            case TEXT:
            default:
                material = Material.PAPER;
                typeDisplay = "&f文本";
                break;
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7类型: " + typeDisplay);
        lore.add("&7内容: &r" + (line.getContent().length() > 30 ? line.getContent().substring(0, 30) + "..." : line.getContent()));
        lore.add("&7偏移: &f" + String.format("%.2f, %.2f, %.2f", line.getOffsetX(), line.getOffsetY(), line.getOffsetZ()));
        lore.add("&7高度: &f" + line.getHeight());
        lore.add("");
        lore.add("&e点击编辑");
        
        return GuiButton.builder(material)
                .name("&f第 " + (lineIndex + 1) + " 行")
                .lore(lore)
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex, lineIndex));
                })
                .build();
    }
}
