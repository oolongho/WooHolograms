package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import com.oolongho.holograms.util.Profiler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class HologramListGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private int currentPage;
    private SortType sortType;
    private static final int ITEMS_PER_PAGE = 45;
    private static final int START_SLOT = 9;

    private final Player viewer;

    public HologramListGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, int page) {
        this(plugin, guiManager, chatInputManager, page, SortType.NAME, null);
    }

    public HologramListGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, int page, SortType sortType) {
        this(plugin, guiManager, chatInputManager, page, sortType, null);
    }

    public HologramListGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, int page, SortType sortType, Player viewer) {
        super("hologram_list", ColorUtil.colorize("&8全息图列表"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.currentPage = page;
        this.sortType = sortType;
        this.viewer = viewer;
        
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
                    if (!player.hasPermission("wooholograms.command.reload")) {
                        player.sendMessage(ColorUtil.colorize("&c你没有权限执行此操作！"));
                        return;
                    }
                    plugin.getConfigManager().reload();
                    plugin.getMessages().reload();
                    plugin.getStorage().reload();
                    plugin.getHologramManager().reload();
                    
                    player.sendMessage(ColorUtil.colorize("&a配置已重新加载！"));
                    guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
                })
                .build());

        setButton(2, GuiButton.builder(Material.COMPARATOR)
                .name("&f性能分析")
                .lore(Arrays.asList(
                        "&7查看性能分析数据",
                        "&7状态: " + (Profiler.getInstance().isEnabled() ? "&a启用" : "&c禁用"),
                        "",
                        "&e点击查看"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ProfilerGui(plugin, guiManager, chatInputManager));
                })
                .build());

        // slot 3 由 fillFirstRow 填充为背景

        setButton(4, GuiButton.builder(Material.EMERALD)
                .name("&f创建全息图")
                .lore(Arrays.asList(
                        "&7点击创建一个新的全息图",
                        "",
                        "&e点击创建"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.create")) {
                        player.sendMessage(ColorUtil.colorize("&c你没有权限执行此操作！"));
                        return;
                    }
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入全息图名称:", ChatInputManager.InputType.HOLOGRAM_NAME, input -> {
                        if (plugin.getHologramManager().containsHologram(input)) {
                            player.sendMessage(ColorUtil.colorize("&c全息图 " + input + " 已存在！"));
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
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
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
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
        sortHolograms(holograms, viewer);
        
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
                        guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage - 1, sortType, viewer));
                    })
                    .build());
        }
        
        setButton(47, GuiButton.builder(Material.HOPPER)
                .name("&f排序方式")
                .lore(Arrays.asList(
                        "",
                        "&7当前: &f" + sortType.getDisplayName(),
                        "",
                        "&7点击切换排序方式"
                ))
                .onClick(context -> {
                    SortType nextSortType = sortType.next();
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0, nextSortType, viewer));
                })
                .build());
        
        setButton(49, GuiButton.builder(Material.PAPER)
                .name("&f第 " + (currentPage + 1) + "/" + totalPages + " 页")
                .lore(Arrays.asList(
                        "",
                        "&7共 &f" + holograms.size() + " &7个全息图"
                ))
                .build());

        setButton(51, GuiButton.builder(Material.ENDER_CHEST)
                .name("&f数据导入")
                .lore(Arrays.asList(
                        "&7从其他插件导入数据",
                        "&7支持: HD, CMI, DH",
                        "",
                        "&e点击导入"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ConvertGui(plugin, guiManager, chatInputManager));
                })
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
                        guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage + 1, sortType, viewer));
                    })
                    .build());
        }
        
        fillLastRow();
    }
    
    private void sortHolograms(List<Hologram> holograms, Player viewer) {
        switch (sortType) {
            case NAME -> holograms.sort(Comparator.comparing(Hologram::getName, String.CASE_INSENSITIVE_ORDER));
            case DISTANCE -> {
                if (viewer != null) {
                    Location playerLoc = viewer.getLocation();
                    holograms.sort((h1, h2) -> {
                        Location loc1 = h1.getLocation();
                        Location loc2 = h2.getLocation();
                        if (loc1 == null && loc2 == null) return 0;
                        if (loc1 == null) return 1;
                        if (loc2 == null) return -1;
                        if (loc1.getWorld() != playerLoc.getWorld()) return 1;
                        if (loc2.getWorld() != playerLoc.getWorld()) return -1;
                        return Double.compare(playerLoc.distanceSquared(loc1), playerLoc.distanceSquared(loc2));
                    });
                }
            }
            case ENABLED -> holograms.sort((h1, h2) -> {
                int cmp = Boolean.compare(h2.isEnabled(), h1.isEnabled());
                if (cmp != 0) return cmp;
                return h1.getName().compareToIgnoreCase(h2.getName());
            });
            case LINES -> holograms.sort((h1, h2) -> {
                int lines1 = getTotalLines(h1);
                int lines2 = getTotalLines(h2);
                int cmp = Integer.compare(lines2, lines1);
                if (cmp != 0) return cmp;
                return h1.getName().compareToIgnoreCase(h2.getName());
            });
        }
    }
    
    private int getTotalLines(Hologram hologram) {
        int total = 0;
        for (int i = 0; i < hologram.getPageCount(); i++) {
            HologramPage page = hologram.getPage(i);
            if (page != null) {
                total += page.size();
            }
        }
        return total;
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
    
    public enum SortType {
        NAME("按名称排序"),
        DISTANCE("按距离排序"),
        ENABLED("按状态排序"),
        LINES("按行数排序");
        
        private final String displayName;
        
        SortType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public SortType next() {
            SortType[] values = values();
            int nextIndex = (this.ordinal() + 1) % values.length;
            return values[nextIndex];
        }
    }
}
