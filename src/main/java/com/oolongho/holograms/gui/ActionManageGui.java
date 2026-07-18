package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManageGui extends AbstractActionManageGui {

    public ActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                          String hologramName, int pageIndex) {
        this(plugin, guiManager, chatInputManager, hologramName, pageIndex, ClickType.LEFT);
    }

    public ActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                          String hologramName, int pageIndex, ClickType clickType) {
        super("action_manage", "&8动作管理: " + hologramName + " P" + (pageIndex + 1), 54,
                plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType);
        render();
    }

    @Override
    protected Map<ClickType, List<Action>> getActions() {
        Hologram hologram = getHologram();
        if (hologram == null) return new HashMap<>();

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null) return new HashMap<>();

        return page.getActions();
    }

    @Override
    protected void setActions(Map<ClickType, List<Action>> actions) {
    }

    @Override
    protected String getTargetDescription() {
        return "第 " + (pageIndex + 1) + " 页";
    }

    @Override
    protected void goBack(Player player) {
        guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, pageIndex));
    }

    @Override
    protected void onNullTarget(Player player) {
        setButton(22, GuiButton.builder(org.bukkit.Material.BARRIER)
                .name("&f页面不存在")
                .lore(java.util.Arrays.asList("", "&7该页面已被删除", "", "&e点击返回详情"))
                .onClick(context -> {
                    guiManager.openGui(context.getPlayer(), new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
                })
                .build());
    }

    @Override
    protected boolean hasTarget() {
        Hologram hologram = getHologram();
        return hologram != null && hologram.getPage(pageIndex) != null;
    }

    @Override
    protected void removeAction(int actionIndex) {
        Hologram hologram = getHologram();
        if (hologram == null) return;

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null) return;

        page.removeAction(currentClickType, actionIndex);
    }

    @Override
    protected void updateAction(int actionIndex, Action newAction) {
        Hologram hologram = getHologram();
        if (hologram == null) return;

        HologramPage page = hologram.getPage(pageIndex);
        if (page == null) return;

        page.setAction(currentClickType, actionIndex, newAction);
    }

    @Override
    protected void reopenGui(Player player) {
        guiManager.openGui(player, new ActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, currentClickType));
    }

    @Override
    protected void addAction(Player player) {
        guiManager.openGui(player, new ActionTypeSelectGui(
                plugin, guiManager, chatInputManager, hologramName,
                action -> {
                    Hologram hologram = getHologram();
                    if (hologram == null) return;
                    HologramPage page = hologram.getPage(pageIndex);
                    if (page == null) return;
                    action.setClickType(currentClickType);
                    page.addAction(currentClickType, action);
                    hologram.save();
                    reopenGui(player);
                },
                () -> reopenGui(player)
        ));
    }
}
