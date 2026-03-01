package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 动作管理 GUI
 * 用于管理全息图的点击动作
 * 
 * @author oolongho
 */
public class ActionManageGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private ClickType currentClickType;

    public ActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, 
                          String hologramName, int pageIndex) {
        super("action_manage", ColorUtil.colorize("&8动作管理: " + hologramName + " P" + (pageIndex + 1)), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.currentClickType = ClickType.LEFT;
        
        render();
    }

    public ActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, 
                          String hologramName, int pageIndex, ClickType clickType) {
        super("action_manage", ColorUtil.colorize("&8动作管理: " + hologramName + " P" + (pageIndex + 1)), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.currentClickType = clickType;
        
        render();
    }

    private void render() {
        clearButtons();
        
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            setButton(22, GuiButton.builder(Material.BARRIER)
                    .name("&f全息图不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该全息图已被删除",
                            "",
                            "&e点击返回列表"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                    })
                    .build());
            return;
        }
        
        HologramPage page = hologram.getPage(pageIndex);
        if (page == null) {
            setButton(22, GuiButton.builder(Material.BARRIER)
                    .name("&f页面不存在")
                    .lore(Arrays.asList(
                            "",
                            "&7该页面已被删除",
                            "",
                            "&e点击返回详情"
                    ))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                    })
                    .build());
            return;
        }
        
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回全息图详情",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
                })
                .build());
        
        setButton(4, GuiButton.builder(Material.NAME_TAG)
                .name("&f" + hologramName + " - 第 " + (pageIndex + 1) + " 页")
                .lore(Arrays.asList(
                        "",
                        "&7当前点击类型: &f" + getClickTypeName(currentClickType),
                        ""
                ))
                .build());
        
        setButton(36, createClickTypeButton(ClickType.LEFT, Material.STONE_BUTTON));
        setButton(37, createClickTypeButton(ClickType.RIGHT, Material.OAK_BUTTON));
        setButton(38, createClickTypeButton(ClickType.SHIFT_LEFT, Material.SPRUCE_BUTTON));
        setButton(39, createClickTypeButton(ClickType.SHIFT_RIGHT, Material.BIRCH_BUTTON));
        
        List<Action> actions = page.getActions(currentClickType);
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
                            guiManager.openGui(player, ConfirmGui.createDeleteActionConfirm(actionIndex, confirmed -> {
                                if (confirmed) {
                                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                    if (h != null) {
                                        HologramPage p = h.getPage(pageIndex);
                                        if (p != null) {
                                            List<Action> actionList = p.getActions(currentClickType);
                                            if (actionIndex < actionList.size()) {
                                                actionList.remove(actionIndex);
                                                h.save();
                                                player.sendMessage(ColorUtil.colorize("&a已删除动作！"));
                                            }
                                        }
                                    }
                                    guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
                                } else {
                                    guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
                                }
                            }));
                        } else {
                            editAction(player, actionIndex, action);
                        }
                    })
                    .build());
        }
        
        setButton(45, GuiButton.builder(Material.EMERALD)
                .name("&f添加动作")
                .lore(Arrays.asList(
                        "&7添加新的点击动作",
                        "",
                        "&7格式: <类型>:<值>",
                        "&7例如: message:&a你好!",
                        "&7      command:spawn",
                        "&7      sound:ENTITY_PLAYER_LEVELUP",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    Player player = context.getPlayer();
                    player.closeInventory();
                    
                    chatInputManager.requestInput(player, "&a请输入动作值 (格式: <类型>:<值>):", 
                            ChatInputManager.InputType.ACTION_VALUE, hologramName, input -> {
                        String[] parts = input.split(":", 2);
                        if (parts.length < 2) {
                            player.sendMessage(ColorUtil.colorize("&c格式错误！正确格式: <类型>:<值>"));
                        } else {
                            String typeStr = parts[0].toUpperCase();
                            String value = parts[1];
                            
                            ActionType actionType = ActionType.getByName(typeStr);
                            if (actionType == null) {
                                List<String> typeNames = new ArrayList<>();
                                for (ActionType t : ActionType.getActionTypes()) {
                                    typeNames.add(t.getName());
                                }
                                player.sendMessage(ColorUtil.colorize("&c未知的动作类型！可用类型: " + String.join(", ", typeNames)));
                            } else {
                                Hologram h = plugin.getHologramManager().getHologram(hologramName);
                                if (h != null) {
                                    HologramPage p = h.getPage(pageIndex);
                                    if (p != null) {
                                        Action action = new Action(actionType, value);
                                        p.addAction(currentClickType, action);
                                        h.save();
                                        player.sendMessage(ColorUtil.colorize("&a已添加动作！"));
                                    }
                                }
                            }
                        }
                        guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
                    });
                })
                .build());
        
        setButton(49, GuiButton.builder(Material.BOOK)
                .name("&f动作类型说明")
                .lore(Arrays.asList(
                        "",
                        "&7可用动作类型:",
                        "&fMESSAGE &7- 发送消息",
                        "&fCOMMAND &7- 执行命令",
                        "&fSOUND &7- 播放声音",
                        "&fTELEPORT &7- 传送玩家",
                        ""
                ))
                .build());
        
        fillBackground();
    }

    private GuiButton createClickTypeButton(ClickType clickType, Material material) {
        boolean isSelected = currentClickType == clickType;
        List<Action> actions = getActionsForType(clickType);
        
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
                .name("&f" + getClickTypeName(clickType))
                .lore(lore)
                .onClick(context -> {
                    if (!isSelected) {
                        currentClickType = clickType;
                        guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType));
                    }
                })
                .build();
    }

    private List<Action> getActionsForType(ClickType clickType) {
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) return new ArrayList<>();
        
        HologramPage page = hologram.getPage(pageIndex);
        if (page == null) return new ArrayList<>();
        
        return page.getActions(clickType);
    }

    private void editAction(Player player, int actionIndex, Action action) {
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
                    Hologram h = plugin.getHologramManager().getHologram(hologramName);
                    if (h != null) {
                        HologramPage p = h.getPage(pageIndex);
                        if (p != null) {
                            List<Action> actions = p.getActions(currentClickType);
                            if (actionIndex < actions.size()) {
                                actions.set(actionIndex, new Action(actionType, value));
                                h.save();
                                player.sendMessage(ColorUtil.colorize("&a已更新动作！"));
                            }
                        }
                    }
                }
            }
            guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
        });
    }

    private String getClickTypeName(ClickType clickType) {
        return clickType.getDescription();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    private void fillBackground() {
        GuiButton background = GuiButton.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        int[] backgroundSlots = {1, 2, 3, 5, 6, 7, 8, 40, 41, 42, 43, 44, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : backgroundSlots) {
            if (getButton(slot) == null) {
                setButton(slot, background);
            }
        }
    }
}
