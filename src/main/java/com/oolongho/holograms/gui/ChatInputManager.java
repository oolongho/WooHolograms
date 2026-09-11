package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.util.SchedulerUtil;
import com.oolongho.holograms.util.SchedulerUtil.TaskHandle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.time.Duration;
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
    private static final long INPUT_TIMEOUT = 120 * 20; // 120秒超时(给玩家留足构思时间)

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

    /**
     * 请求玩家输入（带预填：提示后附一条可点击消息，点击将当前内容填入聊天框供修改）
     *
     * <p>预填内容为原始文本（含 & 颜色代码等），提交后与配置存储格式一致。</p>
     *
     * @param player  玩家
     * @param prompt  提示语（Component）
     * @param prefill 当前内容（null 或空则不显示预填按钮）
     * @param callback 输入完成回调
     */
    public void requestInput(Player player, Component prompt, String prefill, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(InputType.GENERIC, callback).withPrefill(prefill));
    }

    /**
     * 请求玩家输入（完整上下文 + 预填）
     *
     * @param player       玩家
     * @param prompt       提示语（Component）
     * @param type         输入类型
     * @param hologramName 全息图名称
     * @param lineNumber   行号
     * @param pageIndex    页码
     * @param prefill      当前内容（null 或空则不显示预填按钮）
     * @param callback     输入完成回调
     */
    public void requestInput(Player player, Component prompt, InputType type, String hologramName, int lineNumber, int pageIndex, String prefill, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(type, hologramName, lineNumber, pageIndex, callback).withPrefill(prefill));
    }

    /**
     * 请求玩家输入（带预填；供 (type, hologramName) 形状使用，
     * 该形状无法直接插入 prefill 参数——会与既有重载发生擦除冲突）
     *
     * @param player       玩家
     * @param prompt       提示语（Component）
     * @param type         输入类型
     * @param hologramName 全息图名称
     * @param prefill      当前内容（null 或空则不显示预填按钮）
     * @param callback     输入完成回调
     */
    public void requestInputPrefill(Player player, Component prompt, InputType type, String hologramName, String prefill, Consumer<String> callback) {
        requestInputInternal(player, prompt, new InputContext(type, hologramName, callback).withPrefill(prefill));
    }

    /**
     * 请求玩家输入（自定义上下文，供需要预填的其它重载形状使用）
     *
     * @param player  玩家
     * @param prompt  提示语（Component）
     * @param context 输入上下文（可用 withPrefill 附加预填内容）
     */
    public void requestInput(Player player, Component prompt, InputContext context) {
        requestInputInternal(player, prompt, context);
    }

    private void requestInputInternal(Player player, Component prompt, InputContext context) {
        UUID playerId = player.getUniqueId();

        cancelTimeoutTask(playerId);

        pendingInputs.put(playerId, context);

        player.sendMessage(prompt);
        // 预填提示：点击后聊天栏填入当前内容供修改（suggest_command 不覆盖已有输入）
        String prefill = context.getPrefill();
        if (prefill != null && !prefill.isEmpty()) {
            player.sendMessage(buildPrefillMessage(prefill));
        }
        // 取消提示:文本 + 可点击取消按钮(打字 cancel/取消 仍然可用)
        player.sendMessage(buildCancelMessage(playerId, player, context));

        timeoutTasks.put(playerId, createTimeoutTask(player, playerId, context));
    }

    /**
     * 构建预填提示消息：当前内容预览（原样截断显示）+ 可点击预填按钮
     *
     * <p>预览有意显示原始文本（含 & 色码），与提交后插件解析的格式一致。</p>
     *
     * @param prefill 当前内容
     * @return 消息 Component
     */
    private Component buildPrefillMessage(String prefill) {
        String preview = prefill.length() > 32 ? prefill.substring(0, 32) + "..." : prefill;
        Component current = plugin.getMessages().get("input.prefill-current", "value", preview);
        Component button = plugin.getMessages().get("input.prefill-button")
                .clickEvent(ClickEvent.suggestCommand(prefill))
                .hoverEvent(plugin.getMessages().get("input.prefill-hover"));
        return current.append(Component.space()).append(button);
    }

    /**
     * 构建取消提示行：原提示文本 + 可点击取消按钮（Paper ClickCallback）
     *
     * <p>回调内做实例身份比对：仅当当前挂起的仍是发起本次输入的上下文时才取消，
     * 防止玩家点击聊天记录里旧消息的取消按钮误杀后续发起的新输入。
     * 按钮 uses=1、生命周期略长于输入超时，过期后点击自然失效。</p>
     */
    private Component buildCancelMessage(UUID playerId, Player player, InputContext context) {
        Component cancelButton = plugin.getMessages().get("input.cancel-button")
                .clickEvent(ClickEvent.callback(audience -> {
                    if (pendingInputs.get(playerId) == context) {
                        pendingInputs.remove(playerId);
                        cancelTimeoutTask(playerId);
                        plugin.getMessages().send(player, "input.cancelled");
                    }
                }, builder -> builder.uses(1).lifetime(Duration.ofSeconds(INPUT_TIMEOUT / 20 + 5))))
                .hoverEvent(plugin.getMessages().get("input.cancel-hover"));
        return plugin.getMessages().get("input.cancel-hint").append(Component.space()).append(cancelButton);
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
        /** 预填内容（null 表示无预填） */
        private String prefill;

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

        /**
         * 附加预填内容（链式）
         *
         * @param prefill 当前内容（null 或空则不显示预填按钮）
         * @return this
         */
        public InputContext withPrefill(String prefill) {
            this.prefill = prefill;
            return this;
        }

        /**
         * 获取预填内容
         *
         * @return 预填内容；无预填返回 null
         */
        public String getPrefill() {
            return prefill;
        }
    }
}
