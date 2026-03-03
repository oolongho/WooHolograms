package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class LineActionManageGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final int lineIndex;
    private ClickType currentClickType;

    public LineActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, int lineIndex) {
        super("line_action_manage", ColorUtil.colorize("&8行动作管理"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.lineIndex = lineIndex;
        this.currentClickType = ClickType.ANY;
        
        render();
    }

    private void render() {
        clearButtons();
        
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
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
        
        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex >= page.size()) {
            setButton(22, GuiButton.builder(Material.BARRIER)
                    .name("&f行不存在")
                    .lore(Arrays.asList("", "&7该行已被删除", "", "&e点击返回详情"))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                    })
                    .build());
            return;
        }
        
        HologramLine line = page.getLine(lineIndex);
        
        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回行编辑", "", "&e点击返回"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 点击类型选择
        setButton(4, GuiButton.builder(Material.STONE_BUTTON)
                .name("&f点击类型: &e" + currentClickType.name())
                .lore(Arrays.asList(
                        "&7当前选择的点击类型",
                        "",
                        "&7左键: &f切换到下一个",
                        "&7右键: &f切换到上一个",
                        "",
                        "&7类型说明:",
                        "&fANY &7- 任意点击",
                        "&fLEFT &7- 左键点击",
                        "&fRIGHT &7- 右键点击",
                        "&fSHIFT_LEFT &7- Shift+左键",
                        "&fSHIFT_RIGHT &7- Shift+右键"
                ))
                .onClick(context -> {
                    ClickType[] types = ClickType.values();
                    int newIndex;
                    org.bukkit.event.inventory.ClickType bukkitClick = context.getClickType();
                    if (bukkitClick == org.bukkit.event.inventory.ClickType.RIGHT || bukkitClick == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT) {
                        newIndex = (currentClickType.ordinal() - 1 + types.length) % types.length;
                    } else {
                        newIndex = (currentClickType.ordinal() + 1) % types.length;
                    }
                    currentClickType = types[newIndex];
                    render();
                    guiManager.openGui(context.getPlayer(), this);
                })
                .build());
        
        // 显示当前点击类型的动作列表
        List<Action> actions = line.getActions(currentClickType);
        
        for (int i = 0; i < Math.min(actions.size(), 36); i++) {
            Action action = actions.get(i);
            int slot = 9 + i;
            final int actionIndex = i;
            
            setButton(slot, GuiButton.builder(Material.PAPER)
                    .name("&f动作 #" + (i + 1))
                    .lore(Arrays.asList(
                            "&7类型: &e" + action.getType().getName(),
                            "&7数据: &f" + (action.getData() != null ? action.getData() : "-"),
                            "&7点击类型: &f" + action.getClickType().name(),
                            "",
                            "&e左键: 编辑数据",
                            "&c右键: 删除"
                    ))
                    .onClick(context -> {
                        org.bukkit.event.inventory.ClickType bukkitClick = context.getClickType();
                        if (bukkitClick == org.bukkit.event.inventory.ClickType.RIGHT || bukkitClick == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT) {
                            // 右键删除
                            line.removeAction(currentClickType, actionIndex);
                            hologram.save();
                            context.getPlayer().sendMessage(ColorUtil.colorize("&c动作已删除！"));
                            guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                        } else {
                            // 左键编辑动作数据
                            context.getPlayer().closeInventory();
                            chatInputManager.requestInput(context.getPlayer(), "&a请输入新的动作数据:", 
                                    ChatInputManager.InputType.ACTION_VALUE, hologramName, lineIndex, pageIndex, input -> {
                                        action.setData(input);
                                        hologram.save();
                                        context.getPlayer().sendMessage(ColorUtil.colorize("&a动作数据已更新！"));
                                        guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                                    });
                        }
                    })
                    .build());
        }
        
        // 添加动作按钮
        setButton(45, GuiButton.builder(Material.EMERALD)
                .name("&a添加动作")
                .lore(Arrays.asList(
                        "&7点击添加新动作",
                        "",
                        "&7格式: TYPE:DATA",
                        "&7例如: COMMAND:spawn",
                        "&7例如: MESSAGE:&a你好！",
                        "",
                        "&7支持的动作类型:",
                        "&fCOMMAND &7- 以玩家身份执行命令",
                        "&fCONSOLE &7- 以控制台身份执行命令",
                        "&fMESSAGE &7- 发送消息",
                        "&fSOUND &7- 播放音效",
                        "&fTELEPORT &7- 传送",
                        "&fNEXT_PAGE &7- 下一页",
                        "&fPREV_PAGE &7- 上一页"
                ))
                .onClick(context -> {
                    context.getPlayer().closeInventory();
                    chatInputManager.requestInput(context.getPlayer(), "&a请输入动作 (格式: TYPE:DATA):", 
                            ChatInputManager.InputType.ACTION_VALUE, hologramName, lineIndex, pageIndex, input -> {
                                try {
                                    Action action = new Action(input);
                                    action.setClickType(currentClickType);
                                    line.addAction(currentClickType, action);
                                    hologram.save();
                                    context.getPlayer().sendMessage(ColorUtil.colorize("&a动作已添加！"));
                                } catch (IllegalArgumentException e) {
                                    context.getPlayer().sendMessage(ColorUtil.colorize("&c无效的动作格式: " + e.getMessage()));
                                }
                                guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                            });
                })
                .build());
        
        // 清空当前点击类型动作
        setButton(47, GuiButton.builder(Material.BARRIER)
                .name("&c清空当前类型动作")
                .lore(Arrays.asList(
                        "&7清空 " + currentClickType.name() + " 类型的所有动作",
                        "",
                        "&c点击清空"
                ))
                .onClick(context -> {
                    line.clearActions(currentClickType);
                    hologram.save();
                    context.getPlayer().sendMessage(ColorUtil.colorize("&c已清空 " + currentClickType.name() + " 类型的所有动作！"));
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 清空所有动作
        setButton(49, GuiButton.builder(Material.TNT)
                .name("&4清空所有动作")
                .lore(Arrays.asList(
                        "&7清空此行的所有动作",
                        "",
                        "&c点击清空"
                ))
                .onClick(context -> {
                    line.clearAllActions();
                    hologram.save();
                    context.getPlayer().sendMessage(ColorUtil.colorize("&c已清空所有动作！"));
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 快速添加翻页动作
        setButton(51, GuiButton.builder(Material.ARROW)
                .name("&e快速添加: 下一页")
                .lore(Arrays.asList(
                        "&7添加 NEXT_PAGE 动作",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    Action action = new Action(ActionType.NEXT_PAGE, hologramName);
                    action.setClickType(currentClickType);
                    line.addAction(currentClickType, action);
                    hologram.save();
                    context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加下一页动作！"));
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        setButton(52, GuiButton.builder(Material.ARROW)
                .name("&e快速添加: 上一页")
                .lore(Arrays.asList(
                        "&7添加 PREV_PAGE 动作",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    Action action = new Action(ActionType.PREV_PAGE, hologramName);
                    action.setClickType(currentClickType);
                    line.addAction(currentClickType, action);
                    hologram.save();
                    context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加上一页动作！"));
                    guiManager.openGui(context.getPlayer(), new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
                })
                .build());
        
        // 帮助信息
        setButton(53, GuiButton.builder(Material.KNOWLEDGE_BOOK)
                .name("&f动作类型帮助")
                .lore(Arrays.asList(
                        "&7可用的动作类型:",
                        "",
                        "&eCOMMAND:命令 &7- 执行命令",
                        "&eCONSOLE:命令 &7- 控制台执行",
                        "&eMESSAGE:消息 &7- 发送消息",
                        "&eSOUND:音效 &7- 播放音效",
                        "&eTELEPORT:坐标 &7- 传送",
                        "&eNEXT_PAGE &7- 下一页",
                        "&ePREV_PAGE &7- 上一页",
                        "",
                        "&7变量: {player} = 玩家名"
                ))
                .build());
    }
}
