package eu.decentsoftware.holograms.api;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DH 兼容层 - DHAPI 静态工具类
 * 镜像 DecentHolograms 的 DHAPI，委托到 WooHologramsAPI
 */
public final class DHAPI {

	private DHAPI() {}

	// ===== 创建/删除 =====

	public static Hologram createHologram(String name, Location location) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		com.oolonghoo.holograms.hologram.Hologram holo =
				com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().createHologram(name, location);
		return holo != null ? new Hologram(holo) : null;
	}

	public static Hologram createHologram(String name, Location location, List<String> lines) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		Hologram hologram = createHologram(name, location);
		if (hologram != null && lines != null && !lines.isEmpty()) {
			com.oolonghoo.holograms.hologram.HologramPage page = hologram.getHandle().getPage(0);
			if (page != null) {
				for (String line : lines) {
					if (line != null) {
						page.addLine(line);
					}
				}
			}
		}
		return hologram;
	}

	public static Hologram createHologram(String name, Location location, boolean saveToFile) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		com.oolonghoo.holograms.hologram.Hologram holo =
				com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().createHologram(name, location, saveToFile);
		return holo != null ? new Hologram(holo) : null;
	}

	public static Hologram createHologram(String name, Location location, boolean saveToFile, List<String> lines) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		Hologram hologram = createHologram(name, location, saveToFile);
		if (hologram != null && lines != null && !lines.isEmpty()) {
			com.oolonghoo.holograms.hologram.HologramPage page = hologram.getHandle().getPage(0);
			if (page != null) {
				for (String line : lines) {
					if (line != null) {
						page.addLine(line);
					}
				}
			}
		}
		return hologram;
	}

	public static boolean removeHologram(String name) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return false;
		return com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().deleteHologram(name);
	}

	// ===== 查询 =====

	public static Hologram getHologram(String name) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		com.oolonghoo.holograms.hologram.Hologram holo =
				com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().getHologram(name);
		return holo != null ? new Hologram(holo) : null;
	}

	public static Collection<Hologram> getHolograms() {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return java.util.Collections.emptyList();
		Collection<com.oolonghoo.holograms.hologram.Hologram> holos =
				com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().getHolograms();
		List<Hologram> result = new ArrayList<>(holos.size());
		for (com.oolonghoo.holograms.hologram.Hologram holo : holos) {
			result.add(new Hologram(holo));
		}
		return result;
	}

	public static boolean hologramExists(String name) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return false;
		return com.oolonghoo.holograms.api.WooHologramsAPI.getHologramManager().exists(name);
	}

	// ===== 页面操作 =====

	public static HologramPage createHologramPage(Hologram hologram) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		return hologram.addPage();
	}

	public static HologramPage getHologramPage(Hologram hologram, int page) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		return hologram.getPage(page);
	}

	public static HologramPage insertHologramPage(Hologram hologram, int page) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		return hologram.insertPage(page);
	}

	public static boolean removeHologramPage(Hologram hologram, int page) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return false;
		if (hologram == null) return false;
		return hologram.removePage(page);
	}

	// ===== 行操作 =====

	public static void setHologramLines(Hologram hologram, List<String> lines) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		if (hologram == null) return;
		HologramPage page = hologram.getPage(0);
		if (page == null) return;
		page.clearLines();
		if (lines != null) {
			for (String line : lines) {
				if (line != null) {
					page.addLine(line);
				}
			}
		}
	}

	public static HologramLine addHologramLine(Hologram hologram, String line) {
		return addHologramLine(hologram, 0, line);
	}

	public static HologramLine addHologramLine(Hologram hologram, int page, String line) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		HologramPage holoPage = hologram.getPage(page);
		if (holoPage == null) return null;
		return holoPage.addLine(line);
	}

	public static HologramLine insertHologramLine(Hologram hologram, int page, int index, String line) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		HologramPage holoPage = hologram.getPage(page);
		if (holoPage == null) return null;
		boolean success = holoPage.insertLine(index, line);
		return success ? holoPage.getLine(index) : null;
	}

	public static boolean setHologramLine(Hologram hologram, int page, int index, String line) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return false;
		if (hologram == null) return false;
		HologramPage holoPage = hologram.getPage(page);
		if (holoPage == null) return false;
		return holoPage.setLine(index, line);
	}

	public static void setHologramLine(HologramLine line, String content) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		if (line == null || content == null) return;
		line.getHandle().setContent(content);
		com.oolonghoo.holograms.hologram.Hologram holo = line.getHandle().getHologram();
		if (holo != null) holo.save();
	}

	public static boolean removeHologramLine(Hologram hologram, int page, int index) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return false;
		if (hologram == null) return false;
		HologramPage holoPage = hologram.getPage(page);
		if (holoPage == null) return false;
		return holoPage.removeLine(index) != null;
	}

	public static HologramLine getHologramLine(Hologram hologram, int page, int index) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return null;
		if (hologram == null) return null;
		HologramPage holoPage = hologram.getPage(page);
		if (holoPage == null) return null;
		return holoPage.getLine(index);
	}

	// ===== 传送 =====

	public static void teleportHologram(String name, Location location) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		Hologram hologram = getHologram(name);
		if (hologram != null) {
			hologram.teleport(location);
		}
	}

	public static void moveHologram(Hologram hologram, Location location) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		if (hologram == null || location == null) return;
		hologram.getHandle().setLocation(location);
		hologram.getHandle().realignLines();
		hologram.getHandle().save();
	}

	// ===== 显示/隐藏 =====

	public static void showHologram(String name, Player player) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		Hologram hologram = getHologram(name);
		if (hologram != null) {
			hologram.show(player);
		}
	}

	public static void hideHologram(String name, Player player) {
		if (!com.oolonghoo.holograms.api.WooHologramsAPI.isLoaded()) return;
		Hologram hologram = getHologram(name);
		if (hologram != null) {
			hologram.hide(player);
		}
	}
}
