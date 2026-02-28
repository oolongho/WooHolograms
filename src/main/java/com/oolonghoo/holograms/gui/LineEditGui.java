package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

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
        super("line_edit", ColorUtil.colorize("&8编辑行: " + hologramName + " #" + (lineIndex + 1)), 27);
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
                    .name("&c全息图不存在")
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
                    .name("&c行不存在")
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
        
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&e返回")
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
                        "&f" + ColorUtil.stripColor(line.getContent()),
                        ""
                ))
                .build());
        
        setButton(10, GuiButton.builder(Material.OAK_SIGN)
                .name("&a设置文本")
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
        
        setButton(11, GuiButton.builder(Material.STICK)
                .name("&b设置偏移")
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
        
        setButton(12, GuiButton.builder(Material.RAIL)
                .name("&e设置高度")
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
        
        setButton(16, GuiButton.builder(Material.BARRIER)
                .name("&c删除行")
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
        
        if (lineIndex > 0) {
            setButton(18, GuiButton.builder(Material.ARROW)
                    .name("&e上移")
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
        
        if (page != null && lineIndex < page.size() - 1) {
            setButton(22, GuiButton.builder(Material.ARROW)
                    .name("&e下移")
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
        
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 9, 13, 14, 15, 17, 19, 20, 21, 23, 24, 25, 26};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
