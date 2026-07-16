package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.util.ColorUtil;
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

    public AbstractActionManageGui(String id, String title, int size,
                                   WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                                   String hologramName, int pageIndex, ClickType currentClickType) {
        super(id, ColorUtil.colorize(title), size);
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
                    .name("&f全息图不存在")
                    .lore(Arrays.asList("", "&7该全息图已被删除", "", "&e点击返回列表"))
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
                .name("&f返回")
                .lore(Arrays.asList("&7返回" + getTargetDescription(), "", "&e点击返回"))
                .onClick(context -> goBack(context.getPlayer()))
                .build());

        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologramName + " - " + getTargetDescription())
                .lore(Arrays.asList("", "&7当前点击类型: &f" + currentClickType.getDescription(), ""))
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
                    .name("&f动作 #" + (i + 1))
                    .lore(Arrays.asList(
                            "",
                            "&7类型: &f" + action.getType().getName(),
                            "&7值: &f" + truncate(action.getData(), 30),
                            "",
                            "&e左键点击编辑",
                            "&c右键点击删除"
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
                .name("&f添加动作")
                .lore(Arrays.asList("&7添加新的点击动作", "", "&e点击选择动作类型"))
                .onClick(context -> addAction(context.getPlayer()))
                .build());

        setButton(49, GuiButton.builder(Material.BOOK)
                .name("&f动作类型说明")
                .lore(Arrays.asList(
                        "",
                        "&7可用动作类型:",
                        "&fMESSAGE &7- 发送消息",
                        "&fCOMMAND &7- 执行命令",
                        "&fCONSOLE &7- 控制台命令",
                        "&fSOUND &7- 播放声音",
                        "&fTELEPORT &7- 传送玩家",
                        "&fSERVER &7- 跨服传送",
                        "&fNEXT_PAGE &7- 下一页",
                        "&fPREV_PAGE &7- 上一页",
                        "&fPAGE &7- 页面跳转",
                        ""
                ))
                .build());
    }

    protected void deleteAction(Player player, int actionIndex) {
        guiManager.openGui(player, ConfirmGui.createDeleteActionConfirm(actionIndex, confirmed -> {
            if (confirmed) {
                removeAction(actionIndex);
                Hologram h = getHologram();
                if (h != null) {
                    h.save();
                }
                player.sendMessage(ColorUtil.colorize("&a已删除动作！"));
            }
            reopenGui(player);
        }));
    }

    protected void editAction(Player player, int actionIndex, Action action) {
        player.closeInventory();

        player.sendMessage(ColorUtil.colorize("&7当前动作: &f" + action.getType().getName() + ":" + action.getData()));
        player.sendMessage(ColorUtil.colorize("&7输入新值或输入 &ecancel &7取消"));

        chatInputManager.requestInput(player, "&a请输入新的动作值:",
                ChatInputManager.InputType.ACTION_VALUE, hologramName, input -> {
                    String[] parts = input.split(":", 2);
                    if (parts.length < 2) {
                        player.sendMessage(ColorUtil.colorize("&c格式错误！正确格式: <类型>:<值>"));
                    } else {
                        String typeStr = parts[0].toUpperCase();
                        String value = parts[1];

                        ActionType actionType = ActionType.getByName(typeStr);
                        if (actionType == null) {
                            player.sendMessage(ColorUtil.colorize("&c未知的动作类型！"));
                        } else {
                            updateAction(actionIndex, new Action(actionType, value));
                            Hologram h = getHologram();
                            if (h != null) {
                                h.save();
                            }
                            player.sendMessage(ColorUtil.colorize("&a已更新动作！"));
                        }
                    }
                    reopenGui(player);
                });
    }

    protected void addAction(Player player) {
        guiManager.openGui(player, new ActionTypeSelectGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
    }

    protected abstract void removeAction(int actionIndex);

    protected abstract void updateAction(int actionIndex, Action newAction);

    protected abstract void reopenGui(Player player);

    protected GuiButton createClickTypeButton(ClickType clickType, Material material) {
        boolean isSelected = currentClickType == clickType;
        List<Action> actions = getActions().getOrDefault(clickType, new ArrayList<>());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7动作数量: &f" + actions.size());
        lore.add("");
        if (isSelected) {
            lore.add("&a当前选中");
        } else {
            lore.add("&e点击选择");
        }

        return GuiButton.builder(isSelected ? Material.LIME_STAINED_GLASS_PANE : material)
                .name("&f" + clickType.getDescription())
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
