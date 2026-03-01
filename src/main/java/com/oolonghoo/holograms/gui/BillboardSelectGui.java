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

        // 返回按钮
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

        // 当前设置显示
        setButton(4, GuiButton.builder(Material.COMPASS)
                .name("&f当前朝向模式")
                .lore(Arrays.asList(
                        "",
                        "&7" + currentBillboard.getDisplayName(),
                        ""
                ))
                .build());

        // 固定选项
        setButton(10, GuiButton.builder(Material.STONE)
                .name("&f固定")
                .lore(Arrays.asList(
                        "&7固定朝向，不随视角变化",
                        "",
                        currentBillboard == Billboard.FIXED ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.FIXED);
                })
                .build());

        // 垂直选项
        setButton(12, GuiButton.builder(Material.END_ROD)
                .name("&f垂直")
                .lore(Arrays.asList(
                        "&7垂直方向跟随玩家视角",
                        "",
                        currentBillboard == Billboard.VERTICAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.VERTICAL);
                })
                .build());

        // 水平选项
        setButton(14, GuiButton.builder(Material.RAIL)
                .name("&f水平")
                .lore(Arrays.asList(
                        "&7水平方向跟随玩家视角",
                        "",
                        currentBillboard == Billboard.HORIZONTAL ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.HORIZONTAL);
                })
                .build());

        // 居中选项
        setButton(16, GuiButton.builder(Material.END_CRYSTAL)
                .name("&f中心")
                .lore(Arrays.asList(
                        "&7完全跟随玩家视角",
                        "",
                        currentBillboard == Billboard.CENTER ? "&a当前选择" : "&e点击选择"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    setBillboard(player, Billboard.CENTER);
                })
                .build());

        fillBackground();
    }

    private void setBillboard(Player player, Billboard billboard) {
        Hologram h = plugin.getHologramManager().getHologram(hologramName);
        if (h != null) {
            HologramPage p = h.getPage(pageIndex);
            if (p != null && lineIndex < p.size()) {
                HologramLine l = p.getLine(lineIndex);
                if (l != null) {
                    l.setBillboard(billboard);
                    h.save();
                    h.showToNearby();
                    player.sendMessage(ColorUtil.colorize("&a已设置朝向模式为 " + billboard.getDisplayName() + "！"));
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
