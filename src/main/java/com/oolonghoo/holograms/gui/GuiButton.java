package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI 按钮类
 * 表示 GUI 中的一个可点击按钮
 * 
 * @author oolongho
 */
public class GuiButton {

    private final ItemStack itemStack;
    private Consumer<ClickContext> clickHandler;

    public GuiButton(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 创建按钮构建器
     * @param material 材质
     * @return 构建器
     */
    public static Builder builder(Material material) {
        return new Builder(material);
    }

    /**
     * 获取物品堆
     * @return 物品堆
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * 设置点击处理器
     * @param handler 处理器
     */
    public void setClickHandler(Consumer<ClickContext> handler) {
        this.clickHandler = handler;
    }

    /**
     * 处理点击
     * @param player 玩家
     * @param clickType 点击类型
     */
    public void onClick(Player player, ClickType clickType) {
        if (clickHandler != null) {
            clickHandler.accept(new ClickContext(player, clickType));
        }
    }

    /**
     * 点击上下文
     */
    public static class ClickContext {
        private final Player player;
        private final ClickType clickType;

        public ClickContext(Player player, ClickType clickType) {
            this.player = player;
            this.clickType = clickType;
        }

        public Player getPlayer() {
            return player;
        }

        public ClickType getClickType() {
            return clickType;
        }
    }

    /**
     * 按钮构建器
     */
    public static class Builder {
        private final ItemStack itemStack;
        private final ItemMeta meta;
        private String name;
        private List<String> lore = new ArrayList<>();
        private Consumer<ClickContext> clickHandler;

        public Builder(Material material) {
            this.itemStack = new ItemStack(material);
            this.meta = itemStack.getItemMeta();
        }

        /**
         * 设置名称
         * @param name 名称
         * @return 构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 添加描述行
         * @param line 描述行
         * @return 构建器
         */
        public Builder lore(String line) {
            this.lore.add(line);
            return this;
        }

        /**
         * 设置描述
         * @param lore 描述列表
         * @return 构建器
         */
        public Builder lore(List<String> lore) {
            this.lore = new ArrayList<>(lore);
            return this;
        }

        /**
         * 设置点击处理器
         * @param handler 处理器
         * @return 构建器
         */
        public Builder onClick(Consumer<ClickContext> handler) {
            this.clickHandler = handler;
            return this;
        }

        /**
         * 构建按钮
         * @return 按钮
         */
        public GuiButton build() {
            if (name != null) {
                meta.setDisplayName(ColorUtil.colorize(name));
            }
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ColorUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            itemStack.setItemMeta(meta);
            
            GuiButton button = new GuiButton(itemStack);
            if (clickHandler != null) {
                button.setClickHandler(clickHandler);
            }
            return button;
        }
    }
}
