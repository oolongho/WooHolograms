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

import java.util.Arrays;

public class ActionTypeSelectGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private final String hologramName;
    private final int pageIndex;
    private final ClickType clickType;

    public ActionTypeSelectGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, ClickType clickType) {
        super("action_type_select", ColorUtil.colorize("&8选择动作类型"), 27);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.hologramName = hologramName;
        this.pageIndex = pageIndex;
        this.clickType = clickType;
        
        render();
    }

    private void render() {
        fillBackground();
        
        setButton(0, GuiButton.builder(Material.SPECTRAL_ARROW)
                .name("&f返回")
                .lore(Arrays.asList(
                        "&7返回动作管理",
                        "",
                        "&e点击返回"
                ))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType));
                })
                .build());
        
        setButton(10, GuiButton.builder(Material.PAPER)
                .name("&f消息 &7(MESSAGE)")
                .lore(Arrays.asList(
                        "&7向玩家发送消息",
                        "&7支持颜色代码",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.MESSAGE, "请输入要发送的消息内容:");
                })
                .build());
        
        setButton(11, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&f玩家命令 &7(COMMAND)")
                .lore(Arrays.asList(
                        "&7以玩家身份执行命令",
                        "&7可用 {player} 代表玩家名",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.COMMAND, "请输入命令 (不需要/):");
                })
                .build());
        
        setButton(12, GuiButton.builder(Material.COMMAND_BLOCK_MINECART)
                .name("&f控制台命令 &7(CONSOLE)")
                .lore(Arrays.asList(
                        "&7以控制台身份执行命令",
                        "&7可用 {player} 代表玩家名",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.CONSOLE, "请输入命令:");
                })
                .build());
        
        setButton(13, GuiButton.builder(Material.NOTE_BLOCK)
                .name("&f声音 &7(SOUND)")
                .lore(Arrays.asList(
                        "&7播放声音效果",
                        "&7格式: 声音名称[:音量[:音调]]",
                        "&7例: BLOCK_NOTE_BLOCK_PLING:1.0:1.0",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.SOUND, "请输入声音名称:");
                })
                .build());
        
        setButton(14, GuiButton.builder(Material.ENDER_PEARL)
                .name("&f传送 &7(TELEPORT)")
                .lore(Arrays.asList(
                        "&7传送玩家到指定位置",
                        "&7格式: x y z [世界]",
                        "&7例: 100 64 200 world",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.TELEPORT, "请输入坐标 (x y z [世界]):");
                })
                .build());
        
        setButton(15, GuiButton.builder(Material.ENDER_CHEST)
                .name("&f服务器 &7(SERVER)")
                .lore(Arrays.asList(
                        "&7将玩家传送到其他服务器",
                        "&7需要 BungeeCord/Velocity",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.SERVER, "请输入目标服务器名称:");
                })
                .build());
        
        setButton(16, GuiButton.builder(Material.ARROW)
                .name("&f下一页 &7(NEXT_PAGE)")
                .lore(Arrays.asList(
                        "&7翻到下一页",
                        "&7无需输入值",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    createAction(ActionType.NEXT_PAGE, hologramName);
                    context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加下一页动作！"));
                    guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType));
                })
                .build());
        
        setButton(17, GuiButton.builder(Material.TIPPED_ARROW)
                .name("&f上一页 &7(PREV_PAGE)")
                .lore(Arrays.asList(
                        "&7翻到上一页",
                        "&7无需输入值",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    createAction(ActionType.PREV_PAGE, hologramName);
                    context.getPlayer().sendMessage(ColorUtil.colorize("&a已添加上一页动作！"));
                    guiManager.openGui(context.getPlayer(), new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType));
                })
                .build());
        
        setButton(18, GuiButton.builder(Material.BOOK)
                .name("&f页面跳转 &7(PAGE)")
                .lore(Arrays.asList(
                        "&7跳转到指定页面",
                        "&7输入页码数字",
                        "",
                        "&e点击添加"
                ))
                .onClick(context -> {
                    requestInputAndCreateAction(context.getPlayer(), ActionType.PAGE, "请输入目标页码:");
                })
                .build());
    }
    
    private void requestInputAndCreateAction(Player player, ActionType actionType, String prompt) {
        player.closeInventory();
        
        chatInputManager.requestInput(player, "&a" + prompt, ChatInputManager.InputType.ACTION_VALUE, hologramName, input -> {
            createAction(actionType, input);
            player.sendMessage(ColorUtil.colorize("&a已添加 " + actionType.getName() + " 动作！"));
            guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType));
        });
    }
    
    private void createAction(ActionType actionType, String value) {
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram != null) {
            HologramPage page = hologram.getPage(pageIndex);
            if (page != null) {
                Action action = new Action(actionType, value);
                page.addAction(clickType, action);
                hologram.save();
            }
        }
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
