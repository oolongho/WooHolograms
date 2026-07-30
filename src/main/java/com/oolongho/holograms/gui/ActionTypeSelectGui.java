package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 动作类型选择 GUI
 * 通过回调模式支持页面级和行级动作添加。
 * 调用方通过 {@code onActionCreated} 回调接收创建好的 Action 对象，
 * 自行决定如何持久化（page 或 line）以及如何返回上一级 GUI。
 */
public class ActionTypeSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final Consumer<Action> onActionCreated;
    private final Runnable onBack;

    public ActionTypeSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, Consumer<Action> onActionCreated, Runnable onBack) {
        super("action_type_select", plugin.getMessages().get("gui.title-action-type-select"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.onActionCreated = onActionCreated;
        this.onBack = onBack;

        render();
    }

    private void render() {
        fillBackground();

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.action-type.lore-back"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> onBack.run())
                .build());

        setButton(10, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.action-type.message"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.message.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.MESSAGE,
                            plugin.getMessages().get("gui.prompt.action-message"));
                })
                .build());

        setButton(11, GuiButton.builder(Material.COMMAND_BLOCK)
                .name(plugin.getMessages().getString("gui.action-type.command"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.command.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.COMMAND,
                            plugin.getMessages().get("gui.prompt.action-command"));
                })
                .build());

        setButton(12, GuiButton.builder(Material.COMMAND_BLOCK_MINECART)
                .name(plugin.getMessages().getString("gui.action-type.console"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.console.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.CONSOLE,
                            plugin.getMessages().get("gui.prompt.action-console"));
                })
                .build());

        setButton(13, GuiButton.builder(Material.NOTE_BLOCK)
                .name(plugin.getMessages().getString("gui.action-type.sound"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.sound.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.SOUND,
                            plugin.getMessages().get("gui.prompt.action-sound"));
                })
                .build());

        setButton(14, GuiButton.builder(Material.ENDER_PEARL)
                .name(plugin.getMessages().getString("gui.action-type.teleport"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.teleport.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.TELEPORT,
                            plugin.getMessages().get("gui.prompt.action-teleport"));
                })
                .build());

        setButton(15, GuiButton.builder(Material.ENDER_CHEST)
                .name(plugin.getMessages().getString("gui.action-type.server"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.server.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.SERVER,
                            plugin.getMessages().get("gui.prompt.action-server"));
                })
                .build());

        setButton(16, GuiButton.builder(Material.ARROW)
                .name(plugin.getMessages().getString("gui.action-type.next-page"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.next-page.lore"))
                .onClick(context -> {
                    createActionDirect(context.getPlayer(), ActionType.NEXT_PAGE, hologramName);
                })
                .build());

        setButton(17, GuiButton.builder(Material.TIPPED_ARROW)
                .name(plugin.getMessages().getString("gui.action-type.prev-page"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.prev-page.lore"))
                .onClick(context -> {
                    createActionDirect(context.getPlayer(), ActionType.PREV_PAGE, hologramName);
                })
                .build());

        setButton(18, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.action-type.page"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-type.page.lore"))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.PAGE,
                            plugin.getMessages().get("gui.prompt.action-page"));
                })
                .build());
    }

    /**
     * 通过聊天输入请求动作值，然后创建动作并触发回调
     */
    private void requestInputAndCreateAction(Player player, ActionType actionType, Component prompt) {
        player.closeInventory();

        chatInputManager.requestInput(player, prompt, ChatInputManager.InputType.ACTION_VALUE, input -> {
            Action action = new Action(actionType, input);
            plugin.getMessages().send(player, "gui.msg-action-add-success", "type", actionType.getName());
            onActionCreated.accept(action);
        });
    }

    /**
     * 直接创建无需输入值的动作（NEXT_PAGE / PREV_PAGE），并触发回调
     */
    private void createActionDirect(Player player, ActionType actionType, String value) {
        Action action = new Action(actionType, value);
        plugin.getMessages().send(player, "gui.msg-action-add-success", "type", actionType.getName());
        onActionCreated.accept(action);
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 0; i < 27; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
}
