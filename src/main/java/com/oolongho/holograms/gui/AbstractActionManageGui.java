package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractActionManageGui extends GuiScreen {

    protected final WooHolograms plugin;
    protected final GuiManager guiManager;
    protected final ChatInputManager chatInputManager;
    protected final String hologramName;
    protected final int pageIndex;
    protected ClickType currentClickType;

    public AbstractActionManageGui(String id, Component title, int size,
                                   WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                                   String hologramName, int pageIndex, ClickType currentClickType) {
        super(id, title, size);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.currentClickType = currentClickType;
    }

    protected abstract Map<ClickType, List<Action>> getActions();

    protected abstract void setActions(Map<ClickType, List<Action>> actions);

    protected abstract String getTargetDescription();

    protected abstract void goBack(Player player);

    protected abstract void onNullTarget(Player player);

    protected Hologram getHologram() {
        return plugin.getHologramManager().getHologram(hologramName);
    }

    protected void render() {
        clearButtons();

        Hologram hologram = getHologram();
        if (hologram == null) {
            setButton(22, GuiButton.builder(Material.BARRIER)
                    .name(plugin.getMessages().getString("gui.btn-hologram-not-exists"))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.lore-hologram-deleted"),
                            "",
                            plugin.getMessages().getString("gui.lore-click-back-list")))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }

        if (!hasTarget()) {
            onNullTarget(null);
            return;
        }

        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.btn-back"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.action-manage.lore-back", "target", getTargetDescription()),
                        "",
                        plugin.getMessages().getString("gui.lore-click-back")))
                .onClick(context -> goBack(context.getPlayer()))
                .build());

        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name(plugin.getMessages().getString("gui.action-manage.name-title",
                        "name", hologramName, "target", getTargetDescription()))
                .lore(Arrays.asList(
                        "",
                        plugin.getMessages().getString("gui.action-manage.lore-click-type",
                                "type", plugin.getMessages().getRaw(currentClickType.getDescriptionKey())),
                        ""))
                .build());

        renderClickTypeButtons();

        List<Action> actions = getActions().getOrDefault(currentClickType, new ArrayList<>());
        renderActionList(actions);

        renderBottomButtons();

        fillBackground();
    }

    protected boolean hasTarget() {
        return true;
    }

    protected void renderClickTypeButtons() {
        setButton(36, createClickTypeButton(ClickType.LEFT, Material.STONE_BUTTON));
        setButton(37, createClickTypeButton(ClickType.RIGHT, Material.OAK_BUTTON));
        setButton(38, createClickTypeButton(ClickType.SHIFT_LEFT, Material.SPRUCE_BUTTON));
        setButton(39, createClickTypeButton(ClickType.SHIFT_RIGHT, Material.BIRCH_BUTTON));
    }

    protected void renderActionList(List<Action> actions) {
        int actionCount = actions.size();
        for (int i = 0; i < actionCount && i < 27; i++) {
            int slot = 9 + i;
            if (slot >= 36) break;

            final int actionIndex = i;
            Action action = actions.get(i);

            setButton(slot, GuiButton.builder(Material.COMMAND_BLOCK)
                    .name(plugin.getMessages().getString("gui.action-manage.btn-action",
                            "index", String.valueOf(i + 1)))
                    .lore(Arrays.asList(
                            "",
                            plugin.getMessages().getString("gui.action-manage.lore-type",
                                    "type", action.getType().getName()),
                            plugin.getMessages().getString("gui.action-manage.lore-value",
                                    "value", truncate(action.getData(), 30)),
                            "",
                            plugin.getMessages().getString("gui.action-manage.lore-left-click-edit"),
                            plugin.getMessages().getString("gui.action-manage.lore-right-click-delete")
                    ))
                    .onClick(context -> {
                        Player player = context.getPlayer();
                        if (context.getClickType().isRightClick()) {
                            deleteAction(player, actionIndex);
                        } else {
                            editAction(player, actionIndex, action);
                        }
                    })
                    .build());
        }
    }

    protected void renderBottomButtons() {
        setButton(45, GuiButton.builder(Material.EMERALD)
                .name(plugin.getMessages().getString("gui.action-manage.btn-add-action"))
                .lore(Arrays.asList(
                        plugin.getMessages().getString("gui.action-manage.lore-add-action-desc"),
                        "",
                        plugin.getMessages().getString("gui.lore-click-select")))
                .onClick(context -> addAction(context.getPlayer()))
                .build());

        setButton(49, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.action-manage.btn-type-help"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.action-manage.type-help-lore"))
                .build());
    }

    protected void deleteAction(Player player, int actionIndex) {
        guiManager.openGui(player, ConfirmGui.createDeleteActionConfirm(plugin, actionIndex, confirmed -> {
            if (confirmed) {
                removeAction(actionIndex);
                Hologram h = getHologram();
                if (h != null) {
                    h.save();
                }
                plugin.getMessages().send(player, "gui.msg-action-deleted");
            }
            reopenGui(player);
        }));
    }

    protected void editAction(Player player, int actionIndex, Action action) {
        player.closeInventory();

        plugin.getMessages().send(player, "gui.msg-action-current",
                "type", action.getType().getName(), "value", action.getData());
        plugin.getMessages().send(player, "gui.msg-action-input-hint");

        chatInputManager.requestInput(player, plugin.getMessages().get("gui.prompt.action-value"),
                ChatInputManager.InputType.ACTION_VALUE, hologramName, input -> {
                    String[] parts = input.split(":", 2);
                    if (parts.length < 2) {
                        plugin.getMessages().send(player, "gui.msg-action-format-error");
                    } else {
                        String typeStr = parts[0].toUpperCase();
                        String value = parts[1];

                        ActionType actionType = ActionType.getByName(typeStr);
                        if (actionType == null) {
                            plugin.getMessages().send(player, "gui.msg-action-type-unknown");
                        } else {
                            updateAction(actionIndex, new Action(actionType, value));
                            Hologram h = getHologram();
                            if (h != null) {
                                h.save();
                            }
                            plugin.getMessages().send(player, "gui.msg-action-update-success");
                        }
                    }
                    reopenGui(player);
                });
    }

    /**
     * 由子类决定如何添加动作（通常打开 ActionTypeSelectGui，并通过回调持久化到 page 或 line）
     */
    protected abstract void addAction(Player player);

    protected abstract void removeAction(int actionIndex);

    protected abstract void updateAction(int actionIndex, Action newAction);

    protected abstract void reopenGui(Player player);

    protected GuiButton createClickTypeButton(ClickType clickType, Material material) {
        boolean isSelected = currentClickType == clickType;
        List<Action> actions = getActions().getOrDefault(clickType, new ArrayList<>());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(plugin.getMessages().getString("gui.action-manage.lore-action-count",
                "count", String.valueOf(actions.size())));
        lore.add("");
        if (isSelected) {
            lore.add(plugin.getMessages().getString("gui.lore-current-selected"));
        } else {
            lore.add(plugin.getMessages().getString("gui.lore-click-select"));
        }

        return GuiButton.builder(isSelected ? Material.LIME_STAINED_GLASS_PANE : material)
                .name("<white>" + plugin.getMessages().getRaw(clickType.getDescriptionKey()))
                .lore(lore)
                .onClick(context -> {
                    if (!isSelected) {
                        currentClickType = clickType;
                        reopenGui(context.getPlayer());
                    }
                })
                .build();
    }

    protected String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    protected void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 40, 41, 42, 43, 44, 47, 48, 50, 51, 52, 53};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
