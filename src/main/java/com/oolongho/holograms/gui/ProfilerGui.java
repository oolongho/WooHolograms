package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.util.ColorUtil;
import com.oolongho.holograms.util.Profiler;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 性能分析器 GUI
 * 查看、启用/禁用、重置性能分析数据
 */
public class ProfilerGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;

    public ProfilerGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager) {
        super("profiler", ColorUtil.colorize("&8性能分析"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;

        render();
    }

    private void render() {
        clearButtons();

        Profiler profiler = Profiler.getInstance();
        boolean isEnabled = profiler.isEnabled();

        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回全息图列表", "", "&e点击返回"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        // 启用/禁用切换
        setButton(11, GuiButton.builder(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&f" + (isEnabled ? "禁用" : "启用"))
                .lore(Arrays.asList(
                        "&7当前状态: " + (isEnabled ? "&a启用" : "&c禁用"),
                        "",
                        "&e点击切换"
                ))
                .onClick(context -> {
                    Profiler p = Profiler.getInstance();
                    p.setEnabled(!p.isEnabled());
                    Player player = context.getPlayer();
                    player.sendMessage(ColorUtil.colorize("&a性能分析器已" + (p.isEnabled() ? "启用" : "禁用") + "！"));
                    guiManager.openGui(player, new ProfilerGui(plugin, guiManager, chatInputManager));
                })
                .build());

        // 查看报告
        setButton(13, GuiButton.builder(Material.PAPER)
                .name("&f查看报告")
                .lore(Arrays.asList(
                        "&7在聊天中显示性能报告",
                        "",
                        "&e点击查看"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    String report = Profiler.getInstance().getReport();
                    player.sendMessage(ColorUtil.colorize(report));
                })
                .build());

        // 重置数据
        setButton(15, GuiButton.builder(Material.BARRIER)
                .name("&f重置数据")
                .lore(Arrays.asList(
                        "&7重置所有性能数据",
                        "",
                        "&e点击重置"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.create("&c确认重置所有性能数据?", confirmed -> {
                        if (confirmed) {
                            Profiler.getInstance().reset();
                            player.sendMessage(ColorUtil.colorize("&a性能数据已重置！"));
                        }
                        guiManager.openGui(player, new ProfilerGui(plugin, guiManager, chatInputManager));
                    }));
                })
                .build());

        fillBackground();
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 27; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
}
