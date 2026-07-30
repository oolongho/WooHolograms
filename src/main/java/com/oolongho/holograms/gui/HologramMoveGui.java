package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全息图移动菜单
 * 支持移动到坐标和 X/Y/Z 轴增量微调
 */
public class HologramMoveGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;

    public HologramMoveGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, String hologramName) {
        super("hologram_move", plugin.getMessages().get("gui.title-hologram-move", "name", hologramName), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        setupButtons();
    }

    private void setupButtons() {
        // 填充背景
        for (int i = 0; i < 27; i++) {
            setButton(i, GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("<reset>")
                    .build());
        }

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(plugin.getMessages().getString("gui.lore-back-detail")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());

        // 移动到坐标
        setButton(4, GuiButton.builder(Material.ENDER_PEARL)
                .name(plugin.getMessages().getString("gui.move.to-coords"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.move.to-coords-desc-1"),
                        plugin.getMessages().getString("gui.move.to-coords-desc-2"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-input")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.move-coords"),
                            ChatInputManager.InputType.GENERIC, hologramName, input -> {
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h == null) {
                                    plugin.getMessages().send(player, "gui.msg-hologram-not-exists");
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                                    return;
                                }

                                String[] parts = input.split(" ");
                                if (parts.length < 3) {
                                    plugin.getMessages().send(player, "gui.msg-format-error-xyz-world");
                                    guiManager.openGui(player, new HologramMoveGui(plugin, guiManager, chatInputManager, hologramName));
                                    return;
                                }

                                try {
                                    double x = Double.parseDouble(parts[0]);
                                    double y = Double.parseDouble(parts[1]);
                                    double z = Double.parseDouble(parts[2]);
                                    World world = parts.length > 3 ? Bukkit.getWorld(parts[3]) : h.getLocation().getWorld();

                                    if (world == null) {
                                        plugin.getMessages().send(player, "gui.msg-world-not-found");
                                    } else {
                                        Location loc = new Location(world, x, y, z, h.getLocation().getYaw(), h.getLocation().getPitch());
                                        h.teleport(loc);
                                        h.save();
                                        plugin.getMessages().send(player, "gui.msg-moved-to",
                                                "world", world.getName(), "x", String.valueOf(x),
                                                "y", String.valueOf(y), "z", String.valueOf(z));
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-coords-format-error");
                                }
                                guiManager.openGui(player, new HologramMoveGui(plugin, guiManager, chatInputManager, hologramName));
                            });
                })
                .build());

        // X 轴移动
        setButton(12, createAxisButton(Material.RED_STAINED_GLASS_PANE, plugin.getMessages().getString("gui.move.x-axis"), 'x'));

        // 当前坐标显示
        setButton(13, createCoordinateButton());

        // Z 轴移动
        setButton(14, createAxisButton(Material.BLUE_STAINED_GLASS_PANE, plugin.getMessages().getString("gui.move.z-axis"), 'z'));

        // Y 轴移动
        setButton(22, createAxisButton(Material.GREEN_STAINED_GLASS_PANE, plugin.getMessages().getString("gui.move.y-axis"), 'y'));
    }

    private GuiButton createCoordinateButton() {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        List<String> lore = new ArrayList<>();
        if (h != null) {
            Location loc = h.getLocation();
            if (loc.getWorld() != null) {
                lore.add(plugin.getMessages().getString("gui.move.world", "world", loc.getWorld().getName()));
            }
            lore.add(plugin.getMessages().getString("gui.move.x-coord", "value", String.format("%.1f", loc.getX())));
            lore.add(plugin.getMessages().getString("gui.move.y-coord", "value", String.format("%.1f", loc.getY())));
            lore.add(plugin.getMessages().getString("gui.move.z-coord", "value", String.format("%.1f", loc.getZ())));
        } else {
            lore.add(plugin.getMessages().getString("gui.move.hologram-not-exists"));
        }
        return GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.move.current-coords"))
                .lore(lore)
                .build();
    }

    private GuiButton createAxisButton(Material material, String name, char axis) {
        return GuiButton.builder(material)
                .name(name)
                .lore(buildAxisLore(axis))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h == null) {
                        plugin.getMessages().send(player, "gui.msg-hologram-not-exists");
                        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                        return;
                    }

                    double delta = getDelta(context.getClickType(), player.isSneaking());
                    if (delta == 0) return;

                    Location loc = h.getLocation();
                    double newX = loc.getX(), newY = loc.getY(), newZ = loc.getZ();
                    switch (axis) {
                        case 'x' -> newX += delta;
                        case 'y' -> newY += delta;
                        case 'z' -> newZ += delta;
                    }

                    Location newLoc = new Location(loc.getWorld(), newX, newY, newZ, loc.getYaw(), loc.getPitch());
                    h.teleport(newLoc);
                    h.save();

                    // 更新坐标显示和当前轴按钮 Lore
                    int axisSlot = axis == 'x' ? 12 : axis == 'y' ? 22 : 14;
                    setButton(13, createCoordinateButton());
                    setButton(axisSlot, createAxisButton(material, name, axis));
                    refresh(player);
                })
                .build();
    }

    private List<String> buildAxisLore(char axis) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        List<String> lore = new ArrayList<>();
        if (h != null) {
            Location loc = h.getLocation();
            double val = switch (axis) {
                case 'x' -> loc.getX();
                case 'y' -> loc.getY();
                case 'z' -> loc.getZ();
                default -> 0;
            };
            lore.add(plugin.getMessages().getString("gui.move.current-value", "value", String.format("%.1f", val)));
            lore.add("");
        }
        lore.add(plugin.getMessages().getString("gui.move.axis-left-click"));
        lore.add(plugin.getMessages().getString("gui.move.axis-right-click"));
        lore.add(plugin.getMessages().getString("gui.move.axis-shift-left-click"));
        lore.add(plugin.getMessages().getString("gui.move.axis-shift-right-click"));
        return lore;
    }

    private double getDelta(org.bukkit.event.inventory.ClickType clickType, boolean sneaking) {
        boolean shift = clickType == org.bukkit.event.inventory.ClickType.SHIFT_LEFT
                     || clickType == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT;
        double step = shift ? 1.0 : 0.1;
        return switch (clickType) {
            case LEFT, SHIFT_LEFT -> step;
            case RIGHT, SHIFT_RIGHT -> -step;
            default -> 0;
        };
    }
}
