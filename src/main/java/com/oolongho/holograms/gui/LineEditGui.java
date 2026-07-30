package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.HeadTexture;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.hologram.HologramType;
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
        super("line_edit", plugin.getMessages().get("gui.title-line-edit",
                        "name", hologramName, "line", String.valueOf(lineIndex + 1)),
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
            plugin.getMessages().send(player, "gui.msg-hologram-not-exists");
            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
            return false;
        }

        HologramPage p = h.getPage(pageIndex);
        if (p == null || lineIndex < 0 || lineIndex >= p.size()) {
            plugin.getMessages().send(player, "gui.msg-line-not-exists");
            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
            return false;
        }

        HologramLine l = p.getLine(lineIndex);
        if (l == null) {
            plugin.getMessages().send(player, "gui.msg-line-not-exists-alt");
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
            plugin.getMessages().send(player, "gui.msg-hologram-not-exists");
            guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
            return false;
        }

        HologramPage p = h.getPage(pageIndex);
        if (p == null || lineIndex < 0 || lineIndex >= p.size()) {
            plugin.getMessages().send(player, "gui.msg-line-not-exists");
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

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex < 0 || lineIndex >= page.size()) {
            setButton(13, GuiButton.builder(Material.BARRIER)
                    .name(plugin.getMessages().getString("gui.btn-line-not-exists"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.lore-line-deleted"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-back-detail")))
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
     * [0返回] [_] [_] [3偏移] [4内容] [_] [_] [_] [_]
     * [9设置文本] [_] [_] [_] [13动作管理] [_] [_] [_] [_]
     * [18上移] [_] [_] [_] [22删除行] [_] [_] [_] [26下移]
     */
    private void renderTextLine(HologramLine line, Hologram hologram, HologramPage page) {
        // Row 1: 返回 | 偏移 | 内容
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-detail"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                })
                .build());

        // 偏移按钮 (slot 3)
        addOffsetButton(3, line, hologram);

        setButton(4, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.line-edit.btn-current-content"))
                .lore(Arrays.asList(
                        "",
                        "<reset>" + line.getContent(),
                        ""))
                .build());

        // Row 2: 设置文本 | 动作管理
        setButton(9, GuiButton.builder(Material.OAK_SIGN)
                .name(plugin.getMessages().getString("gui.line-edit.btn-set-text"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-set-text"),
                        plugin.getMessages().getString("gui.line-edit.lore-support-color"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-text"),
                            ChatInputManager.InputType.LINE_TEXT, hologramName, lineIndex, pageIndex, input -> {
                                if (withHologramPage(player, (h, p) -> {
                                    p.setLine(lineIndex, input);
                                    h.save();
                                    h.refreshAllViewers();
                                    plugin.getMessages().send(player, "gui.msg-line-update-success");
                                })) {
                                    reopenGui(player);
                                }
                            });
                })
                .build());

        // 动作管理按钮 (slot 13)
        boolean hasActions = line.hasActions();
        setButton(13, GuiButton.builder(Material.COMMAND_BLOCK)
                .name(plugin.getMessages().getString("gui.line-edit.btn-action-manage"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage"),
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-current",
                                "state", plugin.getMessages().getString(hasActions ? "gui.line-edit.action-set" : "gui.line-edit.action-unset")),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-desc1"),
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-desc2"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-manage")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // Row 3: 上移 | 删除行 | 下移
        if (lineIndex > 0) {
            setButton(18, GuiButton.builder(Material.ARROW)
                    .name(plugin.getMessages().getString("gui.line-edit.btn-move-up"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.line-edit.lore-move-up"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-move")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex - 1);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-line-move-up");
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex - 1));
                        }
                    })
                    .build());
        }

        setButton(22, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.line-edit.btn-delete-line"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-delete-line"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-delete")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.createDeleteLineConfirm(plugin, hologramName, lineIndex + 1, confirmed -> {
                        if (confirmed) {
                            if (withHologramPage(player, (h, p) -> {
                                p.removeLine(lineIndex);
                                h.save();
                                h.refreshAllViewers();
                                plugin.getMessages().send(player, "gui.msg-line-remove-success", "line", String.valueOf(lineIndex + 1));
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
                    .name(plugin.getMessages().getString("gui.line-edit.btn-move-down"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.line-edit.lore-move-down"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-move")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex + 1);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-line-move-down");
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
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-back-detail"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                })
                .build());

        setButton(4, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.line-edit.btn-current-content"))
                .lore(Arrays.asList(
                        "",
                        "<reset>" + line.getContent(),
                        ""))
                .build());

        // === Row 2: Content & Management ===
        setButton(9, GuiButton.builder(Material.OAK_SIGN)
                .name(plugin.getMessages().getString("gui.line-edit.btn-set-text"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-set-text"),
                        plugin.getMessages().getString("gui.line-edit.lore-support-color"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-text"),
                            ChatInputManager.InputType.LINE_TEXT, hologramName, lineIndex, pageIndex, input -> {
                                if (withHologramPage(player, (h, p) -> {
                                    p.setLine(lineIndex, input);
                                    h.save();
                                    h.refreshAllViewers();
                                    plugin.getMessages().send(player, "gui.msg-line-update-success");
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
                .name(plugin.getMessages().getString("gui.line-edit.btn-scale"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-scale"),
                        plugin.getMessages().getString("gui.line-edit.lore-scale-current",
                                "scale", String.format("%.2f, %.2f, %.2f",
                                        line.getScaleX() != null ? line.getScaleX() : hologram.getScaleX(),
                                        line.getScaleY() != null ? line.getScaleY() : hologram.getScaleY(),
                                        line.getScaleZ() != null ? line.getScaleZ() : hologram.getScaleZ())),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-scale-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-scale-right"),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-scale-inherit-hint")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setScale(null, null, null);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-scale-reset-inherit");
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.scale"),
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
                                                plugin.getMessages().send(player, "gui.msg-scale-set-line",
                                                        "line", String.valueOf(lineIndex + 1),
                                                        "x", String.valueOf(x),
                                                        "y", String.valueOf(y),
                                                        "z", String.valueOf(z));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            plugin.getMessages().send(player, "gui.msg-format-error-three-num");
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                        reopenGui(player);
                                    }
                                });
                    }
                })
                .build());

        // 发光颜色按钮 (slot 15)
        addGlowColorButton(15, line, hologram);

        // === Row 3: Position & Orientation ===
        // 偏移按钮 (slot 18)
        addOffsetButton(18, line, hologram);

        // 高度按钮 (slot 20)
        addHeightButton(20, line);

        // 朝向设置 (slot 22, SPYGLASS)
        Float customYaw = line.getCustomYaw();
        Float customPitch = line.getCustomPitch();
        String facingDisplay;
        if (customYaw != null || customPitch != null) {
            facingDisplay = plugin.getMessages().getString("gui.line-edit.value-custom-facing",
                    "yaw", customYaw != null ? String.format("%.1f", customYaw) : "-",
                    "pitch", customPitch != null ? String.format("%.1f", customPitch) : "-");
        } else {
            facingDisplay = plugin.getMessages().getString("gui.line-edit.value-follow-parent");
        }

        setButton(22, GuiButton.builder(Material.SPYGLASS)
                .name(plugin.getMessages().getString("gui.line-edit.btn-facing"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-facing"),
                        plugin.getMessages().getString("gui.line-edit.lore-facing-current",
                                "facing", facingDisplay),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-facing-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-facing-right"),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-facing-format"),
                        plugin.getMessages().getString("gui.line-edit.lore-facing-example")))
                .onClick(context -> {
                    Player player = context.getPlayer();

                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.clearCustomFacing();
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-facing-clear");
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();

                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-facing"),
                                ChatInputManager.InputType.LINE_FACING, hologramName, lineIndex, pageIndex, input -> {
                                    try {
                                        String[] parts = input.split(" ");
                                        if (parts.length >= 1) {
                                            float yaw = Float.parseFloat(parts[0]);
                                            float pitch = parts.length >= 2 ? Float.parseFloat(parts[1]) : 0;

                                            if (yaw < -180 || yaw > 180 || pitch < -90 || pitch > 90) {
                                                plugin.getMessages().send(player, "gui.msg-facing-range-error");
                                                reopenGui(player);
                                                return;
                                            }

                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setCustomYaw(yaw);
                                                l.setCustomPitch(pitch);
                                                h.save();
                                                h.refreshAllViewers();
                                                plugin.getMessages().send(player, "gui.msg-facing-set",
                                                        "yaw", String.valueOf(yaw),
                                                        "pitch", String.valueOf(pitch));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            plugin.getMessages().send(player, "gui.msg-format-error-yaw-pitch");
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                        reopenGui(player);
                                    }
                                });
                    }
                })
                .build());

        // 独立朝向模式 Billboard (slot 24, COMPASS)
        setButton(24, GuiButton.builder(Material.COMPASS)
                .name(plugin.getMessages().getString("gui.line-edit.btn-billboard"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-billboard"),
                        plugin.getMessages().getString("gui.line-edit.lore-billboard-current",
                                "mode", line.getBillboard() != null ? plugin.getMessages().getRaw(line.getBillboard().getDisplayNameKey()) : plugin.getMessages().getString("gui.line-edit.value-follow-parent")),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-billboard-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-billboard-right")))
                .onClick(context -> {
                    Player player = context.getPlayer();

                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setBillboard(null);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-facing-reset");
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
                : plugin.getMessages().getString("gui.line-edit.value-default");
        setButton(27, GuiButton.builder(Material.GLOWSTONE)
                .name(plugin.getMessages().getString("gui.line-edit.btn-brightness"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-brightness"),
                        plugin.getMessages().getString("gui.line-edit.lore-brightness-current",
                                "value", brightnessDisplay),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BrightnessSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex, true));
                })
                .build());

        // 阴影按钮 (slot 29)
        Float shadowRadius = line.getShadowRadius();
        Float shadowStrength = line.getShadowStrength();
        String shadowDisplay;
        if (shadowRadius != null || shadowStrength != null) {
            shadowDisplay = plugin.getMessages().getString("gui.line-edit.value-shadow-custom",
                    "radius", String.format("%.2f", shadowRadius != null ? shadowRadius : hologram.getShadowRadius()),
                    "strength", String.format("%.2f", shadowStrength != null ? shadowStrength : hologram.getShadowStrength()));
        } else {
            shadowDisplay = plugin.getMessages().getString("gui.line-edit.value-shadow-inherit",
                    "radius", String.format("%.2f", hologram.getShadowRadius()),
                    "strength", String.format("%.2f", hologram.getShadowStrength()));
        }

        setButton(29, GuiButton.builder(Material.GRAY_DYE)
                .name(plugin.getMessages().getString("gui.line-edit.btn-shadow"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-shadow"),
                        plugin.getMessages().getString("gui.line-edit.lore-shadow-current",
                                "value", shadowDisplay),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-shadow-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-shadow-right"),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-shadow-inherit-hint")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setShadowRadius(null);
                            l.setShadowStrength(null);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-shadow-reset-inherit");
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.shadow"),
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
                                                plugin.getMessages().send(player, "gui.msg-shadow-set-line",
                                                        "line", String.valueOf(lineIndex + 1),
                                                        "radius", String.valueOf(radius),
                                                        "strength", String.valueOf(strength));
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            plugin.getMessages().send(player, "gui.msg-format-error-two-num");
                                            reopenGui(player);
                                        }
                                    } catch (NumberFormatException e) {
                                        plugin.getMessages().send(player, "gui.msg-input-invalid-number");
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
                .name(plugin.getMessages().getString("gui.line-edit.btn-action-manage"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage"),
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-current",
                                "state", plugin.getMessages().getString(hasActions ? "gui.line-edit.action-set" : "gui.line-edit.action-unset")),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-desc1"),
                        plugin.getMessages().getString("gui.line-edit.lore-action-manage-desc2"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-manage")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        // === Row 5: Movement ===
        if (lineIndex > 0) {
            setButton(36, GuiButton.builder(Material.ARROW)
                    .name(plugin.getMessages().getString("gui.line-edit.btn-move-up"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.line-edit.lore-move-up"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-move")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex - 1);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-line-move-up");
                        })) {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex - 1));
                        }
                    })
                    .build());
        }

        // 删除行按钮 (slot 40)
        setButton(40, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.line-edit.btn-delete-line"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-delete-line"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-delete")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.createDeleteLineConfirm(plugin, hologramName, lineIndex + 1, confirmed -> {
                        if (confirmed) {
                            if (withHologramPage(player, (h, p) -> {
                                p.removeLine(lineIndex);
                                h.save();
                                h.refreshAllViewers();
                                plugin.getMessages().send(player, "gui.msg-line-remove-success", "line", String.valueOf(lineIndex + 1));
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
                    .name(plugin.getMessages().getString("gui.line-edit.btn-move-down"))
                    .lore(Arrays.asList(
                            plugin.getMessages().getString("gui.line-edit.lore-move-down"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-move")))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (withHologramPage(player, (h, p) -> {
                            p.swapLines(lineIndex, lineIndex + 1);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-line-move-down");
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
                        .name(plugin.getMessages().getString("gui.line-edit.btn-block-type"))
                        .lore(Arrays.asList(
                                plugin.getMessages().getString("gui.line-edit.lore-block-type"),
                                plugin.getMessages().getString("gui.line-edit.lore-block-type-current",
                                        "material", blockMat != null ? blockMat.name() : "STONE"),
                                "",
                                plugin.getMessages().getString("gui.lore-click-set")))
                        .onClick(context -> {
                            Player player = context.getPlayer();
                            player.closeInventory();

                            chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.block-material"),
                                    ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                        Material material = Material.matchMaterial(input.toUpperCase(Locale.ROOT));
                                        if (material == null || !material.isBlock()) {
                                            plugin.getMessages().send(player, "gui.msg-block-material-invalid");
                                            reopenGui(player);
                                        } else {
                                            if (withHologramLine(player, (h, p, l) -> {
                                                l.setContent("#BLOCK:" + input.toUpperCase(Locale.ROOT));
                                                h.save();
                                                h.refreshAllViewers();
                                                plugin.getMessages().send(player, "gui.msg-block-material-set",
                                                        "material", input.toUpperCase(Locale.ROOT));
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
                        .name(plugin.getMessages().getString("gui.line-edit.btn-glow-effect"))
                        .lore(Arrays.asList(
                                plugin.getMessages().getString("gui.line-edit.lore-glow-effect"),
                                plugin.getMessages().getString("gui.line-edit.lore-glow-effect-current",
                                        "state", plugin.getMessages().getRaw(hasGlow ? "state-enabled" : "state-disabled")),
                                "",
                                plugin.getMessages().getString("gui.lore-click-toggle")))
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
                                plugin.getMessages().send(player, "gui.msg-glow-toggle",
                                        "state", plugin.getMessages().getRaw(hasGlow ? "state-disabled" : "state-enabled"));
                            })) {
                                reopenGui(player);
                            }
                        })
                        .build());
            }
            case HEAD, SMALLHEAD -> {
                HeadTexture headTexture = line.getHeadTexture();
                String textureDisplay;
                if (headTexture == null) {
                    textureDisplay = plugin.getMessages().getString("gui.line-edit.value-head-not-set");
                } else {
                    textureDisplay = switch (headTexture.getType()) {
                        case BASE64 -> plugin.getMessages().getString("gui.line-edit.value-head-url");
                        case PLAYER -> plugin.getMessages().getString("gui.line-edit.value-head-player",
                                "name", headTexture.getValue());
                        case HDB -> plugin.getMessages().getString("gui.line-edit.value-head-hdb",
                                "id", headTexture.getValue());
                    };
                }

                setButton(11, GuiButton.builder(Material.PLAYER_HEAD)
                        .name(plugin.getMessages().getString("gui.line-edit.btn-head-texture"))
                        .lore(Arrays.asList(
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture"),
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture-current",
                                        "texture", textureDisplay),
                                "",
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture-format"),
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture-url"),
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture-player"),
                                plugin.getMessages().getString("gui.line-edit.lore-head-texture-hdb"),
                                "",
                                plugin.getMessages().getString("gui.lore-click-set")))
                        .onClick(context -> {
                            Player player = context.getPlayer();
                            player.closeInventory();

                            chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.head-texture"),
                                    ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                        if (withHologramLine(player, (h, p, l) -> {
                                            String prefix = lineType == HologramType.HEAD ? "#HEAD:" : "#SMALLHEAD:";
                                            String newContent = prefix.toUpperCase(Locale.ROOT) + input;
                                            l.setContent(newContent);
                                            h.save();
                                            h.refreshAllViewers();
                                            plugin.getMessages().send(player, "gui.msg-head-texture-set");
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
     * 偏移按钮（TEXT 和非 TEXT 行共用）
     * 用于设置行的 X/Y/Z 位置偏移
     */
    private void addOffsetButton(int slot, HologramLine line, Hologram hologram) {
        setButton(slot, GuiButton.builder(Material.STICK)
                .name(plugin.getMessages().getString("gui.line-edit.btn-offset"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-offset"),
                        plugin.getMessages().getString("gui.line-edit.lore-offset-current",
                                "offset", String.format("%.2f, %.2f, %.2f", line.getOffsetX(), line.getOffsetY(), line.getOffsetZ())),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-offset-split"),
                        plugin.getMessages().getString("gui.line-edit.lore-offset-split-desc"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-offset"),
                            ChatInputManager.InputType.LINE_OFFSET, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    String[] parts = input.split(" ");
                                    if (parts.length == 3) {
                                        double x = Double.parseDouble(parts[0]);
                                        double y = Double.parseDouble(parts[1]);
                                        double z = Double.parseDouble(parts[2]);
                                        if (withHologramLine(player, (h, p, l) -> {
                                            l.setOffset(x, y, z);
                                            h.save();
                                            plugin.getMessages().send(player, "gui.msg-offset-set-line",
                                                    "line", String.valueOf(lineIndex + 1),
                                                    "x", String.valueOf(x),
                                                    "y", String.valueOf(y),
                                                    "z", String.valueOf(z));
                                        })) {
                                            reopenGui(player);
                                        }
                                    } else {
                                        plugin.getMessages().send(player, "gui.msg-format-error-three-num");
                                        reopenGui(player);
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-input-invalid-number");
                                    reopenGui(player);
                                }
                            });
                })
                .build());
    }

    /**
     * 发光颜色按钮
     */
    private void addGlowColorButton(int slot, HologramLine line, Hologram hologram) {
        Integer glowColor = line.getGlowColor();
        String glowColorDisplay;
        if (glowColor != null) {
            if (glowColor == -1) {
                glowColorDisplay = plugin.getMessages().getString("gui.line-edit.value-glow-none");
            } else {
                glowColorDisplay = plugin.getMessages().getString("gui.line-edit.value-glow-custom",
                        "color", String.format("#%06X", glowColor & 0xFFFFFF));
            }
        } else {
            int holoGlowColor = hologram.getGlowColor();
            if (holoGlowColor == -1) {
                glowColorDisplay = plugin.getMessages().getString("gui.line-edit.value-glow-inherit-none");
            } else {
                glowColorDisplay = plugin.getMessages().getString("gui.line-edit.value-glow-inherit-color",
                        "color", String.format("#%06X", holoGlowColor & 0xFFFFFF));
            }
        }

        setButton(slot, GuiButton.builder(Material.GLOWSTONE_DUST)
                .name(plugin.getMessages().getString("gui.line-edit.btn-glow-color"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color"),
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color-current",
                                "color", glowColorDisplay),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color-right"),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color-format"),
                        plugin.getMessages().getString("gui.line-edit.lore-glow-color-reset")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setGlowColor(null);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-glow-reset-inherit");
                        })) {
                            reopenGui(player);
                        }
                    } else {
                        player.closeInventory();
                        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.glow-color"),
                                ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                                    input = input.trim();
                                    if (input.equalsIgnoreCase("reset")) {
                                        if (withHologramLine(player, (h, p, l) -> {
                                            l.setGlowColor(-1);
                                            h.save();
                                            h.refreshAllViewers();
                                            plugin.getMessages().send(player, "gui.msg-glow-cleared");
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
                                                    plugin.getMessages().send(player, "gui.msg-glow-cleared");
                                                } else {
                                                    plugin.getMessages().send(player, "gui.msg-glow-set-line",
                                                            "line", String.valueOf(lineIndex + 1),
                                                            "color", "#" + String.format("%06X", color & 0xFFFFFF));
                                                }
                                            })) {
                                                reopenGui(player);
                                            }
                                        } else {
                                            plugin.getMessages().send(player, "gui.msg-glow-color-invalid-format");
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
                .name(plugin.getMessages().getString("gui.line-edit.btn-chroma"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-chroma"),
                        plugin.getMessages().getString("gui.line-edit.lore-chroma-current",
                                "state", plugin.getMessages().getRaw(chromaEnabled ? "state-enabled" : "state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.line-edit.lore-chroma-left"),
                        plugin.getMessages().getString("gui.line-edit.lore-chroma-right")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    if (context.getClickType() == ClickType.RIGHT || context.getClickType() == ClickType.SHIFT_RIGHT) {
                        if (withHologramLine(player, (h, p, l) -> {
                            l.setChromaBackground(null);
                            l.setChromaGlow(null);
                            h.save();
                            h.refreshAllViewers();
                            plugin.getMessages().send(player, "gui.msg-chroma-reset-inherit");
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
                            plugin.getMessages().send(player, "gui.msg-chroma-toggle",
                                    "state", plugin.getMessages().getRaw(newState ? "state-enabled" : "state-disabled"));
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
                .name(plugin.getMessages().getString("gui.line-edit.btn-height"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.line-edit.lore-height"),
                        plugin.getMessages().getString("gui.line-edit.lore-height-current",
                                "height", String.valueOf(line.getHeight())),
                        "",
                        plugin.getMessages().getString("gui.lore-click-set")))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();

                    chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.line-height-value"),
                            ChatInputManager.InputType.LINE_HEIGHT, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    double height = Double.parseDouble(input);
                                    if (withHologramLine(player, (h, p, l) -> {
                                        l.setHeight(height);
                                        h.save();
                                        h.realignLines();
                                        plugin.getMessages().send(player, "gui.msg-line-height-set",
                                                "height", String.valueOf(height));
                                    })) {
                                        reopenGui(player);
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getMessages().send(player, "gui.msg-input-invalid-number");
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
