package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * GUI 监听器
 * 监听 GUI 相关事件
 * 
 * @author oolongho
 */
public class GuiListener implements Listener {

    private final WooHolograms plugin;
    private final GuiManager guiManager;

    public GuiListener(WooHolograms plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        // 检查是否是我们的 GUI
        if (!(holder instanceof GuiScreen)) {
            return;
        }

        // 取消默认行为
        event.setCancelled(true);

        GuiScreen screen = (GuiScreen) holder;

        // 检查是否点击的是 GUI 内的槽位
        if (event.getClickedInventory() == null || 
            !event.getClickedInventory().equals(screen.getInventory())) {
            return;
        }

        int slot = event.getSlot();
        
        // 处理点击
        screen.handleClick(player, slot, event.getClick());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        // 检查是否是我们的 GUI
        if (holder instanceof GuiScreen) {
            guiManager.cleanup(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        // 检查是否是我们的 GUI
        if (holder instanceof GuiScreen) {
            // 取消拖拽
            event.setCancelled(true);
        }
    }
}
