package eu.decentsoftware.holograms.api.utils;

import org.bukkit.Location;

/**
 * DH 兼容层 - 不绑定世界的 3D 位置
 * 委托到 WooHolograms 的 DecentPosition
 */
public class DecentPosition {

	private final com.oolonghoo.holograms.nms.util.HologramPosition handle;

	public DecentPosition(double x, double y, double z) {
		this.handle = new com.oolonghoo.holograms.nms.util.HologramPosition(x, y, z);
	}

	public DecentPosition(double x, double y, double z, float yaw, float pitch) {
		this.handle = new com.oolonghoo.holograms.nms.util.HologramPosition(x, y, z, yaw, pitch);
	}

	private DecentPosition(com.oolonghoo.holograms.nms.util.HologramPosition handle) {
		this.handle = handle;
	}

	public double getX() { return handle.getX(); }
	public double getY() { return handle.getY(); }
	public double getZ() { return handle.getZ(); }
	public float getYaw() { return handle.getYaw(); }
	public float getPitch() { return handle.getPitch(); }

	public DecentPosition addY(double y) {
		return new DecentPosition(handle.addY(y));
	}

	public DecentPosition subtractY(double y) {
		return new DecentPosition(handle.subtractY(y));
	}

	public static DecentPosition fromBukkitLocation(Location location) {
		return new DecentPosition(com.oolonghoo.holograms.nms.util.HologramPosition.fromBukkitLocation(location));
	}

	public Location toBukkitLocation(String worldName) {
		return handle.toBukkitLocation(worldName);
	}

	public com.oolonghoo.holograms.nms.util.HologramPosition getHandle() {
		return handle;
	}
}
