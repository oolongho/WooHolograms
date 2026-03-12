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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI 管理器
 * 管理所有 GUI 实例和事件监听
 * 
 */
public class GuiManager implements Listener {

    private final WooHolograms plugin;
    private final Map<UUID, GuiScreen> openGuis;
    private final Map<UUID, GuiScreen> previousGuis;

    public GuiManager(WooHolograms plugin) {
        this.plugin = plugin;
        this.openGuis = new HashMap<>();
        this.previousGuis = new HashMap<>();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 打开 GUI
     * @param player 玩家
     * @param gui GUI 界面
     */
    public void openGui(Player player, GuiScreen gui) {
        UUID playerId = player.getUniqueId();
        
        GuiScreen currentGui = openGuis.get(playerId);
        if (currentGui != null) {
            previousGuis.put(playerId, currentGui);
        }
        
        openGuis.put(playerId, gui);
        gui.open(player);
    }

    /**
     * 关闭 GUI
     * @param player 玩家
     */
    public void closeGui(Player player) {
        UUID playerId = player.getUniqueId();
        GuiScreen gui = openGuis.remove(playerId);
        if (gui != null) {
            gui.close(player);
        }
    }

    /**
     * 返回上一个 GUI
     * @param player 玩家
     */
    public void goBack(Player player) {
        UUID playerId = player.getUniqueId();
        GuiScreen previousGui = previousGuis.remove(playerId);
        if (previousGui != null) {
            openGuis.put(playerId, previousGui);
            previousGui.open(player);
        }
    }

    /**
     * 获取当前打开的 GUI
     * @param player 玩家
     * @return GUI 界面
     */
    public GuiScreen getCurrentGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    /**
     * 刷新 GUI
     * @param player 玩家
     */
    public void refreshGui(Player player) {
        GuiScreen gui = openGuis.get(player.getUniqueId());
        if (gui != null) {
            gui.refresh(player);
        }
    }

    /**
     * 处理库存点击事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof GuiScreen) {
            event.setCancelled(true);
            
            GuiScreen gui = (GuiScreen) holder;
            int slot = event.getRawSlot();
            
            if (slot >= 0 && slot < gui.getSize()) {
                gui.handleClick(player, slot, event.getClick());
            }
        }
    }

    /**
     * 处理库存关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof GuiScreen) {
            GuiScreen gui = (GuiScreen) holder;
            
            if (openGuis.get(player.getUniqueId()) == gui) {
                openGuis.remove(player.getUniqueId());
            }
        }
    }

    /**
     * 处理库存拖拽事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof GuiScreen) {
            event.setCancelled(true);
        }
    }

    /**
     * 检查玩家是否有打开的 GUI
     * @param player 玩家
     * @return 是否有打开的 GUI
     */
    public boolean hasOpenGui(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }

    /**
     * 清理玩家的 GUI 数据
     * @param player 玩家
     */
    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        openGuis.remove(playerId);
        previousGuis.remove(playerId);
    }

    /**
     * 清理玩家的 GUI 数据（别名方法）
     * @param player 玩家
     */
    public void cleanup(Player player) {
        clearPlayerData(player);
    }

    /**
     * 清理所有 GUI 数据
     */
    public void clear() {
        openGuis.clear();
        previousGuis.clear();
    }
}
