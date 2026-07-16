package eu.decentsoftware.holograms.api.holograms;

/**
 * DH 兼容层 - 全息图行类型枚举
 * 与 DecentHolograms 的 HologramType 一致
 */
public enum HologramType {

	TEXT,
	ICON,
	HEAD,
	SMALLHEAD,
	BLOCK,
	ENTITY;

	public static HologramType fromWoo(com.oolongho.holograms.hologram.HologramType wooType) {
		if (wooType == null) return TEXT;
		return switch (wooType) {
			case TEXT, NEXT, PREV, UNKNOWN -> TEXT;
			case ICON -> ICON;
			case HEAD -> HEAD;
			case SMALLHEAD -> SMALLHEAD;
			case BLOCK -> BLOCK;
			case ENTITY -> ENTITY;
		};
	}
}
