package com.oolongho.holograms.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiButton {

    /** MiniMessage 实例，用于解析按钮名称和 lore 中的 MiniMessage 标签 */
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final ItemStack itemStack;
    private Consumer<ClickContext> clickHandler;

    public GuiButton(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static Builder builder(Material material) {
        return new Builder(material);
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public void setClickHandler(Consumer<ClickContext> handler) {
        this.clickHandler = handler;
    }

    public void onClick(Player player, ClickType clickType) {
        if (clickHandler != null) {
            clickHandler.accept(new ClickContext(player, clickType));
        }
    }

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

    public static class Builder {
        private final ItemStack itemStack;
        private final ItemMeta meta;
        private String name;
        private List<String> lore = new ArrayList<>();
        private Consumer<ClickContext> clickHandler;
        private boolean glowing = false;

        public Builder(Material material) {
            this.itemStack = new ItemStack(material);
            this.meta = itemStack.getItemMeta();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lore(String line) {
            this.lore.add(line);
            return this;
        }

        public Builder lore(List<String> lore) {
            this.lore = new ArrayList<>(lore);
            return this;
        }

        public Builder onClick(Consumer<ClickContext> handler) {
            this.clickHandler = handler;
            return this;
        }
        
        public Builder glow() {
            this.glowing = true;
            return this;
        }
        
        public Builder flag(ItemFlag... flags) {
            if (meta != null) {
                meta.addItemFlags(flags);
            }
            return this;
        }

        public GuiButton build() {
            if (name != null) {
                meta.displayName(MINI.deserialize(name));
            }
            if (!lore.isEmpty()) {
                List<Component> loreComponents = lore.stream()
                        .map(MINI::deserialize)
                        .collect(Collectors.toList());
                meta.lore(loreComponents);
            }
            
            if (glowing) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
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
