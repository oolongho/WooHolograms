package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 确认对话框 GUI
 * 用于确认删除等危险操作
 * 
 * @author oolongho
 */
public class ConfirmGui extends GuiScreen {

    private final String warningMessage;
    private final Consumer<Boolean> callback;

    public ConfirmGui(String title, String warningMessage, Consumer<Boolean> callback) {
        super("confirm", ColorUtil.colorize(title), 27);
        this.warningMessage = warningMessage;
        this.callback = callback;
        
        fillBackground();
        
        setButton(11, GuiButton.builder(Material.RED_WOOL)
                .name("&f确认删除")
                .lore(Arrays.asList(
                        "&7" + warningMessage,
                        "",
                        "&e点击确认"
                ))
                .onClick(context -> {
                    callback.accept(true);
                })
                .build());
        
        setButton(13, GuiButton.builder(Material.PAPER)
                .name("&f警告")
                .lore(Arrays.asList(
                        "",
                        "&7" + warningMessage,
                        ""
                ))
                .build());
        
        setButton(15, GuiButton.builder(Material.GREEN_WOOL)
                .name("&f取消")
                .lore(Arrays.asList(
                        "&7取消此操作",
                        "",
                        "&e点击取消"
                ))
                .onClick(context -> {
                    callback.accept(false);
                })
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
    public static ConfirmGui createDeleteConfirm(String hologramName, Consumer<Boolean> callback) {
        return new ConfirmGui(
                "&8确认删除",
                "&c确定要删除全息图 &e" + hologramName + " &c吗？",
                callback
        );
    }

    /**
     * 创建确认删除页面的对话框
     */
    public static ConfirmGui createDeletePageConfirm(String hologramName, int pageIndex, Consumer<Boolean> callback) {
        return new ConfirmGui(
                "&8确认删除页面",
                "&c确定要删除全息图 &e" + hologramName + " &c的第 &e" + (pageIndex + 1) + " &c页吗？",
                callback
        );
    }

    /**
     * 创建确认删除行的对话框
     */
    public static ConfirmGui createDeleteLineConfirm(String hologramName, int lineNumber, Consumer<Boolean> callback) {
        return new ConfirmGui(
                "&8确认删除行",
                "&c确定要删除全息图 &e" + hologramName + " &c的第 &e" + lineNumber + " &c行吗？",
                callback
        );
    }

    /**
     * 创建确认删除动作的对话框
     */
    public static ConfirmGui createDeleteActionConfirm(int actionIndex, Consumer<Boolean> callback) {
        return new ConfirmGui(
                "&8确认删除动作",
                "&c确定要删除第 &e" + (actionIndex + 1) + " &c个动作吗？",
                callback
        );
    }
    
    /**
     * 创建通用确认对话框
     */
    public static ConfirmGui create(String message, Consumer<Boolean> callback) {
        return new ConfirmGui(
                "&8确认操作",
                message,
                callback
        );
    }
}
