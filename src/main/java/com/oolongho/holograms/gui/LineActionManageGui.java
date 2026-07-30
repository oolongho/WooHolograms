package com.oolongho.holograms.gui;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行级动作管理 GUI
 * 完全使用 {@link AbstractActionManageGui} 基类的渲染逻辑（4 按钮点击类型布局、
 * COMMAND_BLOCK 图标 + 确认删除流程、ActionTypeSelectGui 添加动作），
 * 仅在标题中携带行号，并通过 {@link #addAction(Player)} 走行级持久化路径。
 */
public class LineActionManageGui extends AbstractActionManageGui {

    private final int lineIndex;

    public LineActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, int lineIndex) {
        this(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex, ClickType.LEFT);
    }

    public LineActionManageGui(WooHolograms plugin, GuiManager guiManager, ChatInputManager chatInputManager,
                               String hologramName, int pageIndex, int lineIndex, ClickType clickType) {
        super("line_action_manage",
                plugin.getMessages().get("gui.title-line-action-manage",
                        "line", String.valueOf(lineIndex + 1)),
                54,
                plugin, guiManager, chatInputManager, hologramName, pageIndex, clickType);
        this.lineIndex = lineIndex;
        render();
    }

    @Override
    protected Map<ClickType, List<Action>> getActions() {
        HologramLine line = getLine();
        if (line == null) return new HashMap<>();
        return line.getActions();
    }

    @Override
    protected void setActions(Map<ClickType, List<Action>> actions) {
    }

    @Override
    protected String getTargetDescription() {
        return plugin.getMessages().getString("gui.line-action-manage.line-desc", "line", String.valueOf(lineIndex + 1));
    }

    @Override
    protected void goBack(Player player) {
        guiManager.openGui(player, new LineEditGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex));
    }

    @Override
    protected void onNullTarget(Player player) {
        setButton(22, GuiButton.builder(Material.BARRIER)
                .name(plugin.getMessages().getString("gui.btn-line-not-exists"))
                .lore(Arrays.asList("", plugin.getMessages().getString("gui.lore-line-deleted"), "", plugin.getMessages().getString("gui.lore-click-back-detail")))
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
        guiManager.openGui(player, new LineActionManageGui(plugin, guiManager, chatInputManager, hologramName, pageIndex, lineIndex, currentClickType));
    }

    @Override
    protected void addAction(Player player) {
        guiManager.openGui(player, new ActionTypeSelectGui(
                plugin, guiManager, chatInputManager, hologramName,
                action -> {
                    HologramLine line = getLine();
                    if (line == null) return;
                    action.setClickType(currentClickType);
                    line.addAction(currentClickType, action);
                    Hologram hologram = getHologram();
                    if (hologram != null) {
                        hologram.save();
                    }
                    reopenGui(player);
                },
                () -> reopenGui(player)
        ));
    }

    private HologramLine getLine() {
        Hologram hologram = getHologram();
        if (hologram == null) return null;
        HologramPage page = hologram.getPage(pageIndex);
        if (page == null || lineIndex >= page.size()) return null;
        return page.getLine(lineIndex);
    }
}
