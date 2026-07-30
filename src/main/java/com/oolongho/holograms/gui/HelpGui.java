package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import org.bukkit.Material;

import java.util.Arrays;

public class HelpGui extends GuiScreen {

    private final WooHolograms plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;

    public HelpGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager) {
        super("help", plugin.getMessages().get("gui.title-help"), 54);
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
        
        render();
    }

    private void render() {
        clearButtons();
        
        setButton(0, GuiButton.builder(Material.BOOK)
                .name(plugin.getMessages().getString("gui.help.back.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.back.lore"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramListGui(plugin, guiManager, chatInputManager, 0));
                })
                .build());

        fillFirstRow();

        setButton(9, GuiButton.builder(Material.PAPER)
                .name(plugin.getMessages().getString("gui.help.line-format.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.line-format.lore"))
                .build());

        setButton(10, GuiButton.builder(Material.PLAYER_HEAD)
                .name(plugin.getMessages().getString("gui.help.head-type.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.head-type.lore"))
                .build());

        setButton(11, GuiButton.builder(Material.ZOMBIE_HEAD)
                .name(plugin.getMessages().getString("gui.help.entity-display.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.entity-display.lore"))
                .build());

        setButton(12, GuiButton.builder(Material.OAK_SIGN)
                .name(plugin.getMessages().getString("gui.help.text.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.text.lore"))
                .build());

        setButton(13, GuiButton.builder(Material.NAME_TAG)
                .name(plugin.getMessages().getString("gui.help.variables.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.variables.lore"))
                .build());

        setButton(14, GuiButton.builder(Material.COMMAND_BLOCK)
                .name(plugin.getMessages().getString("gui.help.item-params.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.item-params.lore"))
                .build());

        setButton(15, GuiButton.builder(Material.ENCHANTED_BOOK)
                .name(plugin.getMessages().getString("gui.help.item-params-more.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.item-params-more.lore"))
                .build());

        setButton(16, GuiButton.builder(Material.KNOWLEDGE_BOOK)
                .name(plugin.getMessages().getString("gui.help.nbt.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.nbt.lore"))
                .build());

        setButton(18, GuiButton.builder(Material.COMMAND_BLOCK)
                .name(plugin.getMessages().getString("gui.help.action-types.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.action-types.lore"))
                .build());

        setButton(19, GuiButton.builder(Material.NOTE_BLOCK)
                .name(plugin.getMessages().getString("gui.help.actions-more.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.actions-more.lore"))
                .build());

        setButton(20, GuiButton.builder(Material.ARROW)
                .name(plugin.getMessages().getString("gui.help.page-actions.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.page-actions.lore"))
                .build());

        setButton(21, GuiButton.builder(Material.STONE_BUTTON)
                .name(plugin.getMessages().getString("gui.help.click-types.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.click-types.lore"))
                .build());

        setButton(22, GuiButton.builder(Material.REDSTONE_TORCH)
                .name(plugin.getMessages().getString("gui.help.action-variables.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.action-variables.lore"))
                .build());

        setButton(23, GuiButton.builder(Material.BLAZE_POWDER)
                .name(plugin.getMessages().getString("gui.help.animation.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.animation.lore"))
                .build());

        setButton(24, GuiButton.builder(Material.GLOW_INK_SAC)
                .name(plugin.getMessages().getString("gui.help.properties.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.properties.lore"))
                .build());

        setButton(25, GuiButton.builder(Material.EMERALD)
                .name(plugin.getMessages().getString("gui.help.quick-commands.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.quick-commands.lore"))
                .build());

        setButton(26, GuiButton.builder(Material.BOOKSHELF)
                .name(plugin.getMessages().getString("gui.help.papi.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.papi.lore"))
                .build());

        setButton(27, GuiButton.builder(Material.ENDER_EYE)
                .name(plugin.getMessages().getString("gui.help.display-props.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.display-props.lore"))
                .build());

        setButton(28, GuiButton.builder(Material.CYAN_DYE)
                .name(plugin.getMessages().getString("gui.help.rainbow.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.rainbow.lore"))
                .build());

        setButton(30, GuiButton.builder(Material.GRASS_BLOCK)
                .name(plugin.getMessages().getString("gui.help.block-type.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.block-type.lore"))
                .build());

        setButton(31, GuiButton.builder(Material.HOPPER)
                .name(plugin.getMessages().getString("gui.help.data-import.name"))
                .lore(plugin.getMessages().getLangConfig().getStringList("gui.help.data-import.lore"))
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
