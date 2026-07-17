package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineActionManageGui extends AbstractActionManageGui {

    private final int lineIndex;

    public LineActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, int lineIndex) {
        super("line_action_manage", "&8行动作管理", 54,
                plugin, guiManager, chatInputManager, hologramName, pageIndex, ClickType.ANY);
        this.lineIndex = lineIndex;
        render();
    }

    @Override
    protected Map<ClickType, List<Action>> getActions() {
        Hologram hologram = getHologram();
        if (hologram == null) return new HashMap<>();

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex >= page.size()) return new HashMap<>();

        HologramLine line = page.getLine(lineIndex);
        if (line == null) return new HashMap<>();

        return line.getActions();
    }

    @Override
    protected void setActions(Map<ClickType, List<Action>> actions) {
    }

    @Override
    protected String getTargetDescription() {
        return "行编辑";
    }

    @Override
    protected void goBack(Player player) {
        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    @Override
    protected void onNullTarget(Player player) {
        setButton(22, GuiButton.builder(Material.BARRIER)
                .name("&f行不存在")
                .lore(Arrays.asList("", "&7该行已被删除", "", "&e点击返回详情"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());
    }

    @Override
    protected boolean hasTarget() {
        Hologram hologram = getHologram();
        if (hologram == null) return false;
        HologramPage page = hologram.getPage(pageIndex);
        return page != null && lineIndex < page.size();
    }

    @Override
    protected void renderClickTypeButtons() {
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
    }

    @Override
    protected void renderActionList(List<Action> actions) {
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
                            HologramLine line = getLine();
                            if (line != null) {
                                line.removeAction(currentClickType, actionIndex);
                                getHologram().save();
                                context.getPlayer().sendMessage(ColorUtil.colorize("&c动作已删除！"));
                            }
                            reopenGui(context.getPlayer());
                        } else {
                            context.getPlayer().closeInventory();
                            chatInputManager.requestInput(context.getPlayer(), "&a请输入新的动作数据:",
                                    ChatInputManager.InputType.ACTION_VALUE, hologramName, lineIndex, pageIndex, input -> {
                                        action.setData(input);
                                        getHologram().save();
                                        context.getPlayer().sendMessage(ColorUtil.colorize("&a动作数据已更新！"));
                                        reopenGui(context.getPlayer());
                                    });
                        }
                    })
                    .build());
        }
    }

    @Override
    protected void renderBottomButtons() {
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
                                    HologramLine line = getLine();
                                    if (line != null) {
                                        line.addAction(currentClickType, action);
                                        getHologram().save();
                                        context.getPlayer().sendMessage(ColorUtil.colorize("&a动作已添加！"));
                                    }
                                } catch (IllegalArgumentException e) {
                                    context.getPlayer().sendMessage(ColorUtil.colorize("&c无效的动作格式: " + e.getMessage()));
                                }
                                reopenGui(context.getPlayer());
                            });
                })
                .build());

        setButton(47, GuiButton.builder(Material.BARRIER)
                .name("&c清空当前类型动作")
                .lore(Arrays.asList(
                        "&7清空 " + currentClickType.name() + " 类型的所有动作",
                        "",
                        "&c点击清空"
                ))
                .onClick(context -> {
                    HologramLine line = getLine();
                    if (line != null) {
                        line.clearActions(currentClickType);
                        getHologram().save();
                        context.getPlayer().sendMessage(ColorUtil.colorize("&c已清空 " + currentClickType.name() + " 类型的所有动作！"));
                    }
                    reopenGui(context.getPlayer());
                })
                .build());

        setButton(49, GuiButton.builder(Material.TNT)
                .name("&4清空所有动作")
                .lore(Arrays.asList(
                        "&7清空此行的所有动作",
                        "",
                        "&c点击清空"
                ))
                .onClick(context -> {
                    HologramLine line = getLine();
                    if (line != null) {
                        line.clearAllActions();
                        getHologram().save();
                        context.getPlayer().sendMessage(ColorUtil.colorize("&c已清空所有动作！"));
                    }
                    reopenGui(context.getPlayer());
                })
                .build());

        setButton(51, GuiButton.builder(Material.ARROW)
                .name("&e快速添加: 下一页")
                .lore(Arrays.asList("&7添加 NEXT_PAGE 动作", "", "&e点击添加"))
                .onClick(context -> {
                    HologramLine line = getLine();
                    if (line != null) {
                        Action action = new Action(ActionType.NEXT_PAGE, hologramName);
                        action.setClickType(currentClickType);
                        line.addAction(currentClickType, action);
                        getHologram().save();
                        context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加下一页动作！"));
                    }
                    reopenGui(context.getPlayer());
                })
                .build());

        setButton(52, GuiButton.builder(Material.ARROW)
                .name("&e快速添加: 上一页")
                .lore(Arrays.asList("&7添加 PREV_PAGE 动作", "", "&e点击添加"))
                .onClick(context -> {
                    HologramLine line = getLine();
                    if (line != null) {
                        Action action = new Action(ActionType.PREV_PAGE, hologramName);
                        action.setClickType(currentClickType);
                        line.addAction(currentClickType, action);
                        getHologram().save();
                        context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加上一页动作！"));
                    }
                    reopenGui(context.getPlayer());
                })
                .build());

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

    @Override
    protected void removeAction(int actionIndex) {
        HologramLine line = getLine();
        if (line != null) {
            line.removeAction(currentClickType, actionIndex);
        }
    }

    @Override
    protected void updateAction(int actionIndex, Action newAction) {
        HologramLine line = getLine();
        if (line != null) {
            line.setAction(currentClickType, actionIndex, newAction);
        }
    }

    @Override
    protected void reopenGui(Player player) {
        guiManager.openGui(player, new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    private HologramLine getLine() {
        Hologram hologram = getHologram();
        if (hologram == null) return null;
        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex >= page.size()) return null;
        return page.getLine(lineIndex);
    }
}
