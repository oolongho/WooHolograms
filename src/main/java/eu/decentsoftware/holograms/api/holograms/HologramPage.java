package eu.decentsoftware.holograms.api.holograms;

import eu.decentsoftware.holograms.api.actions.ClickType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DH 兼容层 - HologramPage 包装类
 * 委托到 WooHolograms 的 HologramPage
 */
public class HologramPage {

	private final com.oolongho.holograms.hologram.HologramPage handle;

	public HologramPage(com.oolongho.holograms.hologram.HologramPage handle) {
		this.handle = handle;
	}

	public int getIndex() {
		return handle.getIndex();
	}

	public Hologram getParent() {
		return new Hologram(handle.getParent());
	}

	public double getHeight() {
		return handle.getHeight();
	}

	public Location getCenter() {
		return handle.getCenter();
	}

	public int size() {
		return handle.size();
	}

	public List<HologramLine> getLines() {
		List<HologramLine> result = new ArrayList<>();
		for (com.oolongho.holograms.hologram.HologramLine line : handle.getLines()) {
			result.add(new HologramLine(line));
		}
		return result;
	}

	public HologramLine getLine(int index) {
		com.oolongho.holograms.hologram.HologramLine line = handle.getLine(index);
		return line != null ? new HologramLine(line) : null;
	}

	public HologramLine addLine(String content) {
		com.oolongho.holograms.hologram.HologramLine line = handle.addLine(content);
		return line != null ? new HologramLine(line) : null;
	}

	public boolean insertLine(int index, String content) {
		return handle.insertLine(index, content);
	}

	public boolean setLine(int index, String content) {
		return handle.setLine(index, content);
	}

	public HologramLine removeLine(int index) {
		com.oolongho.holograms.hologram.HologramLine line = handle.removeLine(index);
		return line != null ? new HologramLine(line) : null;
	}

	public void clearLines() {
		handle.clearLines();
	}

	public boolean isClickable() {
		return handle.isClickable();
	}

	public boolean hasActions() {
		return handle.hasActions();
	}

	public void addAction(ClickType clickType, com.oolongho.holograms.action.Action action) {
		handle.addAction(clickType.toWoo(), action);
	}

	public List<com.oolongho.holograms.action.Action> getActions(ClickType clickType) {
		return handle.getActions(clickType.toWoo());
	}

	public void removeAction(ClickType clickType, int index) {
		handle.removeAction(clickType.toWoo(), index);
	}

	public void executeActions(Player player, ClickType clickType) {
		handle.executeActions(player, clickType.toWoo());
	}

	public boolean hasFlag(EnumFlag flag) {
		return handle.hasFlag(flag.toWoo());
	}

	public void addFlags(EnumFlag... flags) {
		if (flags == null) return;
		com.oolongho.holograms.hologram.EnumFlag[] wooFlags =
				new com.oolongho.holograms.hologram.EnumFlag[flags.length];
		for (int i = 0; i < flags.length; i++) {
			wooFlags[i] = flags[i].toWoo();
		}
		handle.addFlags(wooFlags);
	}

	public void removeFlag(EnumFlag flag) {
		handle.removeFlag(flag.toWoo());
	}

	public com.oolongho.holograms.hologram.HologramPage getHandle() {
		return handle;
	}
}
