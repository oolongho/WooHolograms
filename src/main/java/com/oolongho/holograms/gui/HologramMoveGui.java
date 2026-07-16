package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
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
        super("hologram_move", ColorUtil.colorize("&8移动全息图: " + hologramName), 27);
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
                    .name("&r")
                    .build());
        }

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回全息图详情页"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());

        // 移动到坐标
        setButton(4, GuiButton.builder(Material.ENDER_PEARL)
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
                                    guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                                    return;
                                }

                                String[] parts = input.split(" ");
                                if (parts.length < 3) {
                                    player.sendMessage(ColorUtil.colorize("&c格式错误！请输入: x y z [世界]"));
                                    guiManager.openGui(player, new HologramMoveGui(plugin, guiManager, chatInputManager, hologramName));
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
                                        h.teleport(loc);
                                        h.save();
                                        player.sendMessage(ColorUtil.colorize("&a已移动到 " + world.getName() + " (" + x + ", " + y + ", " + z + ")！"));
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.colorize("&c坐标格式错误！"));
                                }
                                guiManager.openGui(player, new HologramMoveGui(plugin, guiManager, chatInputManager, hologramName));
                            });
                })
                .build());

        // X 轴移动
        setButton(12, createAxisButton(Material.RED_STAINED_GLASS_PANE, "&cX 轴", 'x'));

        // 当前坐标显示
        setButton(13, createCoordinateButton());

        // Z 轴移动
        setButton(14, createAxisButton(Material.BLUE_STAINED_GLASS_PANE, "&9Z 轴", 'z'));

        // Y 轴移动
        setButton(22, createAxisButton(Material.GREEN_STAINED_GLASS_PANE, "&aY 轴", 'y'));
    }

    private GuiButton createCoordinateButton() {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        List<String> lore = new ArrayList<>();
        if (h != null) {
            Location loc = h.getLocation();
            if (loc.getWorld() != null) {
                lore.add("&7世界: &f" + loc.getWorld().getName());
            }
            lore.add("&cX: &f" + String.format("%.1f", loc.getX()));
            lore.add("&aY: &f" + String.format("%.1f", loc.getY()));
            lore.add("&9Z: &f" + String.format("%.1f", loc.getZ()));
        } else {
            lore.add("&c全息图不存在");
        }
        return GuiButton.builder(Material.COMPASS)
                .name("&f当前坐标")
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
                        player.sendMessage(ColorUtil.colorize("&c全息图不存在！"));
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
            lore.add("&7当前: &f" + String.format("%.1f", val));
            lore.add("");
        }
        lore.add("&7左键 &a+0.1");
        lore.add("&7右键 &c-0.1");
        lore.add("&7SHIFT+左键 &a+1");
        lore.add("&7SHIFT+右键 &c-1");
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
