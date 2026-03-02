package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全息图详情 GUI
 * 显示单个全息图的详细信息和操作按钮
 * 
 * @author oolongho
 */
public class HologramDetailGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private int currentPageIndex;
    private static final int LINES_PER_PAGE = 27;

    public HologramDetailGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName, int pageIndex) {
        super("hologram_detail", ColorUtil.colorize("&8全息图: " + hologramName), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.currentPageIndex = pageIndex;
        
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
        
        setButton(0, GuiButton.builder(Material.BOOK)
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
        
        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologram.getName())
                .lore(Arrays.asList(
                        "",
                        "&7状态: " + (hologram.isEnabled() ? "&a启用" : "&c禁用"),
                        "&7页面: &f" + hologram.getPageCount(),
                        "&7显示范围: &f" + hologram.getDisplayRange() + " 格",
                        "&7更新间隔: &f" + hologram.getUpdateInterval() + " tick",
                        "&7权限: &f" + (hologram.getPermission() != null ? hologram.getPermission() : "无"),
                        ""
                ))
                .build());
        
        HologramPage page = hologram.getPage(currentPageIndex);
        if (page != null) {
            int lineCount = page.size();
            int totalPages = (int) Math.ceil((double) lineCount / LINES_PER_PAGE);
            if (totalPages == 0) totalPages = 1;
            
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
        
        setButton(39, GuiButton.builder(Material.COMPASS)
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
        setButton(40, GuiButton.builder(Material.RECOVERY_COMPASS)
                .name("&f朝向设置")
                .lore(Arrays.asList(
                        "&7设置全息图的朝向模式",
                        "&7当前: &f" + facingDisplay,
                        "",
                        "&7模式说明:",
                        "&7- 固定角度: 固定朝向",
                        "&7- 水平跟随: 水平跟随玩家",
                        "&7- 垂直跟随: 垂直跟随玩家",
                        "&7- 完全跟随: 完全跟随玩家",
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
        
        setButton(46, GuiButton.builder(Material.REDSTONE_BLOCK)
                .name("&f删除")
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
        
        if (hologram.getPageCount() > 1) {
            if (currentPageIndex > 0) {
                setButton(45, GuiButton.builder(Material.ARROW)
                        .name("&f上一页")
                        .lore(Arrays.asList(
                                "&7当前: 第 " + (currentPageIndex + 1) + " 页",
                                "&7点击查看上一页"
                        ))
                        .onClick(context -> {
                            guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex - 1));
                        })
                        .build());
            }
            
            setButton(49, GuiButton.builder(Material.BOOK)
                    .name("&f页面管理")
                    .lore(Arrays.asList(
                            "&7当前: 第 " + (currentPageIndex + 1) + " / " + hologram.getPageCount() + " 页",
                            "",
                            "&e点击管理页面"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                    })
                    .build());
            
            if (currentPageIndex < hologram.getPageCount() - 1) {
                setButton(53, GuiButton.builder(Material.ARROW)
                        .name("&f下一页")
                        .lore(Arrays.asList(
                                "&7当前: 第 " + (currentPageIndex + 1) + " 页",
                                "&7点击查看下一页"
                        ))
                        .onClick(context -> {
                            guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex + 1));
                        })
                        .build());
            }
            
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
        }
    }

    private GuiButton createLineButton(Hologram hologram, int lineIndex, HologramLine line) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7内容: " + line.getContent());
        lore.add("&7偏移: &f" + String.format("%.2f, %.2f, %.2f", line.getOffsetX(), line.getOffsetY(), line.getOffsetZ()));
        lore.add("&7高度: &f" + line.getHeight());
        lore.add("");
        lore.add("&e点击编辑");
        
        return GuiButton.builder(Material.PAPER)
                .name("&f第 " + (lineIndex + 1) + " 行")
                .lore(lore)
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, currentPageIndex, lineIndex));
                })
                .build();
    }
}
