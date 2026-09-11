package com.oolongho.holograms.api.registry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 自定义动作执行器（函数式接口）
 *
 * <p>通过 {@code ActionTypeRegistry#register} 注册后，
 * 玩家点击全息图时可触发自定义逻辑：</p>
 * <pre>{@code
 * api.actions().register("SHOP_OPEN", (player, args) -> {
 *     player.sendMessage("打开商店: " + String.join(" ", args));
 *     return true;   // false 表示动作执行失败，将中断后续动作
 * });
 * // 行内容中即可配置: SHOP_OPEN:shop1
 * }</pre>
 */
@FunctionalInterface
public interface ActionExecutor {

    /**
     * 执行动作
     *
     * @param player 点击的玩家
     * @param args   动作参数（动作数据按空格拆分；可能为空数组）
     * @return true 继续执行后续动作；false 中断本次点击的后续动作
     */
    boolean execute(@Nullable Player player, @NotNull String[] args);
}
