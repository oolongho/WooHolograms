package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HologramListGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private int currentPage;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int START_SLOT = 9;

    public HologramListGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, int page) {
        super("hologram_list", ColorUtil.colorize("&8全息图列表"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.currentPage = page;
        
        render();
    }

    private void render() {
        clearButtons();
        
        setButton(0, GuiButton.builder(Material.CLOCK)
                .name("&f重载配置")
                .lore(Arrays.asList(
                        "&7重载所有配置和全息图",
                        "",
                        "&e点击重载"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    plugin.getConfigManager().reload();
                    plugin.getMessages().reload();
                    plugin.getStorage().reload();
                    plugin.getHologramManager().reload();
                    
                    player.sendMessage(ColorUtil.colorize("&a配置已重新加载！"));
                    guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());
        
        setButton(4, GuiButton.builder(Material.EMERALD)
                .name("&f创建全息图")
                .lore(Arrays.asList(
                        "&7点击创建一个新的全息图",
                        "",
                        "&e点击创建"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入全息图名称:", ChatInputManager.InputType.HOLOGRAM_NAME, input -> {
                        if (plugin.getHologramManager().containsHologram(input)) {
                            player.sendMessage(ColorUtil.colorize("&c全息图 " + input + " 已存在！"));
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage));
                            return;
                        }
                        
                        Location loc = player.getLocation();
                        Hologram newHologram = plugin.getHologramManager().createHologram(input, loc);
                        
                        if (newHologram != null) {
                            HologramPage page = newHologram.getPage(0);
                            if (page != null) {
                                page.addLine("&7请输入文本......");
                            }
                            newHologram.save();
                            newHologram.showToNearby();
                            
                            player.sendMessage(ColorUtil.colorize("&a成功创建全息图 " + input + "！"));
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, input, 0));
                        } else {
                            player.sendMessage(ColorUtil.colorize("&c创建全息图失败！"));
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage));
                        }
                    });
                })
                .build());
        
        setButton(6, GuiButton.builder(Material.KNOWLEDGE_BOOK)
                .name("&f帮助手册")
                .lore(Arrays.asList(
                        "&7查看插件使用说明",
                        "",
                        "&e点击查看"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HelpGui(plugin, guiManager, chatInputManager));
                })
                .build());
        
        setButton(8, GuiButton.builder(Material.OAK_SIGN)
                .name("&f附近全息图")
                .lore(Arrays.asList(
                        "&7查看附近的全息图",
                        "",
                        "&e点击查看"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    showNearbyHolograms(player);
                })
                .build());
        
        fillFirstRow();
        
        List<Hologram> holograms = new ArrayList<>(plugin.getHologramManager().getHolograms());
        int totalPages = (int) Math.ceil((double) holograms.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, holograms.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Hologram hologram = holograms.get(i);
            int slot = START_SLOT + (i - startIndex);
            
            setButton(slot, createHologramButton(hologram));
        }
        
        if (currentPage > 0) {
            setButton(45, GuiButton.builder(Material.ARROW)
                    .name("&f上一页")
                    .lore(Arrays.asList(
                            "&7当前: 第 " + (currentPage + 1) + " 页",
                            "&7点击查看上一页"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage - 1));
                    })
                    .build());
        }
        
        setButton(49, GuiButton.builder(Material.PAPER)
                .name("&f第 " + (currentPage + 1) + "/" + totalPages + " 页")
                .build());
        
        if (currentPage < totalPages - 1) {
            setButton(53, GuiButton.builder(Material.ARROW)
                    .name("&f下一页")
                    .lore(Arrays.asList(
                            "&7当前: 第 " + (currentPage + 1) + " 页",
                            "&7点击查看下一页"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage + 1));
                    })
                    .build());
        }
        
        fillLastRow();
    }

    private GuiButton createHologramButton(Hologram hologram) {
        Location loc = hologram.getLocation();
        String worldName = loc != null && loc.getWorld() != null ? loc.getWorld().getName() : "null";
        String locationStr = loc != null ? String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()) : "null";
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7世界: &f" + worldName);
        lore.add("&7位置: &f" + locationStr);
        lore.add("&7状态: " + (hologram.isEnabled() ? "&a启用" : "&c禁用"));
        lore.add("&7页面: &f" + hologram.getPageCount());
        
        int totalLines = 0;
        for (int i = 0; i < hologram.getPageCount(); i++) {
            HologramPage page = hologram.getPage(i);
            if (page != null) {
                totalLines += page.size();
            }
        }
        lore.add("&7行数: &f" + totalLines);
        lore.add("");
        lore.add("&e点击查看详情");
        
        return GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologram.getName())
                .lore(lore)
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologram.getName(), 0));
                })
                .build();
    }

    private void showNearbyHolograms(Player player) {
        Location playerLoc = player.getLocation();
        int range = 50;
        
        List<Hologram> nearbyHolograms = new ArrayList<>();
        for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
            Location holoLoc = hologram.getLocation();
            if (holoLoc != null && holoLoc.getWorld() != null && 
                holoLoc.getWorld().equals(playerLoc.getWorld()) &&
                playerLoc.distance(holoLoc) <= range) {
                nearbyHolograms.add(hologram);
            }
        }
        
        if (nearbyHolograms.isEmpty()) {
            player.sendMessage(ColorUtil.colorize("&e附近 " + range + " 格内没有全息图。"));
        } else {
            player.sendMessage(ColorUtil.colorize("&e========== &6附近全息图 (" + range + "格) &e=========="));
            for (Hologram hologram : nearbyHolograms) {
                Location loc = hologram.getLocation();
                double distance = playerLoc.distance(loc);
                player.sendMessage(ColorUtil.colorize("&e" + hologram.getName() + 
                        " &7- 距离: " + String.format("%.1f", distance) + " 格"));
            }
            player.sendMessage(ColorUtil.colorize("&e总计: &f" + nearbyHolograms.size() + " 个全息图"));
        }
    }
    
    private void fillFirstRow() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 1; i < 9; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
    
    private void fillLastRow() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 45; i < 54; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
}
