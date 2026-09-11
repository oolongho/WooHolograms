package eu.decentsoftware.holograms.api.actions;

/**
 * DH 兼容层 - 点击类型枚举
 * 与 DecentHolograms 的 ClickType 完全一致
 */
public enum ClickType {

	LEFT,
	RIGHT,
	SHIFT_LEFT,
	SHIFT_RIGHT,
	ANY;

	public com.oolongho.holograms.api.action.ClickType toWoo() {
		return com.oolongho.holograms.api.action.ClickType.valueOf(this.name());
	}

	public static ClickType fromWoo(com.oolongho.holograms.api.action.ClickType wooClickType) {
		if (wooClickType == null) return null;
		return valueOf(wooClickType.name());
	}
}
