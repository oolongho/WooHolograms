package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.HeadTexture;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.hologram.HologramType;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * 行编辑 GUI
 * 用于编辑单行的属性
 *
 * TEXT 行布局（27格 = 3行），GRAY 背景：
 * Row 1 (0-8):   [0返回] [_] [_] [_] [4内容] [_] [_] [_] [_]
 * Row 2 (9-17):  [9设置文本] [_] [_] [_] [13动作管理] [_] [_] [_] [_]
 * Row 3 (18-26): [18上移] [_] [_] [_] [22删除行] [_] [_] [_] [26下移]
 *
 * 非 TEXT 行布局（45格 = 5行），GRAY 背景：
 * Row 1 (0-8):   [0返回] [_] [_] [_] [4内容] [_] [_] [_] [_]
 * Row 2 (9-17):  [9设置文本] [_] [11类型按钮] [_] [13缩放] [_] [15发光颜色] [_] [_]
 * Row 3 (18-26): [18偏移] [_] [20高度] [_] [22朝向] [_] [24Billboard] [_] [_]
 * Row 4 (27-35): [27亮度] [_] [29阴影] [_] [31彩虹渐变] [_] [33动作管理] [_] [_]
 * Row 5 (36-44): [36上移] [_] [_] [_] [40删除行] [_] [_] [_] [44下移]
 */
public class LineEditGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final int lineIndex;

    public LineEditGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                       String hologramName, int pageIndex, int lineIndex) {
        super("line_edit", ColorUtil.colorize("&8编辑行: " + hologramName + " #" + (lineIndex + 1)),
                computeSize(plugin, hologramName, pageIndex, lineIndex));
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.lineIndex = lineIndex;

        render();
    }

    private static int computeSize(WooHolograms plugin, String hologramName, int pageIndex, int lineIndex) {
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram != null) {
            HologramPage page = hologram.getPage(pageIndex);
            if (page != null && lineIndex >= 0 && lineIndex < page.size()) {
                if (page.getLine(lineIndex).getType() == HologramType.TEXT) {
                    return 27;
                }
            }
        }
        return 45;
    }

    // ==================== 辅助方法 ====================

    /**
     * 执行需要全息图、页面和行的操作
     * 自动处理验证，失败时导航到相应 GUI
     *
     * @param player 玩家
     * @param action 要执行的操作
     * @return true 表示操作成功执行，false 表示验证失败（已导航）
     */
    private boolean withHologramLine(Player player, TriConsumer<Hologram, HologramPage, HologramLine> action) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h == null) {
            player.sendMessage(ColorUtil.colorize("&c全息图不存在！"));
            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
            return false;
        }

        HologramPage p = h.getPage(pageIndex);
        if (p == null || lineIndex < 0 || lineIndex >= p.size()) {
            player.sendMessage(ColorUtil.colorize("&c页面或行不存在！"));
            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
            return false;
        }

        HologramLine l = p.getLine(lineIndex);
        if (l == null) {
            player.sendMessage(ColorUtil.colorize("&c行不存在！"));
            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
            return false;
        }

        action.accept(h, p, l);
        return true;
    }

    /**
     * 执行需要全息图和页面的操作（不需要行对象）
     *
     * @param player 玩家
     * @param action 要执行的操作
     * @return true 表示操作成功执行，false 表示验证失败（已导航）
     */
    private boolean withHologramPage(Player player, BiConsumer<Hologram, HologramPage> action) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h == null) {
            player.sendMessage(ColorUtil.colorize("&c全息图不存在！"));
            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
            return false;
        }

        HologramPage p = h.getPage(pageIndex);
        if (p == null || lineIndex < 0 || lineIndex >= p.size()) {
            player.sendMessage(ColorUtil.colorize("&c页面或行不存在！"));
            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
            return false;
        }

        action.accept(h, p);
        return true;
    }

    /**
     * 重新打开当前行编辑 GUI
     */
    private void reopenGui(Player player) {
        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    /**
     * 三参数函数式接口
     */
    @FunctionalInterface
    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    // ==================== 渲染方法 ====================

    private void render() {
        clearButtons();

        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            setButton(13, GuiButton.builder(Material.BARRIER)
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

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex < 0 || lineIndex >= page.size()) {
            setButton(13, GuiButton.builder(Material.BARRIER)
                    .name("&f行不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该行已被删除",
                            "",
                            "&e点击返回详情"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                    })
                    .build());
            return;
        }

        HologramLine line = page.getLine(lineIndex);
        HologramType lineType = line.getType();

        if (lineType == HologramType.TEXT) {
            renderTextLine(line, hologram, page);
        } else {
            renderNonTextLine(line, lineType, hologram, page);
        }
    }

    /**
     * TEXT 行的紧凑布局（27格 = 3行）
     * [0返回] [_] [_] [_] [4内容] [_] [_] [_] [_]
     * [9设置文本] [_] [_] [_] [13动作管理] [_] [_] [_] [_]
     * [18上移] [_] [_] [_] [22删除行] [_] [_] [_] [26下移]
     */
    private void renderTextLine(HologramLine line, Hologram hologram, HologramPage page) {
        // Row 1: 返回 | 内容
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回全息图详情",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                })
                .build());

        setButton(4, GuiButton.builder(Material.PAPER)
                .name("&f当前内容")
                .lore(Arrays.asList(
                        "",
                        "&r" + line.getContent(),
                        ""
                ))
                .build());

        // Row 2: 设置文本 | 动作管理
        setButton(9, GuiButton.builder(Material.OAK_SIGN)
                .name("&f设置文本")
                .lore(Arrays.asList(
                        "&7设置此行的文本内容",
                        "&7支持颜色代码",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入行文本 (支持颜色代码):",
                            ChatInputManager.InputType.LINE_TEXT, hologramName, lineIndex, pageIndex, input -> {
                                if (withHologramPage(player, (h, p) -> {
                                    p.setLine(lineIndex, input);
                                    h.save();
                                    h.refreshAllViewers();
                                    player.sendMessage(ColorUtil.colorize("&a已更新行文本！"));
                                })) {
                                    reopenGui(player);
                                }
                            });
                })
                .build());

        // 动作管理按钮 (slot 13)
        boolean hasActions = line.hasActions();
        setButton(13, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&f动作管理")
                .lore(Arrays.asList(
                        "&7管理此行的点击动作",
                        "&7当前: " + (hasActions ? "&a已设置动作" : "&c未设置动作"),
                        "",
                        "&7可以为行添加点击动作",
                        "&7如执行命令、发送消息、翻页等",
                        "",
                        "&e点击管理"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // Row 3: 上移 | 删除行 | 下移
        if (lineIndex > 0) {
            setButton(18, GuiButton.builder(Material.ARROW)
                    .name("&f上移")
                    .lore(Arrays.asList(
                            "&7将此行向上移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex - 1);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已上移！"));
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex - 1));
                        }
                    })
                    .build());
        }

        setButton(22, GuiButton.builder(Material.BARRIER)
                .name("&f删除行")
                .lore(Arrays.asList(
                        "&7删除此行",
                        "",
                        "&e点击删除"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.createDeleteLineConfirm(hologramName, lineIndex + 1, confirmed -> {
                        if (confirmed) {
                            if (withHologramPage(player, (h, p) -> {
                                p.removeLine(lineIndex);
                                h.save();
                                h.refreshAllViewers();
                                player.sendMessage(ColorUtil.colorize("&a已删除第 " + (lineIndex + 1) + " 行！"));
                            })) {
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                            }
                        } else {
                            reopenGui(player);
                        }
                    }));
                })
                .build());

        if (lineIndex < page.size() - 1) {
            setButton(26, GuiButton.builder(Material.ARROW)
                    .name("&f下移")
                    .lore(Arrays.asList(
                            "&7将此行向下移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex + 1);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已下移！"));
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex + 1));
                        }
                    })
                    .build());
        }

        fillTextLineBackground();
    }

    private void fillTextLineBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 27; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }

    /**
     * 非 TEXT 行的完整布局（45格 = 5行）
     *
     * Row 1 (0-8):   [0返回] [_] [_] [_] [4内容] [_] [_] [_] [_]
     * Row 2 (9-17):  [9设置文本] [_] [11类型按钮] [_] [13缩放] [_] [15发光颜色] [_] [_]
     * Row 3 (18-26): [18偏移] [_] [20高度] [_] [22朝向] [_] [24Billboard] [_] [_]
     * Row 4 (27-35): [27亮度] [_] [29阴影] [_] [31彩虹渐变] [_] [33动作管理] [_] [_]
     * Row 5 (36-44): [36上移] [_] [_] [_] [40删除行] [_] [_] [_] [44下移]
     */
    private void renderNonTextLine(HologramLine line, HologramType lineType, Hologram hologram, HologramPage page) {
        // === Row 1: Navigation & Info ===
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回全息图详情",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                })
                .build());

        setButton(4, GuiButton.builder(Material.PAPER)
                .name("&f当前内容")
                .lore(Arrays.asList(
                        "",
                        "&r" + line.getContent(),
                        ""
                ))
                .build());

        // === Row 2: Content & Management ===
        setButton(9, GuiButton.builder(Material.OAK_SIGN)
                .name("&f设置文本")
                .lore(Arrays.asList(
                        "&7设置此行的文本内容",
                        "&7支持颜色代码",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入行文本 (支持颜色代码):",
                            ChatInputManager.InputType.LINE_TEXT, hologramName, lineIndex, pageIndex, input -> {
                                if (withHologramPage(player, (h, p) -> {
                                    p.setLine(lineIndex, input);
                                    h.save();
                                    h.refreshAllViewers();
                                    player.sendMessage(ColorUtil.colorize("&a已更新行文本！"));
                                })) {
                                    reopenGui(player);
                                }
                            });
                })
                .build());

        // 类型特定按钮 (slot 11)
        renderTypeSpecificButton(line, lineType);

        // 缩放按钮 (slot 13) - 非 TEXT 行支持 x y z
        setButton(13, GuiButton.builder(Material.SLIME_BALL)
                .name("&f缩放")
                .lore(Arrays.asList(
                        "&7设置Display的缩放比例",
                        "&7当前: &f" + String.format("%.2f, %.2f, %.2f",
                                line.getScaleX() != null ? line.getScaleX() : hologram.getScaleX(),
                                line.getScaleY() != null ? line.getScaleY() : hologram.getScaleY(),
                                line.getScaleZ() != null ? line.getScaleZ() : hologram.getScaleZ()),
                        "",
                        "&7左键: &e输入缩放值 (x y z)",
                        "&7右键: &c重置为继承",
                        "",
                        "&7null 时继承全息图级别值"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setScale(null, null, null);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已重置缩放为继承！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, "&a请输入缩放值 (x y z):",
                                ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                    try {
                                        String[] parts = input.split(" ");
                                        if (parts.length == 3) {
                                            float x = Float.parseFloat(parts[0]);
                                            float y = Float.parseFloat(parts[1]);
                                            float z = Float.parseFloat(parts[2]);
                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setScale(x, y, z);
                                                h.save();
                                                h.refreshAllViewers();
                                                player.sendMessage(ColorUtil.colorize("&a已设置缩放为 (" + x + ", " + y + ", " + z + ")！"));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            player.sendMessage(ColorUtil.colorize("&c请输入三个数字，用空格分隔！"));
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                        reopenGui(player);
                                    }
                                });
                    }
                })
                .build());

        // 发光颜色按钮 (slot 15)
        addGlowColorButton(15, line, hologram);

        // === Row 3: Position & Orientation ===
        setButton(18, GuiButton.builder(Material.STICK)
                .name("&f设置偏移")
                .lore(Arrays.asList(
                        "&7设置此行的位置偏移",
                        "&7当前: &f" + String.format("%.2f, %.2f, %.2f", line.getOffsetX(), line.getOffsetY(), line.getOffsetZ()),
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入偏移值 (x y z):",
                            ChatInputManager.InputType.LINE_OFFSET, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    String[] parts = input.split(" ");
                                    if (parts.length == 3) {
                                        double x = Double.parseDouble(parts[0]);
                                        double y = Double.parseDouble(parts[1]);
                                        double z = Double.parseDouble(parts[2]);
                                        if (withHologramLine(player, (h, p, l) -> {
                                            l.setOffsetX(x);
                                            l.setOffsetY(y);
                                            l.setOffsetZ(z);
                                            h.save();
                                            h.realignLines();
                                            player.sendMessage(ColorUtil.colorize("&a已设置偏移为 (" + x + ", " + y + ", " + z + ")！"));
                                        })) {
                                            reopenGui(player);
                                        }
                                    } else {
                                        player.sendMessage(ColorUtil.colorize("&c请输入三个数字，用空格分隔！"));
                                        reopenGui(player);
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                    reopenGui(player);
                                }
                            });
                })
                .build());

        // 高度按钮 (slot 20)
        addHeightButton(20, line);

        // 朝向设置 (slot 22, SPYGLASS)
        Float customYaw = line.getCustomYaw();
        Float customPitch = line.getCustomPitch();
        String facingDisplay;
        if (customYaw != null || customPitch != null) {
            facingDisplay = "自定义: " +
                    (customYaw != null ? String.format("%.1f", customYaw) : "-") + " / " +
                    (customPitch != null ? String.format("%.1f", customPitch) : "-");
        } else {
            facingDisplay = "跟随整体";
        }

        setButton(22, GuiButton.builder(Material.SPYGLASS)
                .name("&f朝向设置")
                .lore(Arrays.asList(
                        "&7设置此行的独立朝向",
                        "&7当前: &f" + facingDisplay,
                        "",
                        "&7左键: &e设置朝向",
                        "&7右键: &c清空设置",
                        "",
                        "&7格式: yaw pitch",
                        "&7例如: 90 0"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();

                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.clearCustomFacing();
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已清空朝向设置，现在跟随整体！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();

                        chatInputManager.requestInput(player, "&a请输入朝向 (yaw pitch):",
                                ChatInputManager.InputType.LINE_FACING, hologramName, lineIndex, pageIndex, input -> {
                                    try {
                                        String[] parts = input.split(" ");
                                        if (parts.length >= 1) {
                                            float yaw = Float.parseFloat(parts[0]);
                                            float pitch = parts.length >= 2 ? Float.parseFloat(parts[1]) : 0;

                                            if (yaw < -180 || yaw > 180 || pitch < -90 || pitch > 90) {
                                                player.sendMessage(ColorUtil.colorize("&c角度范围无效！yaw: -180~180, pitch: -90~90"));
                                                reopenGui(player);
                                                return;
                                            }

                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setCustomYaw(yaw);
                                                l.setCustomPitch(pitch);
                                                h.save();
                                                h.refreshAllViewers();
                                                player.sendMessage(ColorUtil.colorize("&a已设置朝向为 (" + yaw + ", " + pitch + ")！"));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            player.sendMessage(ColorUtil.colorize("&c请输入 yaw pitch 格式！"));
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                        reopenGui(player);
                                    }
                                });
                    }
                })
                .build());

        // 独立朝向模式 Billboard (slot 24, COMPASS)
        setButton(24, GuiButton.builder(Material.COMPASS)
                .name("&f独立 Billboard")
                .lore(Arrays.asList(
                        "&7设置此行的独立Billboard模式",
                        "&7当前: &f" + (line.getBillboard() != null ? line.getBillboard().getDisplayName() : "跟随整体"),
                        "",
                        "&e左键 &7选择模式",
                        "&e右键 &7重置为跟随整体"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();

                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setBillboard(null);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已重置为跟随整体朝向！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        guiManager.openGui(player, new LineBillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                    }
                })
                .build());

        // === Row 4: Visual & Actions ===
        // 亮度设置 (slot 27)
        Brightness brightness = line.getBrightness();
        String brightnessDisplay = brightness != null && !brightness.isDefault()
                ? brightness.getSkyLight() + "/" + brightness.getBlockLight()
                : "默认";
        setButton(27, GuiButton.builder(Material.GLOWSTONE)
                .name("&f亮度设置")
                .lore(Arrays.asList(
                        "&7设置此行的亮度等级",
                        "&7当前: &f" + brightnessDisplay,
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex, true));
                })
                .build());

        // 阴影按钮 (slot 29)
        Float shadowRadius = line.getShadowRadius();
        Float shadowStrength = line.getShadowStrength();
        String shadowDisplay;
        if (shadowRadius != null || shadowStrength != null) {
            shadowDisplay = String.format("%.2f / %.2f &a(自定义)",
                    shadowRadius != null ? shadowRadius : hologram.getShadowRadius(),
                    shadowStrength != null ? shadowStrength : hologram.getShadowStrength());
        } else {
            shadowDisplay = String.format("&7继承 &f(%.2f / %.2f)",
                    hologram.getShadowRadius(), hologram.getShadowStrength());
        }

        setButton(29, GuiButton.builder(Material.GRAY_DYE)
                .name("&f阴影")
                .lore(Arrays.asList(
                        "&7设置Display的阴影属性",
                        "&7当前: &f" + shadowDisplay,
                        "",
                        "&7左键: &e输入阴影值 (半径 强度)",
                        "&7右键: &c重置为继承",
                        "",
                        "&7null 时继承全息图级别值"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setShadowRadius(null);
                            l.setShadowStrength(null);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已重置阴影为继承！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, "&a请输入阴影值 (半径 强度):",
                                ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                    try {
                                        String[] parts = input.split(" ");
                                        if (parts.length == 2) {
                                            float radius = Float.parseFloat(parts[0]);
                                            float strength = Float.parseFloat(parts[1]);
                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setShadowRadius(radius);
                                                l.setShadowStrength(strength);
                                                h.save();
                                                h.refreshAllViewers();
                                                player.sendMessage(ColorUtil.colorize("&a已设置阴影为 (" + radius + " / " + strength + ")！"));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            player.sendMessage(ColorUtil.colorize("&c请输入两个数字，用空格分隔！"));
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                        reopenGui(player);
                                    }
                                });
                    }
                })
                .build());

        // 彩虹渐变按钮 (slot 31)
        addChromaToggleButton(31, line);

        // 动作管理按钮 (slot 33)
        boolean hasActions = line.hasActions();
        setButton(33, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&f动作管理")
                .lore(Arrays.asList(
                        "&7管理此行的点击动作",
                        "&7当前: " + (hasActions ? "&a已设置动作" : "&c未设置动作"),
                        "",
                        "&7可以为行添加点击动作",
                        "&7如执行命令、发送消息、翻页等",
                        "",
                        "&e点击管理"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // === Row 5: Movement ===
        if (lineIndex > 0) {
            setButton(36, GuiButton.builder(Material.ARROW)
                    .name("&f上移")
                    .lore(Arrays.asList(
                            "&7将此行向上移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex - 1);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已上移！"));
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex - 1));
                        }
                    })
                    .build());
        }

        // 删除行按钮 (slot 40)
        setButton(40, GuiButton.builder(Material.BARRIER)
                .name("&f删除行")
                .lore(Arrays.asList(
                        "&7删除此行",
                        "",
                        "&e点击删除"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.createDeleteLineConfirm(hologramName, lineIndex + 1, confirmed -> {
                        if (confirmed) {
                            if (withHologramPage(player, (h, p) -> {
                                p.removeLine(lineIndex);
                                h.save();
                                h.refreshAllViewers();
                                player.sendMessage(ColorUtil.colorize("&a已删除第 " + (lineIndex + 1) + " 行！"));
                            })) {
                                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                            }
                        } else {
                            reopenGui(player);
                        }
                    }));
                })
                .build());

        if (lineIndex < page.size() - 1) {
            setButton(44, GuiButton.builder(Material.ARROW)
                    .name("&f下移")
                    .lore(Arrays.asList(
                            "&7将此行向下移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex + 1);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已下移！"));
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex + 1));
                        }
                    })
                    .build());
        }

        fillNonTextLineBackground();
    }

    /**
     * 渲染类型特定按钮 (slot 11)
     * BLOCK -> STONE 方块类型
     * ICON -> NETHER_STAR 附魔光效
     * HEAD/SMALLHEAD -> PLAYER_HEAD 头颅材质
     * ENTITY -> GRAY_STAINED_GLASS_PANE (无类型特定按钮)
     */
    private void renderTypeSpecificButton(HologramLine line, HologramType lineType) {
        switch (lineType) {
            case BLOCK -> {
                Material blockMat = line.getBlockMaterial();
                setButton(11, GuiButton.builder(Material.STONE)
                        .name("&f方块类型")
                        .lore(Arrays.asList(
                                "&7设置方块显示的材质类型",
                                "&7当前: &f" + (blockMat != null ? blockMat.name() : "STONE"),
                                "",
                                "&e点击设置"
                        ))
                        .onClick(context -> {
                            Player player = context.getPlayer();
                            player.closeInventory();

                            chatInputManager.requestInput(player, "&a请输入方块材质名称 (如 STONE, DIAMOND_BLOCK):",
                                    ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                        Material material = Material.matchMaterial(input.toUpperCase(Locale.ROOT));
                                        if (material == null || !material.isBlock()) {
                                            player.sendMessage(ColorUtil.colorize("&c无效的方块材质名称！"));
                                            reopenGui(player);
                                        } else {
                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setContent("#BLOCK:" + input.toUpperCase(Locale.ROOT));
                                                h.save();
                                                h.refreshAllViewers();
                                                player.sendMessage(ColorUtil.colorize("&a已设置方块类型为 " + input.toUpperCase(Locale.ROOT) + "！"));
                                            })) {
                                                reopenGui(player);
                                            }
                                        }
                                    });
                        })
                        .build());
            }
            case ICON -> {
                boolean hasGlow = line.getContent() != null &&
                        line.getContent().toLowerCase(Locale.ROOT).contains(":glow") ||
                        line.getContent() != null &&
                        line.getContent().toLowerCase(Locale.ROOT).contains(" glow");
                setButton(11, GuiButton.builder(Material.NETHER_STAR)
                        .name("&f附魔光效")
                        .lore(Arrays.asList(
                                "&7设置物品的附魔光效",
                                "&7当前: &f" + (hasGlow ? "&a启用" : "&c禁用"),
                                "",
                                "&e点击切换"
                        ))
                        .onClick(context -> {
                            Player player = context.getPlayer();
                            if (withHologramLine(player, (h, p, l) -> {
                                String content = l.getContent();
                                String newContent;
                                if (content.toLowerCase(Locale.ROOT).contains(":glow")) {
                                    newContent = content.replaceAll("(?i):glow", "");
                                } else if (content.toLowerCase(Locale.ROOT).contains(" glow")) {
                                    newContent = content.replaceAll("(?i) glow", "");
                                } else {
                                    newContent = content + ":glow";
                                }
                                l.setContent(newContent);
                                h.save();
                                h.refreshAllViewers();
                                player.sendMessage(ColorUtil.colorize("&a已" + (hasGlow ? "禁用" : "启用") + "附魔光效！"));
                            })) {
                                reopenGui(player);
                            }
                        })
                        .build());
            }
            case HEAD, SMALLHEAD -> {
                HeadTexture headTexture = line.getHeadTexture();
                String textureDisplay = "未设置";
                if (headTexture != null) {
                    textureDisplay = switch (headTexture.getType()) {
                        case BASE64 -> "URL材质";
                        case PLAYER -> "玩家: " + headTexture.getValue();
                        case HDB -> "HDB: " + headTexture.getValue();
                    };
                }

                setButton(11, GuiButton.builder(Material.PLAYER_HEAD)
                        .name("&f头颅材质设置")
                        .lore(Arrays.asList(
                                "&7设置头颅的材质",
                                "&7当前: &f" + textureDisplay,
                                "",
                                "&7支持格式:",
                                "&7- URL:Base64字符串",
                                "&7- PLAYER:玩家名称",
                                "&7- HDB:头颅数据库ID",
                                "",
                                "&e点击设置"
                        ))
                        .onClick(context -> {
                            Player player = context.getPlayer();
                            player.closeInventory();

                            chatInputManager.requestInput(player, "&a请输入头颅材质 (URL:xxx 或 PLAYER:xxx 或 HDB:xxx):",
                                    ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                        if (withHologramLine(player, (h, p, l) -> {
                                            String prefix = lineType == HologramType.HEAD ? "#HEAD:" : "#SMALLHEAD:";
                                            String newContent = prefix.toUpperCase(Locale.ROOT) + input;
                                            l.setContent(newContent);
                                            h.save();
                                            h.refreshAllViewers();
                                            player.sendMessage(ColorUtil.colorize("&a已设置头颅材质！"));
                                        })) {
                                            reopenGui(player);
                                        }
                                    });
                        })
                        .build());
            }
            default -> {
                // ENTITY 或其他类型，不显示类型特定按钮
            }
        }
    }

    private void fillNonTextLineBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 45; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }

    // ==================== 共享按钮构建方法 ====================

    /**
     * 发光颜色按钮
     */
    private void addGlowColorButton(int slot, HologramLine line, Hologram hologram) {
        Integer glowColor = line.getGlowColor();
        String glowColorDisplay;
        if (glowColor != null) {
            if (glowColor == -1) {
                glowColorDisplay = "&c无发光";
            } else {
                glowColorDisplay = String.format("#%06X &a(自定义)", glowColor & 0xFFFFFF);
            }
        } else {
            int holoGlowColor = hologram.getGlowColor();
            if (holoGlowColor == -1) {
                glowColorDisplay = "&7继承 &f(无发光)";
            } else {
                glowColorDisplay = String.format("&7继承 &f(#%06X)", holoGlowColor & 0xFFFFFF);
            }
        }

        setButton(slot, GuiButton.builder(Material.GLOWSTONE_DUST)
                .name("&f发光颜色")
                .lore(Arrays.asList(
                        "&7设置Display的发光颜色",
                        "&7当前: &f" + glowColorDisplay,
                        "",
                        "&7左键: &e输入颜色值",
                        "&7右键: &c重置为继承",
                        "",
                        "&7支持颜色名称、#RRGGBB 格式",
                        "&7输入 reset 清除发光效果"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setGlowColor(null);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已重置发光颜色为继承！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, "&a请输入发光颜色 (颜色名称/#RRGGBB/reset):",
                                ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                    input = input.trim();
                                    if (input.equalsIgnoreCase("reset")) {
                                        if (withHologramLine(player, (h, p, l) -> {
                                            l.setGlowColor(-1);
                                            h.save();
                                            h.refreshAllViewers();
                                            player.sendMessage(ColorUtil.colorize("&a已清除发光效果！"));
                                        })) {
                                            reopenGui(player);
                                        }
                                    } else {
                                        Integer color = parseGlowColorInput(input);
                                        if (color != null) {
                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setGlowColor(color);
                                                h.save();
                                                h.refreshAllViewers();
                                                if (color == -1) {
                                                    player.sendMessage(ColorUtil.colorize("&a已清除发光效果！"));
                                                } else {
                                                    player.sendMessage(ColorUtil.colorize("&a已设置发光颜色为 #" + String.format("%06X", color & 0xFFFFFF) + "！"));
                                                }
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            player.sendMessage(ColorUtil.colorize("&c无效的颜色格式！支持颜色名称、#RRGGBB 或 reset"));
                                            reopenGui(player);
                                        }
                                    }
                                });
                    }
                })
                .build());
    }

    /**
     * 彩虹渐变切换按钮
     */
    private void addChromaToggleButton(int slot, HologramLine line) {
        boolean chromaEnabled = line.isChromaBackground();
        setButton(slot, GuiButton.builder(Material.PRISMARINE_CRYSTALS)
                .name("&f彩虹渐变")
                .lore(Arrays.asList(
                        "&7彩虹渐变效果",
                        "&7当前: &f" + (chromaEnabled ? "&a启用" : "&c禁用"),
                        "",
                        "&7左键: &e切换启用/禁用",
                        "&7右键: &c重置为继承"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setChromaBackground(null);
                            l.setChromaGlow(null);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已重置彩虹渐变为继承！"));
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        if (withHologramLine(player, (h, p, l) -> {
                            boolean newState = !l.isChromaBackground();
                            l.setChromaBackground(newState);
                            l.setChromaGlow(newState);
                            h.save();
                            h.refreshAllViewers();
                            player.sendMessage(ColorUtil.colorize("&a已" + (newState ? "启用" : "禁用") + "彩虹渐变！"));
                        })) {
                            reopenGui(player);
                        }
                    }
                })
                .build());
    }

    /**
     * 高度按钮
     */
    private void addHeightButton(int slot, HologramLine line) {
        setButton(slot, GuiButton.builder(Material.RAIL)
                .name("&f设置高度")
                .lore(Arrays.asList(
                        "&7设置此行的高度",
                        "&7当前: &f" + line.getHeight(),
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, "&a请输入高度值:",
                            ChatInputManager.InputType.LINE_HEIGHT, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    double height = Double.parseDouble(input);
                                    if (withHologramLine(player, (h, p, l) -> {
                                        l.setHeight(height);
                                        h.save();
                                        h.realignLines();
                                        player.sendMessage(ColorUtil.colorize("&a已设置高度为 " + height + "！"));
                                    })) {
                                        reopenGui(player);
                                    }
                                } catch (NumberFormatException e) {
                                    player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                                    reopenGui(player);
                                }
                            });
                })
                .build());
    }

    // ==================== 工具方法 ====================

    private Integer parseGlowColorInput(String input) {
        if (input == null || input.isEmpty()) return null;
        String trimmed = input.trim();
        if (trimmed.startsWith("#") && trimmed.length() == 7) {
            try {
                int rgb = Integer.parseInt(trimmed.substring(1), 16);
                return rgb & 0xFFFFFF;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (trimmed.length() == 6) {
            try {
                int rgb = Integer.parseInt(trimmed, 16);
                return rgb & 0xFFFFFF;
            } catch (NumberFormatException e) {
                // might be color name
            }
        }
        org.bukkit.Color bukkitColor = matchColorByName(trimmed);
        if (bukkitColor != null) {
            return (bukkitColor.getRed() << 16) | (bukkitColor.getGreen() << 8) | bukkitColor.getBlue();
        }
        return null;
    }

    private org.bukkit.Color matchColorByName(String name) {
        return switch (name.toLowerCase()) {
            case "white" -> org.bukkit.Color.WHITE;
            case "silver", "light_gray" -> org.bukkit.Color.SILVER;
            case "gray" -> org.bukkit.Color.GRAY;
            case "dark_gray" -> org.bukkit.Color.GRAY;
            case "black" -> org.bukkit.Color.BLACK;
            case "red" -> org.bukkit.Color.RED;
            case "dark_red", "maroon" -> org.bukkit.Color.MAROON;
            case "yellow" -> org.bukkit.Color.YELLOW;
            case "olive" -> org.bukkit.Color.OLIVE;
            case "lime" -> org.bukkit.Color.LIME;
            case "green" -> org.bukkit.Color.GREEN;
            case "aqua", "teal" -> org.bukkit.Color.TEAL;
            case "cyan" -> org.bukkit.Color.AQUA;
            case "blue" -> org.bukkit.Color.BLUE;
            case "navy" -> org.bukkit.Color.NAVY;
            case "purple" -> org.bukkit.Color.PURPLE;
            case "fuchsia", "magenta" -> org.bukkit.Color.FUCHSIA;
            case "orange" -> org.bukkit.Color.ORANGE;
            default -> null;
        };
    }
}
