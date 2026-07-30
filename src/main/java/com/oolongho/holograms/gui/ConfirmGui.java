package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 确认对话框 GUI
 * 用于确认删除等危险操作
 *
 * 标题由调用方通过 {@code plugin.getMessages().get("gui.title-confirm-xxx")} 传入 Component
 * 警告消息作为 lore 显示，使用 MiniMessage 字符串（不含 {prefix}）
 */
public class ConfirmGui extends GuiScreen {

    private final WooHolograms plugin;

    public ConfirmGui(WooHolograms plugin, Component title, String warningMessage, Consumer<Boolean> callback) {
        super("confirm", title, 27);
        this.plugin = plugin;

        fillBackground();

        setButton(11, GuiButton.builder(Material.RED_WOOL)
                .name(plugin.getMessages().getString("gui.btn-confirm-delete"))
                .lore(Arrays.asList(
                        warningMessage,
                        "",
                        plugin.getMessages().getString("gui.lore-click-confirm")
                ))
                .onClick(context -> callback.accept(true))
                .build());

        setButton(13, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.btn-warning"))
                .lore(Arrays.asList(
                        "",
                        warningMessage,
                        ""
                ))
                .build());

        setButton(15, GuiButton.builder(Material.GREEN_WOOL)
                .name(plugin.getMessages().getString("gui.btn-cancel"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.lore-cancel-action"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-cancel")
                ))
                .onClick(context -> callback.accept(false))
                .build());
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 13 && i != 15) {
                setButton(i, background);
            }
        }
    }

    /**
     * 创建确认删除全息图的对话框
     */
    public static ConfirmGui createDeleteConfirm(WooHolograms plugin, String hologramName, Consumer<Boolean> callback) {
        return new ConfirmGui(
                plugin,
                plugin.getMessages().get("gui.title-confirm-delete"),
                plugin.getMessages().getString("gui.confirm.delete-hologram-warning", "name", hologramName),
                callback
        );
    }

    /**
     * 创建确认删除页面的对话框
     */
    public static ConfirmGui createDeletePageConfirm(WooHolograms plugin, String hologramName, int pageIndex, Consumer<Boolean> callback) {
        return new ConfirmGui(
                plugin,
                plugin.getMessages().get("gui.title-confirm-delete-page"),
                plugin.getMessages().getString("gui.confirm.delete-page-warning", "name", hologramName, "page", String.valueOf(pageIndex + 1)),
                callback
        );
    }

    /**
     * 创建确认删除行的对话框
     */
    public static ConfirmGui createDeleteLineConfirm(WooHolograms plugin, String hologramName, int lineNumber, Consumer<Boolean> callback) {
        return new ConfirmGui(
                plugin,
                plugin.getMessages().get("gui.title-confirm-delete-line"),
                plugin.getMessages().getString("gui.confirm.delete-line-warning", "name", hologramName, "line", String.valueOf(lineNumber)),
                callback
        );
    }

    /**
     * 创建确认删除动作的对话框
     */
    public static ConfirmGui createDeleteActionConfirm(WooHolograms plugin, int actionIndex, Consumer<Boolean> callback) {
        return new ConfirmGui(
                plugin,
                plugin.getMessages().get("gui.title-confirm-delete-action"),
                plugin.getMessages().getString("gui.confirm.delete-action-warning", "index", String.valueOf(actionIndex + 1)),
                callback
        );
    }

    /**
     * 创建通用确认对话框（使用默认标题）
     */
    public static ConfirmGui create(WooHolograms plugin, String warningMessage, Consumer<Boolean> callback) {
        return new ConfirmGui(
                plugin,
                plugin.getMessages().get("gui.title-confirm"),
                warningMessage,
                callback
        );
    }
}
