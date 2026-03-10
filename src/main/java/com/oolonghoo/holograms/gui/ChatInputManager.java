package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 聊天框输入管理器
 * 用于处理玩家在聊天框中的输入
 * 
 * @author oolongho
 */
public class ChatInputManager implements Listener {

    private final WooHolograms plugin;
    private final Map<UUID, InputContext> pendingInputs;
    private final Map<UUID, BukkitTask> timeoutTasks;
    private static final long INPUT_TIMEOUT = 30 * 20; // 30秒超时

    public ChatInputManager(WooHolograms plugin) {
        this.plugin = plugin;
        this.pendingInputs = new ConcurrentHashMap<>();
        this.timeoutTasks = new ConcurrentHashMap<>();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示语
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, String prompt, Consumer<String> callback) {
        requestInput(player, prompt, InputType.GENERIC, callback);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示语
     * @param type 输入类型
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, String prompt, InputType type, Consumer<String> callback) {
        UUID playerId = player.getUniqueId();
        
        cancelTimeoutTask(playerId);
        
        InputContext context = new InputContext(type, callback);
        pendingInputs.put(playerId, context);
        
        player.sendMessage(ColorUtil.colorize(prompt));
        player.sendMessage(ColorUtil.colorize("&7输入 &ecancel &7或 &e取消 &7来取消输入"));
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingInputs.containsKey(playerId) && pendingInputs.get(playerId) == context) {
                    pendingInputs.remove(playerId);
                    timeoutTasks.remove(playerId);
                    player.sendMessage(ColorUtil.colorize("&c输入已超时取消！"));
                }
            }
        }.runTaskLater(plugin, INPUT_TIMEOUT);
        timeoutTasks.put(playerId, task);
    }

    /**
     * 请求玩家输入（带上下文）
     * @param player 玩家
     * @param prompt 提示语
     * @param type 输入类型
     * @param hologramName 全息图名称
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, String prompt, InputType type, String hologramName, Consumer<String> callback) {
        UUID playerId = player.getUniqueId();
        
        cancelTimeoutTask(playerId);
        
        InputContext context = new InputContext(type, hologramName, callback);
        pendingInputs.put(playerId, context);
        
        player.sendMessage(ColorUtil.colorize(prompt));
        player.sendMessage(ColorUtil.colorize("&7输入 &ecancel &7或 &e取消 &7来取消输入"));
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingInputs.containsKey(playerId) && pendingInputs.get(playerId) == context) {
                    pendingInputs.remove(playerId);
                    timeoutTasks.remove(playerId);
                    player.sendMessage(ColorUtil.colorize("&c输入已超时取消！"));
                }
            }
        }.runTaskLater(plugin, INPUT_TIMEOUT);
        timeoutTasks.put(playerId, task);
    }

    /**
     * 请求玩家输入（带完整上下文）
     * @param player 玩家
     * @param prompt 提示语
     * @param type 输入类型
     * @param hologramName 全息图名称
     * @param lineNumber 行号
     * @param pageIndex 页码
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, String prompt, InputType type, String hologramName, int lineNumber, int pageIndex, Consumer<String> callback) {
        UUID playerId = player.getUniqueId();
        
        cancelTimeoutTask(playerId);
        
        InputContext context = new InputContext(type, hologramName, lineNumber, pageIndex, callback);
        pendingInputs.put(playerId, context);
        
        player.sendMessage(ColorUtil.colorize(prompt));
        player.sendMessage(ColorUtil.colorize("&7输入 &ecancel &7或 &e取消 &7来取消输入"));
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingInputs.containsKey(playerId) && pendingInputs.get(playerId) == context) {
                    pendingInputs.remove(playerId);
                    timeoutTasks.remove(playerId);
                    player.sendMessage(ColorUtil.colorize("&c输入已超时取消！"));
                }
            }
        }.runTaskLater(plugin, INPUT_TIMEOUT);
        timeoutTasks.put(playerId, task);
    }
    
    /**
     * 取消超时任务
     * @param playerId 玩家ID
     */
    private void cancelTimeoutTask(UUID playerId) {
        BukkitTask task = timeoutTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 处理聊天事件
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        InputContext context = pendingInputs.remove(playerId);
        if (context != null) {
            event.setCancelled(true);
            cancelTimeoutTask(playerId);
            
            String input = event.getMessage();
            
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("取消")) {
                player.sendMessage(ColorUtil.colorize("&c输入已取消！"));
                return;
            }
            
            // 输入验证
            String validationError = validateInput(input, context.getType());
            if (validationError != null) {
                player.sendMessage(ColorUtil.colorize(validationError));
                pendingInputs.put(playerId, context);
                return;
            }
            
            final String finalInput = input;
            new BukkitRunnable() {
                @Override
                public void run() {
                    context.callback.accept(finalInput);
                }
            }.runTask(plugin);
        }
    }
    
    /**
     * 验证输入
     * @param input 输入内容
     * @param type 输入类型
     * @return 错误消息，null表示验证通过
     */
    private String validateInput(String input, InputType type) {
        int maxLength = plugin.getConfigManager().getMaxInputLength();
        
        if (input == null || input.isEmpty()) {
            return "&c输入不能为空！";
        }
        
        if (input.length() > maxLength) {
            return "&c输入长度超过限制（最大 " + maxLength + " 字符）！";
        }
        
        // 根据类型进行特定验证
        switch (type) {
            case HOLOGRAM_NAME:
                if (!input.matches("^[a-zA-Z0-9_\\-]+$")) {
                    return "&c名称只能包含字母、数字、下划线和连字符！";
                }
                break;
            case DISPLAY_RANGE:
            case UPDATE_INTERVAL:
            case LINE_HEIGHT:
                try {
                    int value = Integer.parseInt(input);
                    if (value <= 0) {
                        return "&c数值必须大于0！";
                    }
                } catch (NumberFormatException e) {
                    return "&c请输入有效的数字！";
                }
                break;
            case LINE_OFFSET:
                if (!input.matches("^[\\d.\\- ]+$")) {
                    return "&c偏移值只能包含数字、小数点和负号！";
                }
                break;
            case COORDINATES:
                if (!input.matches("^[\\d.\\- ]+$")) {
                    return "&c坐标只能包含数字、小数点和负号！";
                }
                break;
            default:
                break;
        }
        
        return null;
    }

    /**
     * 处理玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        pendingInputs.remove(playerId);
        cancelTimeoutTask(playerId);
    }

    /**
     * 检查玩家是否在等待输入
     * @param player 玩家
     * @return 是否在等待输入
     */
    public boolean isWaitingInput(Player player) {
        return pendingInputs.containsKey(player.getUniqueId());
    }

    /**
     * 取消玩家的输入等待
     * @param player 玩家
     */
    public void cancelInput(Player player) {
        UUID playerId = player.getUniqueId();
        pendingInputs.remove(playerId);
        cancelTimeoutTask(playerId);
    }

    /**
     * 输入类型枚举
     */
    public enum InputType {
        GENERIC,            // 通用输入
        HOLOGRAM_NAME,      // 全息图名称
        LINE_TEXT,          // 行文本
        LINE_OFFSET,        // 行偏移
        LINE_HEIGHT,        // 行高度
        LINE_FACING,        // 行朝向
        DISPLAY_RANGE,      // 显示范围
        UPDATE_INTERVAL,    // 更新间隔
        PERMISSION,         // 权限
        ACTION_VALUE,       // 动作值
        TARGET_NAME,        // 目标名称（克隆用）
        COORDINATES         // 坐标
    }

    /**
     * 输入上下文
     */
    public static class InputContext {
        private final InputType type;
        private final String hologramName;
        private final int lineNumber;
        private final int pageIndex;
        private final Consumer<String> callback;

        public InputContext(InputType type, Consumer<String> callback) {
            this.type = type;
            this.hologramName = null;
            this.lineNumber = -1;
            this.pageIndex = -1;
            this.callback = callback;
        }

        public InputContext(InputType type, String hologramName, Consumer<String> callback) {
            this.type = type;
            this.hologramName = hologramName;
            this.lineNumber = -1;
            this.pageIndex = -1;
            this.callback = callback;
        }

        public InputContext(InputType type, String hologramName, int lineNumber, int pageIndex, Consumer<String> callback) {
            this.type = type;
            this.hologramName = hologramName;
            this.lineNumber = lineNumber;
            this.pageIndex = pageIndex;
            this.callback = callback;
        }

        public InputType getType() {
            return type;
        }

        public String getHologramName() {
            return hologramName;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getPageIndex() {
            return pageIndex;
        }
    }
}
