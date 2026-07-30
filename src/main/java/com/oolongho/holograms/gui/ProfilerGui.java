package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
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
        super("profiler", plugin.getMessages().get("gui.title-profiler"), 27);
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
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(plugin.getMessages().getString("gui.lore-back-list"), "", plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        // 启用/禁用切换
        setButton(11, GuiButton.builder(isEnabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(plugin.getMessages().getString(isEnabled ? "gui.profiler.disable" : "gui.profiler.enable"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.profiler.current-state",
                                "state", plugin.getMessages().getString(isEnabled ? "gui.profiler.state-enabled" : "gui.profiler.state-disabled")),
                        "",
                        plugin.getMessages().getString("gui.lore-click-toggle")
                ))
                .onClick(context -> {
                    Profiler p = Profiler.getInstance();
                    p.setEnabled(!p.isEnabled());
                    Player player = context.getPlayer();
                    plugin.getMessages().send(player, p.isEnabled() ? "gui.msg-profiler-enabled" : "gui.msg-profiler-disabled");
                    guiManager.openGui(player, new ProfilerGui(plugin, guiManager, chatInputManager));
                })
                .build());

        // 查看报告
        setButton(13, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.profiler.view-report"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.profiler.view-report-desc"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-view")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    String report = Profiler.getInstance().getReport(plugin.getMessages());
                    player.sendMessage(plugin.getMessages().parse(report));
                })
                .build());

        // 重置数据
        setButton(15, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.profiler.reset-data"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.profiler.reset-data-desc"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-reset")
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    guiManager.openGui(player, ConfirmGui.create(plugin, plugin.getMessages().getString("gui.profiler.confirm-reset"), confirmed -> {
                        if (confirmed) {
                            Profiler.getInstance().reset();
                            plugin.getMessages().send(player, "profiler.reset");
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
