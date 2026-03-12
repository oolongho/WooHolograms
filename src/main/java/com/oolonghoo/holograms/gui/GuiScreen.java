package com.oolonghoo.holograms.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI 界面基类
 * 提供基本的 GUI 功能
 * 
 */
public class GuiScreen implements InventoryHolder {

    private final String id;
    private final String title;
    private final int size;
    private final Inventory inventory;
    private final Map<Integer, GuiButton> buttons;

    public GuiScreen(String id, String title, int size) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(this, size, title);
        this.buttons = new HashMap<>();
    }

    /**
     * 获取 GUI ID
     * @return GUI ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取标题
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取大小
     * @return 大小
     */
    public int getSize() {
        return size;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * 设置按钮
     * @param slot 槽位
     * @param button 按钮
     */
    public void setButton(int slot, GuiButton button) {
        if (slot >= 0 && slot < size) {
            buttons.put(slot, button);
            inventory.setItem(slot, button.getItemStack());
        }
    }

    /**
     * 获取按钮
     * @param slot 槽位
     * @return 按钮
     */
    public GuiButton getButton(int slot) {
        return buttons.get(slot);
    }

    /**
     * 移除按钮
     * @param slot 槽位
     */
    public void removeButton(int slot) {
        buttons.remove(slot);
        inventory.setItem(slot, null);
    }

    /**
     * 清空所有按钮
     */
    public void clearButtons() {
        buttons.clear();
        inventory.clear();
    }

    /**
     * 打开 GUI
     * @param player 玩家
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * 关闭 GUI
     * @param player 玩家
     */
    public void close(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }

    /**
     * 刷新 GUI
     * @param player 玩家
     */
    public void refresh(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.updateInventory();
        }
    }

    /**
     * 点击处理
     * @param player 玩家
     * @param slot 槽位
     * @param clickType 点击类型
     * @return 是否取消事件
     */
    public boolean handleClick(Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        GuiButton button = buttons.get(slot);
        if (button != null) {
            button.onClick(player, clickType);
            return true;
        }
        return false;
    }
}
