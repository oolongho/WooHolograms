package eu.decentsoftware.holograms.api.holograms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * DH 兼容层 - HologramLine 包装类
 * 委托到 WooHolograms 的 HologramLine
 */
public class HologramLine {

	private final com.oolongho.holograms.hologram.HologramLine handle;

	public HologramLine(com.oolongho.holograms.hologram.HologramLine handle) {
		this.handle = handle;
	}

	public String getContent() {
		return handle.getContent();
	}

	public void setContent(String content) {
		handle.setContent(content);
	}

	public HologramType getType() {
		return HologramType.fromWoo(handle.getType());
	}

	public double getHeight() {
		return handle.getHeight();
	}

	public void setHeight(double height) {
		handle.setHeight(height);
	}

	public double getOffsetX() {
		return handle.getOffsetX();
	}

	public void setOffsetX(double offsetX) {
		handle.setOffsetX(offsetX);
	}

	public double getOffsetY() {
		return handle.getOffsetY();
	}

	public void setOffsetY(double offsetY) {
		handle.setOffsetY(offsetY);
	}

	public double getOffsetZ() {
		return handle.getOffsetZ();
	}

	public void setOffsetZ(double offsetZ) {
		handle.setOffsetZ(offsetZ);
	}

	public float getFacing() {
		return handle.getFacing();
	}

	public void setFacing(float facing) {
		handle.setFacing(facing);
	}

	public String getPermission() {
		return handle.getPermission();
	}

	public void setPermission(String permission) {
		handle.setPermission(permission);
	}

	public void show(Player... players) {
		handle.show(players);
	}

	public void hide(Player... players) {
		handle.hide(players);
	}

	public void update(Player... players) {
		handle.update(players);
	}

	public void destroy() {
		handle.destroy();
	}

	public boolean isEnabled() {
		return handle.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		handle.setEnabled(enabled);
	}

	public Location getLocation() {
		return handle.getLocation();
	}

	public Set<UUID> getViewers() {
		return handle.getViewers();
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

	// ===== Display Entity 属性 =====

	public Float getScaleX() {
		return handle.getScaleX();
	}

	public Float getScaleY() {
		return handle.getScaleY();
	}

	public Float getScaleZ() {
		return handle.getScaleZ();
	}

	public void setScale(Float x, Float y, Float z) {
		handle.setScale(x, y, z);
	}

	public Double getTranslationX() {
		return handle.getTranslationX();
	}

	public Double getTranslationY() {
		return handle.getTranslationY();
	}

	public Double getTranslationZ() {
		return handle.getTranslationZ();
	}

	public void setTranslation(Double x, Double y, Double z) {
		handle.setTranslation(x, y, z);
	}

	public Float getShadowRadius() {
		return handle.getShadowRadius();
	}

	public void setShadowRadius(Float shadowRadius) {
		handle.setShadowRadius(shadowRadius);
	}

	public Float getShadowStrength() {
		return handle.getShadowStrength();
	}

	public void setShadowStrength(Float shadowStrength) {
		handle.setShadowStrength(shadowStrength);
	}

	public Integer getGlowColor() {
		return handle.getGlowColor();
	}

	public void setGlowColor(Integer glowColor) {
		handle.setGlowColor(glowColor);
	}

	public Boolean getChromaBackground() {
		return handle.getChromaBackground();
	}

	public boolean isChromaBackground() {
		return handle.isChromaBackground();
	}

	public void setChromaBackground(Boolean chromaBackground) {
		handle.setChromaBackground(chromaBackground);
	}

	public Boolean getChromaGlow() {
		return handle.getChromaGlow();
	}

	public boolean isChromaGlow() {
		return handle.isChromaGlow();
	}

	public void setChromaGlow(Boolean chromaGlow) {
		handle.setChromaGlow(chromaGlow);
	}

	public com.oolongho.holograms.hologram.HologramLine getHandle() {
		return handle;
	}
}
