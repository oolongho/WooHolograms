package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;

import java.util.Arrays;

public class HelpGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;

    public HelpGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager) {
        super("help", ColorUtil.colorize("&8帮助手册"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        
        render();
    }

    private void render() {
        clearButtons();
        
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回上一页", "", "&e点击返回"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());
        
        fillFirstRow();
        
        setButton(9, GuiButton.builder(Material.PAPER)
                .name("&e行类型格式")
                .lore(Arrays.asList(
                        "&7在行内容中使用以下格式：",
                        "",
                        "&f#ICON:<材质>",
                        "&7显示物品图标",
                        "&7例: #ICON:DIAMOND_SWORD",
                        "",
                        "&f#HEAD:<玩家名>",
                        "&7显示大头颅（0.6格高）",
                        "&7例: #HEAD:Notch",
                        "",
                        "&f#SMALLHEAD:<玩家名>",
                        "&7显示小头颅（0.4格高）",
                        "&7例: #SMALLHEAD:Notch"
                ))
                .build());
        
        setButton(10, GuiButton.builder(Material.PLAYER_HEAD)
                .name("&e头颅类型")
                .lore(Arrays.asList(
                        "&7头颅支持以下格式：",
                        "",
                        "&f#HEAD:PLAYER_HEAD (玩家名)",
                        "&7显示玩家头颅",
                        "&7例: #HEAD:Notch",
                        "",
                        "&f#HEAD:PLAYER_HEAD ({player})",
                        "&7显示查看者的头颅",
                        "",
                        "&f#HEAD:URL (Base64)",
                        "&7使用 URL 材质",
                        "",
                        "&f#HEAD:HDB (ID)",
                        "&7使用 HeadDatabase"
                ))
                .build());
        
        setButton(11, GuiButton.builder(Material.ZOMBIE_HEAD)
                .name("&e实体和翻页")
                .lore(Arrays.asList(
                        "&f#ENTITY:<类型>",
                        "&7显示实体模型",
                        "&7例: #ENTITY:ZOMBIE",
                        "",
                        "&f#NEXT",
                        "&7下一页按钮",
                        "&7点击自动翻到下一页",
                        "",
                        "&f#PREV",
                        "&7上一页按钮",
                        "&7点击自动翻到上一页"
                ))
                .build());
        
        setButton(12, GuiButton.builder(Material.OAK_SIGN)
                .name("&e普通文本")
                .lore(Arrays.asList(
                        "&7不以 # 开头的内容",
                        "&7将显示为普通文本",
                        "",
                        "&7支持颜色代码和变量"
                ))
                .build());
        
        setButton(13, GuiButton.builder(Material.NAME_TAG)
                .name("&e内置变量")
                .lore(Arrays.asList(
                        "&7在内容中使用变量：",
                        "",
                        "&f{player} &7- 玩家名称",
                        "&f{player_uuid} &7- 玩家 UUID",
                        "&f{player_displayname} &7- 显示名称",
                        "&f{player_x} &7- X 坐标",
                        "&f{player_y} &7- Y 坐标",
                        "&f{player_z} &7- Z 坐标",
                        "&f{player_world} &7- 所在世界",
                        "&f{player_health} &7- 生命值",
                        "&f{player_level} &7- 等级"
                ))
                .build());
        
        setButton(14, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&e物品参数")
                .lore(Arrays.asList(
                        "&7在 #ICON 后添加参数：",
                        "",
                        "&fcustom-model-data:<值>",
                        "&7自定义模型数据",
                        "&7例: #ICON:DIAMOND_SWORD",
                        "&7    custom-model-data:10000",
                        "",
                        "&fcolor:<RGB>",
                        "&7皮革颜色",
                        "&7例: color:FF0000",
                        "",
                        "&fname:<名称>",
                        "&7自定义名称",
                        "&7例: name:&6传说之剑"
                ))
                .build());
        
        setButton(15, GuiButton.builder(Material.ENCHANTED_BOOK)
                .name("&e更多物品参数")
                .lore(Arrays.asList(
                        "&flore:<描述>",
                        "&7物品描述",
                        "",
                        "&fglow",
                        "&7发光效果（无附魔光效）",
                        "",
                        "&funbreakable",
                        "&7无法破坏",
                        "",
                        "&7简写格式：",
                        "&fcmd:<值> &7= custom-model-data"
                ))
                .build());
        
        setButton(16, GuiButton.builder(Material.KNOWLEDGE_BOOK)
                .name("&eNBT 格式")
                .lore(Arrays.asList(
                        "&7也支持原生 NBT 格式：",
                        "",
                        "&f{CustomModelData:10000}",
                        "&f{display:{color:3847130}}",
                        "&f{Enchantments:[{id:\"sharpness\",lvl:5}]}",
                        "",
                        "&7两种格式可混用：",
                        "#ICON:DIAMOND_SWORD",
                        "custom-model-data:10000",
                        "{Enchantments:[{id:\"sharpness\",lvl:5}]}"
                ))
                .build());
        
        setButton(18, GuiButton.builder(Material.COMMAND_BLOCK)
                .name("&e动作类型")
                .lore(Arrays.asList(
                        "&7为行添加点击动作：",
                        "",
                        "&fCOMMAND:<命令>",
                        "&7以玩家身份执行命令",
                        "&7例: COMMAND:spawn",
                        "",
                        "&fCONSOLE:<命令>",
                        "&7以控制台身份执行命令",
                        "",
                        "&fMESSAGE:<消息>",
                        "&7发送消息给玩家"
                ))
                .build());
        
        setButton(19, GuiButton.builder(Material.NOTE_BLOCK)
                .name("&e更多动作")
                .lore(Arrays.asList(
                        "&fSOUND:<音效>",
                        "&7播放音效",
                        "&7例: SOUND:ENTITY_PLAYER_LEVELUP",
                        "",
                        "&fTELEPORT:<坐标>",
                        "&7传送玩家",
                        "&7例: TELEPORT:world,100,64,200",
                        "",
                        "&fSERVER:<服务器>",
                        "&7连接到其他服务器 (BungeeCord)"
                ))
                .build());
        
        setButton(20, GuiButton.builder(Material.ARROW)
                .name("&e翻页动作")
                .lore(Arrays.asList(
                        "&fNEXT_PAGE",
                        "&7翻到下一页",
                        "",
                        "&fPREV_PAGE",
                        "&7翻到上一页",
                        "",
                        "&fPAGE:<页码>",
                        "&7跳转到指定页",
                        "&7例: PAGE:3",
                        "",
                        "&7或直接使用 #NEXT / #PREV"
                ))
                .build());
        
        setButton(21, GuiButton.builder(Material.STONE_BUTTON)
                .name("&e点击类型")
                .lore(Arrays.asList(
                        "&7可为不同点击设置不同动作：",
                        "",
                        "&fANY &7- 任意点击",
                        "&fLEFT &7- 左键点击",
                        "&fRIGHT &7- 右键点击",
                        "&fSHIFT_LEFT &7- Shift+左键",
                        "&fSHIFT_RIGHT &7- Shift+右键",
                        "",
                        "&7在动作管理 GUI 中切换类型"
                ))
                .build());
        
        setButton(22, GuiButton.builder(Material.REDSTONE_TORCH)
                .name("&e变量支持")
                .lore(Arrays.asList(
                        "&7动作中支持变量：",
                        "",
                        "&f{player} &7- 玩家名称",
                        "",
                        "&7例: MESSAGE:&a你好, {player}!",
                        "&7例: COMMAND:tp {player} 100 64 100"
                ))
                .build());
        
        setButton(23, GuiButton.builder(Material.BLAZE_POWDER)
                .name("&e动画效果")
                .lore(Arrays.asList(
                        "&7使用动画让文本动起来：",
                        "",
                        "&f<#ANIM:wave>文本</#ANIM>",
                        "&7波浪动画（默认黄白）",
                        "",
                        "&f<#ANIM:wave:&c,&b>文本</#ANIM>",
                        "&7自定义颜色波浪",
                        "",
                        "&f<#ANIM:typewriter>文本</#ANIM>",
                        "&7打字机效果",
                        "",
                        "&f<#ANIM:blink>文本</#ANIM>",
                        "&7闪烁效果",
                        "",
                        "&f<#ANIM:scroll>文本</#ANIM>",
                        "&7滚动效果",
                        "",
                        "&f<#ANIM:gradient:red,blue>文本</#ANIM>",
                        "&7渐变色效果"
                ))
                .build());
        
        setButton(24, GuiButton.builder(Material.GLOW_INK_SAC)
                .name("&e全息图属性")
                .lore(Arrays.asList(
                        "&7在详情 GUI 中可设置：",
                        "",
                        "&f朝向模式:",
                        "&7- 固定角度 (自定义朝向)",
                        "&7- 水平/垂直跟随",
                        "&7- 完全跟随玩家",
                        "",
                        "&f其他属性:",
                        "&7- 显示范围、更新间隔",
                        "&7- 双面显示、权限控制"
                ))
                .build());
        
        setButton(25, GuiButton.builder(Material.EMERALD)
                .name("&e快捷命令")
                .lore(Arrays.asList(
                        "&7常用命令：",
                        "",
                        "&f/wh create <名称>",
                        "&7创建全息图",
                        "",
                        "&f/wh edit <名称>",
                        "&7打开编辑 GUI",
                        "",
                        "&f/wh delete <名称>",
                        "&7删除全息图",
                        "",
                        "&f/wh reload",
                        "&7重载配置"
                ))
                .build());
        
        setButton(26, GuiButton.builder(Material.BOOKSHELF)
                .name("&ePlaceholderAPI")
                .lore(Arrays.asList(
                        "&7支持 PAPI 占位符：",
                        "",
                        "&f%player_name%",
                        "&f%player_health%",
                        "&f%server_online%",
                        "",
                        "&7需安装 PlaceholderAPI"
                ))
                .build());
        
        fillLastRow();
    }
    
    private void fillFirstRow() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 1; i < 9; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
    
    private void fillLastRow() {
        GuiButton background = GuiButton.builder(Material.LIME_STAINED_GLASS_PANE)
                .name(" ")
                .build();
        
        for (int i = 45; i < 54; i++) {
            if (getButton(i) == null) {
                setButton(i, background);
            }
        }
    }
}
