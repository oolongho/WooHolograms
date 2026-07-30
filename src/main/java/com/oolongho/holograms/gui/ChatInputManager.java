package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.util.SchedulerUtil;
import com.oolongho.holograms.util.SchedulerUtil.TaskHandle;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 聊天框输入管理器
 * 用于处理玩家在聊天框中的输入
 *
 * 提示语由调用方通过 {@code plugin.getMessages().get("gui.prompt.xxx")} 传入 Component
 * 验证错误消息统一使用 input.* 语言键（带 {prefix}）
 */
public class ChatInputManager implements Listener {

    private final WooHolograms plugin;
    private final Map<UUID, InputContext> pendingInputs;
    private final Map<UUID, TaskHandle> timeoutTasks;
    private static final long INPUT_TIMEOUT = 30 * 20; // 30秒超时

    public ChatInputManager(WooHolograms plugin) {
        this.plugin = plugin;
        this.pendingInputs = new ConcurrentHashMap<>();
        this.timeoutTasks = new ConcurrentHashMap<>();
    }

    /**
     * 注册事件监听器
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示语（Component，由 messages.get() 构造）
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, Component prompt, Consumer<String> callback) {
        requestInput(player, prompt, InputType.GENERIC, callback);
    }

    /**
     * 请求玩家输入
     * @param player 玩家
     * @param prompt 提示语（Component）
     * @param type 输入类型
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, Component prompt, InputType type, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(type, callback));
    }

    /**
     * 请求玩家输入（带上下文）
     * @param player 玩家
     * @param prompt 提示语（Component）
     * @param type 输入类型
     * @param hologramName 全息图名称
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, Component prompt, InputType type, String hologramName, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(type, hologramName, callback));
    }

    /**
     * 请求玩家输入（带完整上下文）
     * @param player 玩家
     * @param prompt 提示语（Component）
     * @param type 输入类型
     * @param hologramName 全息图名称
     * @param lineNumber 行号
     * @param pageIndex 页码
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, Component prompt, InputType type, String hologramName, int lineNumber, int pageIndex, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(type, hologramName, lineNumber, pageIndex, callback));
    }

    private void requestInputInternal(Player player, Component prompt, InputContext context) {
        UUID playerId = player.getUniqueId();

        cancelTimeoutTask(playerId);

        pendingInputs.put(playerId, context);

        player.sendMessage(prompt);
        plugin.getMessages().send(player, "input.cancel-hint");

        timeoutTasks.put(playerId, createTimeoutTask(player, playerId, context));
    }

    private TaskHandle createTimeoutTask(Player player, UUID playerId, InputContext context) {
        return SchedulerUtil.runTaskLater(player, () -> {
            if (pendingInputs.containsKey(playerId) && pendingInputs.get(playerId) == context) {
                pendingInputs.remove(playerId);
                timeoutTasks.remove(playerId);
                plugin.getMessages().send(player, "input.timeout");
            }
        }, INPUT_TIMEOUT);
    }

    /**
     * 取消超时任务
     * @param playerId 玩家ID
     */
    private void cancelTimeoutTask(UUID playerId) {
        TaskHandle task = timeoutTasks.remove(playerId);
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

        if (!pendingInputs.containsKey(playerId)) {
            return;
        }

        event.setCancelled(true);
        String input = event.getMessage();

        SchedulerUtil.runTask(player, () -> {
            InputContext context = pendingInputs.remove(playerId);
            if (context == null) {
                return;
            }

            cancelTimeoutTask(playerId);

            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("取消")) {
                plugin.getMessages().send(player, "input.cancelled");
                return;
            }

            Component validationError = validateInput(input, context.getType());
            if (validationError != null) {
                player.sendMessage(validationError);
                pendingInputs.put(playerId, context);
                timeoutTasks.put(playerId, createTimeoutTask(player, playerId, context));
                return;
            }

            context.callback.accept(input);
        });
    }

    /**
     * 验证输入
     * @param input 输入内容
     * @param type 输入类型
     * @return 错误消息 Component，null表示验证通过
     */
    private Component validateInput(String input, InputType type) {
        int maxLength = plugin.getConfigManager().getMaxInputLength();

        if (input == null || input.isEmpty()) {
            return plugin.getMessages().get("input.empty");
        }

        if (input.length() > maxLength) {
            return plugin.getMessages().get("input.too-long", "max", String.valueOf(maxLength));
        }

        // 根据类型进行特定验证
        switch (type) {
            case HOLOGRAM_NAME -> {
                if (!input.matches("^[\\w\\-\\p{L}]+$")) {
                    return plugin.getMessages().get("input.invalid-name");
                }
            }
            case DISPLAY_RANGE, UPDATE_INTERVAL -> {
                try {
                    int value = Integer.parseInt(input);
                    if (value <= 0) {
                        return plugin.getMessages().get("input.must-be-positive");
                    }
                } catch (NumberFormatException e) {
                    return plugin.getMessages().get("input.invalid-number");
                }
            }
            case LINE_HEIGHT -> {
                try {
                    double value = Double.parseDouble(input);
                    if (value <= 0) {
                        return plugin.getMessages().get("input.must-be-positive");
                    }
                } catch (NumberFormatException e) {
                    return plugin.getMessages().get("input.invalid-number");
                }
            }
            case LINE_OFFSET -> {
                if (!input.matches("^[\\d.\\- ]+$")) {
                    return plugin.getMessages().get("input.invalid-offset");
                }
            }
            case COORDINATES -> {
                if (!input.matches("^[\\d.\\- ]+$")) {
                    return plugin.getMessages().get("input.invalid-coords");
                }
            }
            default -> {
            }
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
