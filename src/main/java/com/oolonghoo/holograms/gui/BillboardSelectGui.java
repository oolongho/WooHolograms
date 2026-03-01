package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 朝向设置 GUI
 * 用于设置全息图行的 Billboard 模式
 *
 * @author oolongho
 */
public class BillboardSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final int lineIndex;

    public BillboardSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                              String hologramName, int pageIndex, int lineIndex) {
        super("billboard_select", ColorUtil.colorize("&8朝向设置"), 27);
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
        Billboard currentBillboard = line.getBillboard();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回行编辑",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());

        String currentDisplay = currentBillboard.getDisplayName();
        if (currentBillboard == Billboard.FIXED_ANGLE) {
            currentDisplay += " (" + line.getFacing() + "度)";
        }
        setButton(4, GuiButton.builder(Material.COMPASS)
                .name("&f当前朝向模式")
                .lore(Arrays.asList(
                        "",
                        "&7" + currentDisplay,
                        ""
                ))
                .build());

        setButton(10, GuiButton.builder(Material.STONE_BUTTON)
                .name("&f固定角度")
                .lore(Arrays.asList(
                        "&7使用固定角度朝向",
                        "&7需要设置具体角度值",
                        "",
                        currentBillboard == Billboard.FIXED_ANGLE ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入固定角度 (0-360度):", 
                            ChatInputManager.InputType.GENERIC, hologramName, lineIndex, pageIndex, input -> {
                        try {
                            float angle = Float.parseFloat(input);
                            setBillboard(player, Billboard.FIXED_ANGLE, angle);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&c请输入有效的数字！"));
                            guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                        }
                    });
                })
                .build());

        setButton(12, GuiButton.builder(Material.END_ROD)
                .name("&f垂直跟随")
                .lore(Arrays.asList(
                        "&7垂直方向跟随玩家视角",
                        "&7水平方向固定",
                        "",
                        currentBillboard == Billboard.VERTICAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.VERTICAL, 0);
                })
                .build());

        setButton(14, GuiButton.builder(Material.RAIL)
                .name("&f水平跟随")
                .lore(Arrays.asList(
                        "&7水平方向跟随玩家视角",
                        "&7垂直方向固定",
                        "",
                        currentBillboard == Billboard.HORIZONTAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.HORIZONTAL, 0);
                })
                .build());

        setButton(16, GuiButton.builder(Material.END_CRYSTAL)
                .name("&f完全跟随")
                .lore(Arrays.asList(
                        "&7完全跟随玩家视角",
                        "&7默认模式",
                        "",
                        currentBillboard == Billboard.CENTER ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.CENTER, 0);
                })
                .build());

        fillBackground();
    }

    private void setBillboard(Player player, Billboard billboard, float facing) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h != null) {
            HologramPage p = h.getPage(pageIndex);
            if (p != null && lineIndex < p.size()) {
                HologramLine l = p.getLine(lineIndex);
                if (l != null) {
                    l.setBillboard(billboard);
                    if (billboard == Billboard.FIXED_ANGLE) {
                        l.setFacing(facing);
                    }
                    h.save();
                    h.showToNearby();
                    
                    String modeDisplay = billboard.getDisplayName();
                    if (billboard == Billboard.FIXED_ANGLE) {
                        modeDisplay += " (" + facing + "度)";
                    }
                    player.sendMessage(ColorUtil.colorize("&a已设置朝向模式为 " + modeDisplay + "！"));
                }
            }
        }
        guiManager.openGui(player, new BillboardSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
