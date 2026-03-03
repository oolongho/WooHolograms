package com.oolonghoo.holograms.gui;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Material;

import java.util.Arrays;

/**
 * 帮助 GUI
 * 显示插件使用说明
 *
 * @author oolongho
 */
public class HelpGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;
    private int page;

    public HelpGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager, int page) {
        super("help", ColorUtil.colorize("&8帮助手册"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        this.page = page;
        
        render();
    }

    private void render() {
        clearButtons();
        
        // 返回按钮
        setButton(0, GuiButton.builder(Material.BOOK)
                .name("&f返回")
                .lore(Arrays.asList("&7返回上一页", "", "&e点击返回"))
                .onClick(context -> {
                    if (page > 0) {
                        guiManager.openGui(context.getPlayer(), new HelpGui(plugin, guiManager, chatInputManager, page - 1));
                    }
                })
                .build());
        
        // 页码显示
        setButton(4, GuiButton.builder(Material.PAPER)
                .name("&f第 " + (page + 1) + " 页")
                .build());
        
        if (page == 0) {
            // 第一页：行类型说明
            renderLineTypesHelp();
        } else if (page == 1) {
            // 第二页：变量和参数说明
            renderVariablesHelp();
        } else if (page == 2) {
            // 第三页：动作说明
            renderActionsHelp();
        }
        
        // 下一页按钮
        if (page < 2) {
            setButton(53, GuiButton.builder(Material.ARROW)
                    .name("&f下一页")
                    .lore(Arrays.asList("&7查看更多帮助", "", "&e点击翻页"))
                    .onClick(context -> {
                        guiManager.openGui(context.getPlayer(), new HelpGui(plugin, guiManager, chatInputManager, page + 1));
                    })
                    .build());
        }
    }
    
    private void renderLineTypesHelp() {
        setButton(9, GuiButton.builder(Material.PAPER)
                .name("&e行类型格式")
                .lore(Arrays.asList(
                        "&7在行内容中使用以下格式：",
                        "",
                        "&f#ICON:<材质>",
                        "&7显示物品图标",
                        "&7例: #ICON:DIAMOND_SWORD",
                        "",
                        "&f#HEAD:<类型>",
                        "&7显示大头颅（0.6格高）",
                        "&7例: #HEAD:PLAYER_HEAD (玩家名)",
                        "",
                        "&f#SMALLHEAD:<类型>",
                        "&7显示小头颅（0.4格高）",
                        "&7例: #SMALLHEAD:PLAYER_HEAD (玩家名)"
                ))
                .build());
        
        setButton(18, GuiButton.builder(Material.PLAYER_HEAD)
                .name("&e头颅类型")
                .lore(Arrays.asList(
                        "&7头颅支持以下格式：",
                        "",
                        "&fPLAYER_HEAD (玩家名)",
                        "&7显示玩家头颅",
                        "&7例: #HEAD:PLAYER_HEAD (Notch)",
                        "",
                        "&fPLAYER_HEAD ({player})",
                        "&7显示查看者的头颅",
                        "",
                        "&fURL (Base64)",
                        "&7使用 URL 材质",
                        "&fHDB (ID)",
                        "&7使用 HeadDatabase"
                ))
                .build());
        
        setButton(27, GuiButton.builder(Material.ZOMBIE_HEAD)
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
        
        setButton(36, GuiButton.builder(Material.OAK_SIGN)
                .name("&e普通文本")
                .lore(Arrays.asList(
                        "&7不以 # 开头的内容",
                        "&7将显示为普通文本",
                        "",
                        "&7支持颜色代码:",
                        "&f&0-&9, &a-&f &7颜色",
                        "&f&l &7粗体",
                        "&f&o &7斜体",
                        "&f&n &7下划线",
                        "&f&k &7随机字符",
                        "",
                        "&7例: &a欢迎&a&l{player}!"
                ))
                .build());
    }
    
    private void renderVariablesHelp() {
        setButton(9, GuiButton.builder(Material.NAME_TAG)
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
        
        setButton(18, GuiButton.builder(Material.COMMAND_BLOCK)
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
        
        setButton(27, GuiButton.builder(Material.ENCHANTED_BOOK)
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
        
        setButton(36, GuiButton.builder(Material.KNOWLEDGE_BOOK)
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
    }
    
    private void renderActionsHelp() {
        setButton(9, GuiButton.builder(Material.COMMAND_BLOCK)
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
        
        setButton(18, GuiButton.builder(Material.NOTE_BLOCK)
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
                        "&fCONNECT:<服务器>",
                        "&7连接到其他服务器 (BungeeCord)"
                ))
                .build());
        
        setButton(27, GuiButton.builder(Material.ARROW)
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
        
        setButton(36, GuiButton.builder(Material.STONE_BUTTON)
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
        
        setButton(45, GuiButton.builder(Material.REDSTONE_TORCH)
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
    }
}
