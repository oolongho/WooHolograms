package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 页面管理 GUI
 * 用于管理全息图的页面
 * 
 * @author oolongho
 */
public class PageManageGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;

    public PageManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName) {
        super("page_manage", ColorUtil.colorize("&8页面管理: " + hologramName), 54);
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
        
        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologramName)
                .lore(Arrays.asList(
                        "",
                        "&7总页数: &f" + hologram.getPageCount(),
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
            
            setButton(slot, GuiButton.builder(Material.BOOK)
                    .name("&f第 " + (i + 1) + " 页")
                    .lore(Arrays.asList(
                            "",
                            "&7行数: &f" + lineCount,
                            "",
                            "&e左键点击查看",
                            "&c右键点击删除"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (context.getClickType().isRightClick()) {
                            if (pageCount <= 1) {
                                player.sendMessage(ColorUtil.colorize("&c至少需要保留一页！"));
                                return;
                            }
                            guiManager.openGui(player, ConfirmGui.createDeletePageConfirm(hologramName, pageIndex + 1, confirmed -> {
                                if (confirmed) {
                                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                    if (h != null) {
                                        h.removePage(pageIndex);
                                        h.save();
                                        h.showToNearby();
                                        player.sendMessage(ColorUtil.colorize("&a已删除第 " + (pageIndex + 1) + " 页！"));
                                    }
                                    guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                } else {
                                    guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                }
                            }));
                        } else {
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                        }
                    })
                    .build());
        }
        
        setButton(45, GuiButton.builder(Material.EMERALD)
                .name("&f添加页面")
                .lore(Arrays.asList(
                        "&7在末尾添加新页面",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        HologramPage newPage = h.addPage();
                        if (newPage != null) {
                            h.save();
                            player.sendMessage(ColorUtil.colorize("&a已添加新页面！"));
                            guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                        } else {
                            player.sendMessage(ColorUtil.colorize("&c添加页面失败！"));
                        }
                    }
                })
                .build());
        
        setButton(49, GuiButton.builder(Material.COMPASS)
                .name("&f快速跳转")
                .lore(Arrays.asList(
                        "&7跳转到指定页面",
                        "",
                        "&e点击跳转"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入页码:", 
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                        try {
                            int pageNum = Integer.parseInt(input);
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                if (pageNum < 1 || pageNum > h.getPageCount()) {
                                    player.sendMessage(ColorUtil.colorize("&c页码超出范围！有效范围: 1-" + h.getPageCount()));
                                    guiManager.openGui(player, new PageManageGui(plugin, guiManager, chatInputManager, hologramName));
                                } else {
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageNum - 1));
                                }
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
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
        
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
