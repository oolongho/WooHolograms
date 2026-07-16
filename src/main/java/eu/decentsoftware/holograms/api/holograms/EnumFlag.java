package eu.decentsoftware.holograms.api.holograms;

/**
 * DH 兼容层 - 全息图标志枚举
 * 与 DecentHolograms 的 EnumFlag 完全一致
 */
public enum EnumFlag {

	DISABLE_PLACEHOLDERS,
	DISABLE_UPDATING,
	DISABLE_ANIMATIONS,
	DISABLE_ACTIONS,
	ALWAYS_FACE_PLAYER,
	CLICKABLE;

	public com.oolongho.holograms.hologram.EnumFlag toWoo() {
		return com.oolongho.holograms.hologram.EnumFlag.valueOf(this.name());
	}

	public static EnumFlag fromWoo(com.oolongho.holograms.hologram.EnumFlag wooFlag) {
		if (wooFlag == null) return null;
		return valueOf(wooFlag.name());
	}
}
