package com.oolonghoo.holograms.storage;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.hologram.HologramType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * YAML 格式的全息图存储实现
 * 使用 YAML 文件存储全息图数据
 * 
 * @author oolongho
 */
public class YamlHologramStorage implements HologramStorage {

    private final WooHolograms plugin;
    private final File dataFolder;
    private final File hologramsFile;
    private FileConfiguration storage;

    public YamlHologramStorage(WooHolograms plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.hologramsFile = new File(dataFolder, "holograms.yml");
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        if (!hologramsFile.exists()) {
            try {
                hologramsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建全息图存储文件: " + e.getMessage());
            }
        }
        
        this.storage = YamlConfiguration.loadConfiguration(hologramsFile);
    }

    @Override
    public boolean save(Hologram hologram) {
        String id = hologram.getId();
        String path = "holograms." + id;
        
        storage.set(path, null);
        
        Location loc = hologram.getLocation();
        storage.set(path + ".world", loc.getWorld().getName());
        storage.set(path + ".x", loc.getX());
        storage.set(path + ".y", loc.getY());
        storage.set(path + ".z", loc.getZ());
        storage.set(path + ".yaw", loc.getYaw());
        storage.set(path + ".pitch", loc.getPitch());
        storage.set(path + ".type", hologram.getType().getId());
        storage.set(path + ".visible", hologram.isVisible());
        storage.set(path + ".persistent", hologram.isPersistent());
        storage.set(path + ".lineHeight", hologram.getLineHeight());
        
        List<HologramPage> pages = hologram.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            HologramPage page = pages.get(pageIndex);
            String pagePath = path + ".pages." + pageIndex;
            
            List<HologramLine> lines = page.getLines();
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                HologramLine line = lines.get(lineIndex);
                String linePath = pagePath + ".lines." + lineIndex;
                
                storage.set(linePath + ".text", line.getContent());
                storage.set(linePath + ".type", line.getType().getId());
                storage.set(linePath + ".offsetY", line.getOffsetY());
            }
        }
        
        return saveStorage();
    }

    @Override
    public Hologram load(String id) {
        String path = "holograms." + id;
        if (!storage.contains(path)) {
            return null;
        }
        return loadHologram(id, storage.getConfigurationSection(path));
    }

    @Override
    public Map<String, Hologram> loadAll() {
        Map<String, Hologram> holograms = new HashMap<>();
        
        ConfigurationSection section = storage.getConfigurationSection("holograms");
        if (section == null) {
            return holograms;
        }
        
        Set<String> keys = section.getKeys(false);
        for (String id : keys) {
            Hologram hologram = load(id);
            if (hologram != null) {
                holograms.put(id, hologram);
            }
        }
        
        return holograms;
    }

    @Override
    public boolean delete(String id) {
        storage.set("holograms." + id, null);
        return saveStorage();
    }

    @Override
    public boolean exists(String id) {
        return storage.contains("holograms." + id);
    }

    @Override
    public int count() {
        ConfigurationSection section = storage.getConfigurationSection("holograms");
        return section == null ? 0 : section.getKeys(false).size();
    }

    @Override
    public void saveAsync(Hologram hologram) {
        // 在主线程异步保存
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> save(hologram));
    }

    @Override
    public void saveAll() {
        // 由 HologramManager 调用，这里不需要实现
    }

    @Override
    public void saveAllAsync() {
        // 由 HologramManager 调用，这里不需要实现
    }

    private Hologram loadHologram(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        String worldName = section.getString("world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("无法加载全息图 " + id + ": 世界 " + worldName + " 不存在");
            return null;
        }
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);
        
        Location location = new Location(world, x, y, z, yaw, pitch);
        Hologram hologram = new Hologram(id, location, true);
        hologram.setStorage(plugin.getHologramManager().getStorage());
        
        hologram.setType(HologramType.fromId(section.getString("type", "text_display")));
        hologram.setVisible(section.getBoolean("visible", true));
        hologram.setPersistent(section.getBoolean("persistent", true));
        hologram.setLineHeight(section.getDouble("lineHeight", 0.25));
        
        ConfigurationSection pagesSection = section.getConfigurationSection("pages");
        if (pagesSection != null) {
            hologram.removePage(0);
            
            Set<String> pageKeys = pagesSection.getKeys(false);
            for (String pageIndex : pageKeys) {
                HologramPage page = hologram.addPage();
                ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageIndex);
                
                if (pageSection != null) {
                    ConfigurationSection linesSection = pageSection.getConfigurationSection("lines");
                    if (linesSection != null) {
                        Set<String> lineKeys = linesSection.getKeys(false);
                        for (String lineIndex : lineKeys) {
                            ConfigurationSection lineSection = linesSection.getConfigurationSection(lineIndex);
                            if (lineSection != null) {
                                String text = lineSection.getString("text", "");
                                double offsetY = lineSection.getDouble("offsetY", 0);
                                
                                HologramLine line = page.addLine(text);
                                line.setOffsetY(offsetY);
                            }
                        }
                    }
                }
            }
        }
        
        return hologram;
    }

    private boolean saveStorage() {
        try {
            storage.save(hologramsFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存全息图存储文件: " + e.getMessage());
            return false;
        }
    }

    public void reload() {
        storage = YamlConfiguration.loadConfiguration(hologramsFile);
    }
}
