package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.HeadTexture;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.hologram.HologramType;
import com.oolonghoo.holograms.hologram.TextAlignment;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 行编辑 GUI
 * 用于编辑单行的属性
 * 
 * @author oolongho
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
        super("line_edit", ColorUtil.colorize("&8编辑行: " + hologramName + " #" + (lineIndex + 1)), 36);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.lineIndex = lineIndex;
        
        render();
    }

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
        
        // 返回按钮
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
        
        // 当前内容显示
        setButton(4, GuiButton.builder(Material.PAPER)
                .name("&f当前内容")
                .lore(Arrays.asList(
                        "",
                        line.getContent(),
                        ""
                ))
                .build());
        
        // 第一行按钮
        // 设置文本
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
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            HologramPage p = h.getPage(pageIndex);
                            if (p != null && lineIndex < p.size()) {
                                p.setLine(lineIndex, input);
                                h.save();
                                h.showToNearby();
                                player.sendMessage(ColorUtil.colorize("&a已更新行文本！"));
                            }
                        }
                        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                    });
                })
                .build());
        
        // 设置偏移
        setButton(10, GuiButton.builder(Material.STICK)
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
                                
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    HologramPage p = h.getPage(pageIndex);
                                    if (p != null && lineIndex < p.size()) {
                                        HologramLine l = p.getLine(lineIndex);
                                        if (l != null) {
                                            l.setOffsetX(x);
                                            l.setOffsetY(y);
                                            l.setOffsetZ(z);
                                            h.save();
                                            h.realignLines();
                                            player.sendMessage(ColorUtil.colorize("&a已设置偏移为 (" + x + ", " + y + ", " + z + ")！"));
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(ColorUtil.colorize("&c请输入三个数字，用空格分隔！"));
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                        }
                        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                    });
                })
                .build());
        
        // 设置高度
        setButton(11, GuiButton.builder(Material.RAIL)
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
                            
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                HologramPage p = h.getPage(pageIndex);
                                if (p != null && lineIndex < p.size()) {
                                    HologramLine l = p.getLine(lineIndex);
                                    if (l != null) {
                                        l.setHeight(height);
                                        h.save();
                                        h.realignLines();
                                        player.sendMessage(ColorUtil.colorize("&a已设置高度为 " + height + "！"));
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                        }
                        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                    });
                })
                .build());
        
        // 第二行按钮
        // 亮度设置
        Brightness brightness = line.getBrightness();
        String brightnessDisplay = brightness != null && !brightness.isDefault() 
                ? brightness.getSkyLight() + "/" + brightness.getBlockLight() 
                : "默认";
        setButton(18, GuiButton.builder(Material.GLOWSTONE)
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
        
        // 对齐设置
        TextAlignment alignment = line.getAlignment();
        setButton(19, GuiButton.builder(Material.REPEATER)
                .name("&f对齐设置")
                .lore(Arrays.asList(
                        "&7设置文本的对齐方式",
                        "&7当前: &f" + alignment.getDisplayName(),
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new AlignmentSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 朝向设置
        Billboard billboard = line.getBillboard();
        String facingDisplay = billboard.getDisplayName();
        if (billboard == Billboard.FIXED_ANGLE) {
            facingDisplay += " (" + line.getFacing() + "度)";
        }
        setButton(20, GuiButton.builder(Material.COMPASS)
                .name("&f朝向设置")
                .lore(Arrays.asList(
                        "&7设置显示的朝向模式",
                        "&7当前: &f" + facingDisplay,
                        "",
                        "&7模式说明:",
                        "&7- 固定角度: 使用固定朝向",
                        "&7- 水平跟随: 水平方向跟随玩家",
                        "&7- 垂直跟随: 垂直方向跟随玩家",
                        "&7- 完全跟随: 完全跟随玩家视角",
                        "",
                        "&e点击设置"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 头颅材质设置 (仅当行类型为HEAD或SMALLHEAD时显示)
        if (lineType == HologramType.HEAD || lineType == HologramType.SMALLHEAD) {
            HeadTexture headTexture = line.getHeadTexture();
            String textureDisplay = "未设置";
            if (headTexture != null) {
                switch (headTexture.getType()) {
                    case BASE64:
                        textureDisplay = "URL材质";
                        break;
                    case PLAYER:
                        textureDisplay = "玩家: " + headTexture.getValue();
                        break;
                    case HDB:
                        textureDisplay = "HDB: " + headTexture.getValue();
                        break;
                }
            }
            
            setButton(21, GuiButton.builder(Material.PLAYER_HEAD)
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
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                HologramPage p = h.getPage(pageIndex);
                                if (p != null && lineIndex < p.size()) {
                                    HologramLine l = p.getLine(lineIndex);
                                    if (l != null) {
                                        // 构建新的内容
                                        String prefix = lineType == HologramType.HEAD ? "#HEAD:" : "#SMALLHEAD:";
                                        String newContent = prefix + input.toUpperCase();
                                        l.setContent(newContent);
                                        h.save();
                                        h.showToNearby();
                                        player.sendMessage(ColorUtil.colorize("&a已设置头颅材质！"));
                                    }
                                }
                            }
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                        });
                    })
                    .build());
        }
        
        // 删除行按钮
        setButton(31, GuiButton.builder(Material.BARRIER)
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
                            Hologram h = plugin.getHologramManager().getHologram(hologramName);
                            if (h != null) {
                                HologramPage p = h.getPage(pageIndex);
                                if (p != null && lineIndex < p.size()) {
                                    p.removeLine(lineIndex);
                                    h.save();
                                    h.showToNearby();
                                    player.sendMessage(ColorUtil.colorize("&a已删除第 " + (lineIndex + 1) + " 行！"));
                                }
                            }
                            guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                        } else {
                            guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                        }
                    }));
                })
                .build());
        
        // 上移按钮
        if (lineIndex > 0) {
            setButton(27, GuiButton.builder(Material.ARROW)
                    .name("&f上移")
                    .lore(Arrays.asList(
                            "&7将此行向上移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            HologramPage p = h.getPage(pageIndex);
                            if (p != null && lineIndex > 0) {
                                HologramLine current = p.getLine(lineIndex);
                                HologramLine above = p.getLine(lineIndex - 1);
                                
                                if (current != null && above != null) {
                                    String tempContent = current.getContent();
                                    current.setContent(above.getContent());
                                    above.setContent(tempContent);
                                    
                                    h.save();
                                    h.showToNearby();
                                    player.sendMessage(ColorUtil.colorize("&a已上移！"));
                                }
                            }
                        }
                        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex - 1));
                    })
                    .build());
        }
        
        // 下移按钮
        if (page != null && lineIndex < page.size() - 1) {
            setButton(35, GuiButton.builder(Material.ARROW)
                    .name("&f下移")
                    .lore(Arrays.asList(
                            "&7将此行向下移动",
                            "",
                            "&e点击移动"
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        Hologram h = plugin.getHologramManager().getHologram(hologramName);
                        if (h != null) {
                            HologramPage p = h.getPage(pageIndex);
                            if (p != null && lineIndex < p.size() - 1) {
                                HologramLine current = p.getLine(lineIndex);
                                HologramLine below = p.getLine(lineIndex + 1);
                                
                                if (current != null && below != null) {
                                    String tempContent = current.getContent();
                                    current.setContent(below.getContent());
                                    below.setContent(tempContent);
                                    
                                    h.save();
                                    h.showToNearby();
                                    player.sendMessage(ColorUtil.colorize("&a已下移！"));
                                }
                            }
                        }
                        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex + 1));
                    })
                    .build());
        }
        
        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 22, 23, 24, 25, 26, 28, 29, 30, 32, 33, 34};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
