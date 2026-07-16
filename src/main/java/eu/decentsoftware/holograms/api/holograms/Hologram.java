package eu.decentsoftware.holograms.api.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * DH 兼容层 - Hologram 包装类
 * 委托到 WooHolograms 的 Hologram
 */
public class Hologram {

	private final com.oolongho.holograms.hologram.Hologram handle;

	public Hologram(com.oolongho.holograms.hologram.Hologram handle) {
		this.handle = handle;
	}

	// ===== 基本信息 =====

	public String getName() {
		return handle.getName();
	}

	public Location getLocation() {
		return handle.getLocation();
	}

	public void setLocation(Location location) {
		handle.setLocation(location);
	}

	public void teleport(Location location) {
		handle.teleport(location);
	}

	public void teleport(Location location, boolean updateViewers) {
		handle.teleport(location, updateViewers);
	}

	// ===== 启用/禁用 =====

	public boolean isEnabled() {
		return handle.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		handle.setEnabled(enabled);
	}

	public void enable() {
		handle.enable();
	}

	public void disable() {
		handle.disable();
	}

	// ===== 显示范围 =====

	public double getDisplayRange() {
		return handle.getDisplayRange();
	}

	public void setDisplayRange(double displayRange) {
		handle.setDisplayRange(displayRange);
	}

	public double getUpdateRange() {
		return handle.getUpdateRange();
	}

	public void setUpdateRange(double updateRange) {
		handle.setUpdateRange(updateRange);
	}

	public int getUpdateInterval() {
		return handle.getUpdateInterval();
	}

	public void setUpdateInterval(int updateInterval) {
		handle.setUpdateInterval(updateInterval);
	}

	// ===== 朝向 =====

	public float getFacing() {
		return handle.getFacing();
	}

	public void setFacing(float facing) {
		handle.setFacing(facing);
	}

	// ===== DH 特有：downOrigin（WooHolograms 不支持，降级处理） =====

	public boolean isDownOrigin() {
		return false;
	}

	public void setDownOrigin(boolean downOrigin) {
		// no-op: WooHolograms 不支持此功能
	}

	// ===== 行高 =====

	public double getLineHeight() {
		return handle.getLineHeight();
	}

	public void setLineHeight(double lineHeight) {
		handle.setLineHeight(lineHeight);
	}

	// ===== 权限 =====

	public String getPermission() {
		return handle.getPermission();
	}

	public void setPermission(String permission) {
		handle.setPermission(permission);
	}

	// ===== 页面管理 =====

	public int getPageCount() {
		return handle.getPageCount();
	}

	public HologramPage getPage(int index) {
		com.oolongho.holograms.hologram.HologramPage page = handle.getPage(index);
		return page != null ? new HologramPage(page) : null;
	}

	public HologramPage getPage(Player player) {
		com.oolongho.holograms.hologram.HologramPage page = handle.getPage(player);
		return page != null ? new HologramPage(page) : null;
	}

	public List<HologramPage> getPages() {
		List<HologramPage> result = new ArrayList<>();
		for (com.oolongho.holograms.hologram.HologramPage page : handle.getPages()) {
			result.add(new HologramPage(page));
		}
		return result;
	}

	public HologramPage addPage() {
		return new HologramPage(handle.addPage());
	}

	public HologramPage addPage(List<String> lines) {
		return new HologramPage(handle.addPage(lines));
	}

	public HologramPage insertPage(int index) {
		com.oolongho.holograms.hologram.HologramPage page = handle.insertPage(index);
		return page != null ? new HologramPage(page) : null;
	}

	public boolean removePage(int index) {
		return handle.removePage(index) != null;
	}

	public boolean swapPages(int index1, int index2) {
		return handle.swapPages(index1, index2);
	}

	// ===== 页面切换 =====

	public boolean nextPage(Player player) {
		return handle.nextPage(player);
	}

	public boolean previousPage(Player player) {
		return handle.previousPage(player);
	}

	// ===== 显示/隐藏 =====

	public boolean show(Player player) {
		return handle.show(player);
	}

	public boolean show(Player player, int page) {
		return handle.show(player, page);
	}

	public void showAll() {
		handle.showAll();
	}

	public void hide(Player player) {
		handle.hide(player);
	}

	public void hideAll() {
		handle.hideAll();
	}

	// ===== 观看者 =====

	public Set<UUID> getViewers() {
		return handle.getViewers();
	}

	public boolean isVisible(Player player) {
		return handle.isVisible(player);
	}

	public int getPlayerPage(Player player) {
		return handle.getPlayerPage(player);
	}

	// ===== 标志 =====

	public Set<EnumFlag> getFlags() {
		Set<EnumFlag> result = EnumSet.noneOf(EnumFlag.class);
		for (com.oolongho.holograms.hologram.EnumFlag flag : handle.getFlags()) {
			EnumFlag dhFlag = EnumFlag.fromWoo(flag);
			if (dhFlag != null) {
				result.add(dhFlag);
			}
		}
		return result;
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

	// ===== 持久化 =====

	public boolean isSaveToFile() {
		return handle.isSaveToFile();
	}

	public void setSaveToFile(boolean saveToFile) {
		handle.setSaveToFile(saveToFile);
	}

	public void save() {
		handle.save();
	}

	public void delete() {
		handle.delete();
	}

	public void destroy() {
		handle.destroy();
	}

	// ===== 克隆 =====

	public Hologram clone(String name, Location location, boolean temp) {
		com.oolongho.holograms.hologram.Hologram cloned = handle.clone(name, location, temp);
		return cloned != null ? new Hologram(cloned) : null;
	}

	// ===== Display Entity 属性 =====

	public float getScaleX() {
		return handle.getScaleX();
	}

	public float getScaleY() {
		return handle.getScaleY();
	}

	public float getScaleZ() {
		return handle.getScaleZ();
	}

	public void setScale(float x, float y, float z) {
		handle.setScale(x, y, z);
	}

	public double getTranslationX() {
		return handle.getTranslationX();
	}

	public double getTranslationY() {
		return handle.getTranslationY();
	}

	public double getTranslationZ() {
		return handle.getTranslationZ();
	}

	public void setTranslation(double x, double y, double z) {
		handle.setTranslation(x, y, z);
	}

	public float getShadowRadius() {
		return handle.getShadowRadius();
	}

	public void setShadowRadius(float shadowRadius) {
		handle.setShadowRadius(shadowRadius);
	}

	public float getShadowStrength() {
		return handle.getShadowStrength();
	}

	public void setShadowStrength(float shadowStrength) {
		handle.setShadowStrength(shadowStrength);
	}

	public int getGlowColor() {
		return handle.getGlowColor();
	}

	public void setGlowColor(int glowColor) {
		handle.setGlowColor(glowColor);
	}

	public boolean isChromaBackground() {
		return handle.isChromaBackground();
	}

	public void setChromaBackground(boolean chromaBackground) {
		handle.setChromaBackground(chromaBackground);
	}

	public boolean isChromaGlow() {
		return handle.isChromaGlow();
	}

	public void setChromaGlow(boolean chromaGlow) {
		handle.setChromaGlow(chromaGlow);
	}

	// ===== 内部访问 =====

	public com.oolongho.holograms.hologram.Hologram getHandle() {
		return handle;
	}
}
