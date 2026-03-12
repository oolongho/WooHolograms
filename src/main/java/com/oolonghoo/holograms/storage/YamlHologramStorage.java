package com.oolonghoo.holograms.storage;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Brightness;
import com.oolonghoo.holograms.hologram.EnumFlag;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.hologram.HologramType;
import com.oolonghoo.holograms.hologram.TextAlignment;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * YAML 格式的全息图存储实现
 * 使用 YAML 文件存储全息图数据
 * 
 */
public class YamlHologramStorage implements HologramStorage {

    private final WooHolograms plugin;
    private final File dataFolder;
    private final File hologramsFile;
    private FileConfiguration storage;
    private final ReentrantLock saveLock = new ReentrantLock();

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
                String errorMsg = e.getMessage();
                plugin.getLogger().severe(() -> "无法创建全息图存储文件: " + errorMsg);
            }
        }
        
        this.storage = YamlConfiguration.loadConfiguration(hologramsFile);
    }

    @Override
    public boolean save(Hologram hologram) {
        saveLock.lock();
        try {
            String id = hologram.getId();
            String path = "holograms." + id;
            
            storage.set(path, null);
            
            Location loc = hologram.getLocation();
            if (loc == null) {
                plugin.getLogger().warning(() -> "无法保存全息图 " + id + ": 位置为空");
                return false;
            }
            
            World world = loc.getWorld();
            if (world == null) {
                plugin.getLogger().warning(() -> "无法保存全息图 " + id + ": 世界为空");
                return false;
            }
            
            storage.set(path + ".world", world.getName());
            storage.set(path + ".x", loc.getX());
            storage.set(path + ".y", loc.getY());
            storage.set(path + ".z", loc.getZ());
            storage.set(path + ".yaw", loc.getYaw());
            storage.set(path + ".pitch", loc.getPitch());
            
            storage.set(path + ".enabled", hologram.isEnabled());
            storage.set(path + ".type", hologram.getType().getId());
            storage.set(path + ".visible", hologram.isVisible());
            storage.set(path + ".persistent", hologram.isPersistent());
            storage.set(path + ".lineHeight", hologram.getLineHeight());
            storage.set(path + ".billboard", hologram.getBillboard().getId());
            storage.set(path + ".facing", hologram.getFacing());
            storage.set(path + ".doubleSided", hologram.isDoubleSided());
            storage.set(path + ".displayRange", hologram.getDisplayRange());
            storage.set(path + ".updateRange", hologram.getUpdateRange());
            storage.set(path + ".updateInterval", hologram.getUpdateInterval());
            storage.set(path + ".downOrigin", hologram.isDownOrigin());
            
            if (hologram.getPermission() != null && !hologram.getPermission().isEmpty()) {
                storage.set(path + ".permission", hologram.getPermission());
            }
            
            if (!hologram.getFlags().isEmpty()) {
                storage.set(path + ".flags", hologram.getFlags().stream()
                        .map(EnumFlag::name)
                        .collect(Collectors.toList()));
            }
            
            List<HologramPage> pages = hologram.getPages();
            for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
                HologramPage page = pages.get(pageIndex);
                String pagePath = path + ".pages." + pageIndex;
                
                savePageActions(pagePath + ".actions", page);
                
                List<HologramLine> lines = page.getLines();
                for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                    HologramLine line = lines.get(lineIndex);
                    String linePath = pagePath + ".lines." + lineIndex;
                    
                    storage.set(linePath + ".content", line.getContent());
                    storage.set(linePath + ".type", line.getType().getId());
                    storage.set(linePath + ".height", line.getHeight());
                    storage.set(linePath + ".offsetX", line.getOffsetX());
                    storage.set(linePath + ".offsetY", line.getOffsetY());
                    storage.set(linePath + ".offsetZ", line.getOffsetZ());
                    storage.set(linePath + ".facing", line.getFacing());
                    
                    if (line.getCustomYaw() != null) {
                        storage.set(linePath + ".customYaw", line.getCustomYaw());
                    }
                    
                    if (line.getCustomPitch() != null) {
                        storage.set(linePath + ".customPitch", line.getCustomPitch());
                    }
                    
                    if (line.getBrightness() != null) {
                        storage.set(linePath + ".brightness", 
                                line.getBrightness().getSkyLight() + "," + line.getBrightness().getBlockLight());
                    }
                    
                    if (line.getAlignment() != null) {
                        storage.set(linePath + ".alignment", line.getAlignment().getId());
                    }
                    
                    if (line.getBillboard() != null) {
                        storage.set(linePath + ".billboard", line.getBillboard().getId());
                    }
                    
                    if (line.getPermission() != null && !line.getPermission().isEmpty()) {
                        storage.set(linePath + ".permission", line.getPermission());
                    }
                    
                    if (!line.getFlags().isEmpty()) {
                        storage.set(linePath + ".flags", line.getFlags().stream()
                                .map(EnumFlag::name)
                                .collect(Collectors.toList()));
                    }
                    
                    if (line.hasActions()) {
                        saveLineActions(linePath + ".actions", line);
                    }
                }
            }
            
            return saveStorage();
        } finally {
            saveLock.unlock();
        }
    }
    
    private void savePageActions(String path, HologramPage page) {
        for (Map.Entry<ClickType, List<Action>> entry : page.getActions().entrySet()) {
            ClickType clickType = entry.getKey();
            List<Action> actions = entry.getValue();
            if (actions == null || actions.isEmpty()) {
                continue;
            }
            
            List<String> actionStrings = new ArrayList<>();
            for (Action action : actions) {
                actionStrings.add(action.toString());
            }
            storage.set(path + "." + clickType.name(), actionStrings);
        }
    }
    
    private void saveLineActions(String path, HologramLine line) {
        for (Map.Entry<ClickType, List<Action>> entry : line.getActions().entrySet()) {
            ClickType clickType = entry.getKey();
            List<Action> actions = entry.getValue();
            if (actions == null || actions.isEmpty()) {
                continue;
            }
            
            List<String> actionStrings = new ArrayList<>();
            for (Action action : actions) {
                actionStrings.add(action.toString());
            }
            storage.set(path + "." + clickType.name(), actionStrings);
        }
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
        if (worldName == null || worldName.isEmpty()) {
            plugin.getLogger().warning(() -> "无法加载全息图 " + id + ": 世界名称为空");
            return null;
        }
        
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning(() -> "无法加载全息图 " + id + ": 世界 " + worldName + " 未加载，将在世界加载后自动加载");
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
        
        hologram.setEnabled(section.getBoolean("enabled", true));
        hologram.setType(HologramType.fromId(section.getString("type", "TEXT")));
        hologram.setVisible(section.getBoolean("visible", true));
        hologram.setPersistent(section.getBoolean("persistent", true));
        hologram.setLineHeight(section.getDouble("lineHeight", 0.25));
        hologram.setBillboard(Billboard.fromId(section.getString("billboard", "center")));
        hologram.setFacing((float) section.getDouble("facing", 0));
        hologram.setDoubleSided(section.getBoolean("doubleSided", false));
        hologram.setDisplayRange(section.getDouble("displayRange", 48.0));
        hologram.setUpdateRange(section.getDouble("updateRange", 48.0));
        hologram.setUpdateInterval(section.getInt("updateInterval", 3));
        hologram.setDownOrigin(section.getBoolean("downOrigin", true));
        
        String permission = section.getString("permission");
        if (permission != null && !permission.isEmpty()) {
            hologram.setPermission(permission);
        }
        
        if (section.contains("flags")) {
            List<String> flagList = section.getStringList("flags");
            for (String flagStr : flagList) {
                try {
                    EnumFlag flag = EnumFlag.valueOf(flagStr.toUpperCase());
                    hologram.addFlags(flag);
                } catch (IllegalArgumentException e) {
                    if (plugin.getConfigManager().isDebug()) {
                        plugin.getLogger().warning(() -> "Unknown flag '" + flagStr + "' for hologram " + hologram.getName());
                    }
                }
            }
        }
        
        ConfigurationSection pagesSection = section.getConfigurationSection("pages");
        if (pagesSection != null) {
            hologram.removePage(0);
            
            Set<String> pageKeys = pagesSection.getKeys(false);
            for (String pageIndex : pageKeys) {
                HologramPage page = hologram.addPage();
                ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageIndex);
                
                if (pageSection != null) {
                    loadPageActions(pageSection.getConfigurationSection("actions"), page);
                    
                    ConfigurationSection linesSection = pageSection.getConfigurationSection("lines");
                    if (linesSection != null) {
                        Set<String> lineKeys = linesSection.getKeys(false);
                        for (String lineIndex : lineKeys) {
                            ConfigurationSection lineSection = linesSection.getConfigurationSection(lineIndex);
                            if (lineSection != null) {
                                loadHologramLine(lineSection, page);
                            }
                        }
                    }
                }
            }
        }
        
        return hologram;
    }
    
    private void loadPageActions(ConfigurationSection section, HologramPage page) {
        if (section == null) {
            return;
        }
        
        for (ClickType clickType : ClickType.values()) {
            if (!section.contains(clickType.name())) {
                continue;
            }
            
            List<String> actionStrings = section.getStringList(clickType.name());
            for (String actionStr : actionStrings) {
                Action action = Action.fromString(actionStr);
                if (action != null) {
                    page.addAction(clickType, action);
                }
            }
        }
    }
    
    private void loadHologramLine(ConfigurationSection section, HologramPage page) {
        String content = section.getString("content", "");
        HologramLine line = page.addLine(content);
        
        line.setHeight(section.getDouble("height", 0.25));
        line.setOffsetX(section.getDouble("offsetX", 0));
        line.setOffsetY(section.getDouble("offsetY", 0));
        line.setOffsetZ(section.getDouble("offsetZ", 0));
        line.setFacing((float) section.getDouble("facing", 0));
        
        if (section.contains("customYaw")) {
            line.setCustomYaw((float) section.getDouble("customYaw"));
        }
        
        if (section.contains("customPitch")) {
            line.setCustomPitch((float) section.getDouble("customPitch"));
        }
        
        page.realignLines();
        
        if (section.contains("brightness")) {
            String brightnessValue = section.getString("brightness", "15,15");
            if (brightnessValue != null) {
                String[] brightnessParts = brightnessValue.split(",");
                if (brightnessParts.length == 2) {
                    try {
                        int skyLight = Integer.parseInt(brightnessParts[0]);
                        int blockLight = Integer.parseInt(brightnessParts[1]);
                        line.setBrightness(Brightness.of(skyLight, blockLight));
                    } catch (NumberFormatException e) {
                        if (plugin.getConfigManager().isDebug()) {
                            String hologramName = line.getHologram() != null ? line.getHologram().getName() : "unknown";
                            plugin.getLogger().warning(() -> "Invalid brightness format for line in hologram " + hologramName + ": " + brightnessValue);
                        }
                    }
                }
            }
        }
        
        if (section.contains("alignment")) {
            line.setAlignment(TextAlignment.fromId(section.getString("alignment")));
        }
        
        if (section.contains("billboard")) {
            line.setBillboard(Billboard.fromId(section.getString("billboard")));
        }
        
        String linePermission = section.getString("permission");
        if (linePermission != null && !linePermission.isEmpty()) {
            line.setPermission(linePermission);
        }
        
        if (section.contains("flags")) {
            List<String> flagList = section.getStringList("flags");
            for (String flagStr : flagList) {
                try {
                    EnumFlag flag = EnumFlag.valueOf(flagStr.toUpperCase());
                    line.addFlags(flag);
                } catch (IllegalArgumentException e) {
                    if (plugin.getConfigManager().isDebug()) {
                        String hologramName = line.getHologram() != null ? line.getHologram().getName() : "unknown";
                        plugin.getLogger().warning(() -> "Unknown flag '" + flagStr + "' for line in hologram " + hologramName);
                    }
                }
            }
        }
        
        ConfigurationSection actionsSection = section.getConfigurationSection("actions");
        if (actionsSection != null) {
            for (ClickType clickType : ClickType.values()) {
                if (!actionsSection.contains(clickType.name())) {
                    continue;
                }
                
                List<String> actionStrings = actionsSection.getStringList(clickType.name());
                for (String actionStr : actionStrings) {
                    Action action = Action.fromString(actionStr);
                    if (action != null) {
                        line.addAction(clickType, action);
                    }
                }
            }
        }
    }

    private boolean saveStorage() {
        try {
            storage.save(hologramsFile);
            return true;
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            plugin.getLogger().severe(() -> "无法保存全息图存储文件: " + errorMsg);
            return false;
        }
    }

    @Override
    public void reload() {
        storage = YamlConfiguration.loadConfiguration(hologramsFile);
    }
}
