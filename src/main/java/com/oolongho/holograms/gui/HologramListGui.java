package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
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
        super("hologram_list", plugin.getMessages().get("gui.title-list"), 54);
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
                .name(plugin.getMessages().getString("gui.hologram-list.btn-reload"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-reload"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-reload")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.reload")) {
                        plugin.getMessages().send(player, "gui.msg-no-permission");
                        return;
                    }
                    plugin.getConfigManager().reload();
                    plugin.getMessages().reload();
                    plugin.getStorage().reload();
                    plugin.getHologramManager().reload();

                    plugin.getMessages().send(player, "gui.msg-reloaded");
                    guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
                })
                .build());

        setButton(2, GuiButton.builder(Material.COMPARATOR)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-profiler"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-profiler"),
                        plugin.getMessages().getString("gui.hologram-list.lore-profiler-state",
                                "state", plugin.getMessages().getRaw(Profiler.getInstance().isEnabled() ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-view")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ProfilerGui(plugin, guiManager, chatInputManager));
                })
                .build());

        // slot 3 由 fillFirstRow 填充为背景

        setButton(4, GuiButton.builder(Material.EMERALD)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-create"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-create"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-add")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (!player.hasPermission("wooholograms.command.create")) {
                        plugin.getMessages().send(player, "gui.msg-no-permission");
                        return;
                    }
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.hologram-name"),
                            ChatInputManager.InputType.HOLOGRAM_NAME, input -> {
                        if (plugin.getHologramManager().containsHologram(input)) {
                            plugin.getMessages().send(player, "gui.msg-hologram-exists", "name", input);
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
                            return;
                        }

                        Location loc = player.getLocation();
                        Hologram newHologram = plugin.getHologramManager().createHologram(input, loc);

                        if (newHologram != null) {
                            HologramPage page = newHologram.getPage(0);
                            if (page != null) {
                                page.addLine(plugin.getMessages().getRaw("gui.default-line-text"));
                            }
                            newHologram.save();
                            newHologram.showToNearby();

                            plugin.getMessages().send(player, "gui.msg-create-success", "name", input);
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, input, 0));
                        } else {
                            plugin.getMessages().send(player, "gui.msg-create-failed");
                            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage, sortType, viewer));
                        }
                    });
                })
                .build());
        
        setButton(6, GuiButton.builder(Material.KNOWLEDGE_BOOK)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-help"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-help"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-view")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HelpGui(plugin, guiManager, chatInputManager));
                })
                .build());
        
        setButton(8, GuiButton.builder(Material.OAK_SIGN)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-nearby"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-nearby"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-view")))
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
                    .name(plugin.getMessages().getString("gui.hologram-list.btn-prev-page"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.hologram-list.lore-page-current",
                                    "page", String.valueOf(currentPage + 1)),
                            plugin.getMessages().getString("gui.hologram-list.lore-prev-page")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, currentPage - 1, sortType, viewer));
                    })
                    .build());
        }

        setButton(47, GuiButton.builder(Material.HOPPER)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-sort"))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.hologram-list.lore-sort-current",
                                "type", getSortTypeDisplayName()),
                        "",
                        plugin.getMessages().getString("gui.hologram-list.lore-sort-switch")))
                .onClick(context -> {
                    SortType nextSortType = sortType.next();
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0, nextSortType, viewer));
                })
                .build());

        setButton(49, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-page-info",
                        "page", String.valueOf(currentPage + 1), "total", String.valueOf(totalPages)))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.hologram-list.lore-total-count",
                                "count", String.valueOf(holograms.size()))))
                .build());

        setButton(51, GuiButton.builder(Material.ENDER_CHEST)
                .name(plugin.getMessages().getString("gui.hologram-list.btn-import"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.hologram-list.lore-import"),
                        plugin.getMessages().getString("gui.hologram-list.lore-import-support"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-import")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ConvertGui(plugin, guiManager, chatInputManager));
                })
                .build());
        
        if (currentPage < totalPages - 1) {
            setButton(53, GuiButton.builder(Material.ARROW)
                    .name(plugin.getMessages().getString("gui.hologram-list.btn-next-page"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.hologram-list.lore-page-current",
                                    "page", String.valueOf(currentPage + 1)),
                            plugin.getMessages().getString("gui.hologram-list.lore-next-page")))
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

        int totalLines = 0;
        for (int i = 0; i < hologram.getPageCount(); i++) {
            HologramPage page = hologram.getPage(i);
            if (page != null) {
                totalLines += page.size();
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-world", "world", worldName));
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-location", "location", locationStr));
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-state",
                "state", plugin.getMessages().getRaw(hologram.isEnabled() ? "state-enabled" : "state-disabled")));
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-pages", "pages", String.valueOf(hologram.getPageCount())));
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-lines", "lines", String.valueOf(totalLines)));
        lore.add("");
        lore.add(plugin.getMessages().getString("gui.hologram-list.lore-click-detail"));

        return GuiButton.builder(Material.NAME_TAG)
                .name("<white>" + hologram.getName())
                .lore(lore)
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologram.getName(), 0));
                })
                .build();
    }

    private String getSortTypeDisplayName() {
        return switch (sortType) {
            case NAME -> plugin.getMessages().getString("gui.sort-type.name");
            case DISTANCE -> plugin.getMessages().getString("gui.sort-type.distance");
            case ENABLED -> plugin.getMessages().getString("gui.sort-type.enabled");
            case LINES -> plugin.getMessages().getString("gui.sort-type.lines");
        };
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
            plugin.getMessages().send(player, "cmd.near-empty", "range", String.valueOf(range));
        } else {
            plugin.getMessages().send(player, "cmd.near-header", "range", String.valueOf(range));
            for (Hologram hologram : nearbyHolograms) {
                Location loc = hologram.getLocation();
                double distance = playerLoc.distance(loc);
                plugin.getMessages().send(player, "cmd.near-item",
                        "name", hologram.getName(),
                        "distance", String.format("%.1f", distance),
                        "x", String.format("%.1f", loc.getX()),
                        "y", String.format("%.1f", loc.getY()),
                        "z", String.format("%.1f", loc.getZ()));
            }
            plugin.getMessages().send(player, "cmd.near-footer", "count", String.valueOf(nearbyHolograms.size()));
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
        NAME,
        DISTANCE,
        ENABLED,
        LINES;

        public SortType next() {
            SortType[] values = values();
            int nextIndex = (this.ordinal() + 1) % values.length;
            return values[nextIndex];
        }
    }
}
