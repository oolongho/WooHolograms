package com.oolongho.holograms.storage;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.EnumFlag;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.hologram.HologramType;
import com.oolongho.holograms.hologram.TextAlignment;
import com.oolongho.holograms.util.LocationUtil;
import com.oolongho.holograms.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class YamlHologramStorage implements HologramStorage {

    private final WooHolograms plugin;
    private final File hologramsDir;
    private final ReentrantLock saveLock = new ReentrantLock();

    // 世界未加载时暂存的全息图 ID，按世界名分组
    private final Map<String, List<String>> pendingHolograms = new HashMap<>();

    public YamlHologramStorage(WooHolograms plugin) {
        this.plugin = plugin;
        this.hologramsDir = new File(plugin.getDataFolder(), "holograms");
        if (!hologramsDir.exists()) {
            hologramsDir.mkdirs();
        }
    }

    private File getHologramFile(String id) {
        if (id.contains("..") || id.contains("/") || id.contains("\\") || !id.matches("[\\w\\-\\p{L}]+")) {
            throw new IllegalArgumentException("Invalid hologram ID: " + id);
        }
        return new File(hologramsDir, id + ".yml");
    }

    @Override
    public boolean save(Hologram hologram) {
        String id = hologram.getId();
        File file = getHologramFile(id);

        // 在 visibilityMutex 内读取状态构建快照，保证与状态修改方法的一致性
        YamlConfiguration yaml;
        synchronized (hologram.getStateLock()) {
            yaml = buildYamlSnapshot(hologram);
        }
        if (yaml == null) {
            return false;
        }

        saveLock.lock();
        try {
            return saveYaml(yaml, file);
        } finally {
            saveLock.unlock();
        }
    }

    /**
     * 构建 Hologram 状态的 YamlConfiguration 快照
     * 调用者必须在 synchronized (hologram.getStateLock()) 内调用，以保证读取状态的一致性
     *
     * @param hologram 全息图
     * @return YamlConfiguration 快照，location/world 无效时返回 null
     */
    private YamlConfiguration buildYamlSnapshot(Hologram hologram) {
        String id = hologram.getId();
        YamlConfiguration yaml = new YamlConfiguration();

        Location loc = hologram.getLocation();
        if (loc == null) {
            plugin.getLogger().warning(() -> "Cannot save hologram " + id + ": location is null");
            return null;
        }

        World world = loc.getWorld();
        if (world == null) {
            plugin.getLogger().warning(() -> "Cannot save hologram " + id + ": world is null");
            return null;
        }

        yaml.set("location", LocationUtil.toString(loc));
        yaml.set("enabled", hologram.isEnabled());
        yaml.set("type", hologram.getType().getId());
        yaml.set("visible", hologram.isVisible());
        yaml.set("persistent", hologram.isPersistent());
        yaml.set("line-height", hologram.getLineHeight());
        yaml.set("billboard", hologram.getBillboard().getId());
        yaml.set("facing", hologram.getFacing());
        yaml.set("double-sided", hologram.isDoubleSided());
        yaml.set("display-range", hologram.getDisplayRange());
        yaml.set("update-range", hologram.getUpdateRange());
        yaml.set("update-interval", hologram.getUpdateInterval());
        yaml.set("alignment", hologram.getAlignment().getId());
        yaml.set("background-alpha", hologram.getBackgroundAlpha());
        yaml.set("background-color", hologram.getBackgroundColor());
        yaml.set("line-width", hologram.getLineWidth());
        yaml.set("scale-x", hologram.getScaleX());
        yaml.set("scale-y", hologram.getScaleY());
        yaml.set("scale-z", hologram.getScaleZ());
        yaml.set("translation-x", hologram.getTranslationX());
        yaml.set("translation-y", hologram.getTranslationY());
        yaml.set("translation-z", hologram.getTranslationZ());
        yaml.set("shadow-radius", hologram.getShadowRadius());
        yaml.set("shadow-strength", hologram.getShadowStrength());
        yaml.set("glow-color", hologram.getGlowColor());
        if (hologram.getBrightness() != null) {
            yaml.set("brightness", hologram.getBrightness().getSkyLight() + "," + hologram.getBrightness().getBlockLight());
        } else {
            yaml.set("brightness", null);
        }
        yaml.set("chroma-background", hologram.isChromaBackground());
        yaml.set("chroma-glow", hologram.isChromaGlow());

        if (hologram.getPermission() != null && !hologram.getPermission().isEmpty()) {
            yaml.set("permission", hologram.getPermission());
        }

        if (!hologram.getFlags().isEmpty()) {
            yaml.set("flags", hologram.getFlags().stream()
                    .map(EnumFlag::name)
                    .collect(Collectors.toList()));
        }

        List<HologramPage> pages = hologram.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            HologramPage page = pages.get(pageIndex);
            String pagePath = "pages." + pageIndex;

            saveActions(yaml, pagePath + ".actions", page.getActions());

            if (!page.getFlags().isEmpty()) {
                yaml.set(pagePath + ".flags", page.getFlags().stream()
                        .map(EnumFlag::name)
                        .collect(Collectors.toList()));
            }

            List<HologramLine> lines = page.getLines();
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                HologramLine line = lines.get(lineIndex);
                String linePath = pagePath + ".lines." + lineIndex;

                yaml.set(linePath + ".content", line.getContent());
                yaml.set(linePath + ".height", line.getBaseHeight());
                yaml.set(linePath + ".offsetX", line.getOffsetX());
                yaml.set(linePath + ".offsetY", line.getOffsetY());
                yaml.set(linePath + ".offsetZ", line.getOffsetZ());
                yaml.set(linePath + ".facing", line.getFacing());

                if (line.getCustomYaw() != null) {
                    yaml.set(linePath + ".custom-yaw", line.getCustomYaw());
                }

                if (line.getCustomPitch() != null) {
                    yaml.set(linePath + ".custom-pitch", line.getCustomPitch());
                }

                if (line.getBrightness() != null) {
                    yaml.set(linePath + ".brightness",
                            line.getBrightness().getSkyLight() + "," + line.getBrightness().getBlockLight());
                }

                if (line.getBillboard() != null) {
                    yaml.set(linePath + ".billboard", line.getBillboard().getId());
                }

                // Display Entity 行级别属性
                if (line.getScaleX() != null || line.getScaleY() != null || line.getScaleZ() != null) {
                    if (line.getScaleX() != null) yaml.set(linePath + ".scale-x", line.getScaleX());
                    if (line.getScaleY() != null) yaml.set(linePath + ".scale-y", line.getScaleY());
                    if (line.getScaleZ() != null) yaml.set(linePath + ".scale-z", line.getScaleZ());
                }

                if (line.getTranslationX() != null || line.getTranslationY() != null || line.getTranslationZ() != null) {
                    if (line.getTranslationX() != null) yaml.set(linePath + ".translation-x", line.getTranslationX());
                    if (line.getTranslationY() != null) yaml.set(linePath + ".translation-y", line.getTranslationY());
                    if (line.getTranslationZ() != null) yaml.set(linePath + ".translation-z", line.getTranslationZ());
                }

                if (line.getShadowRadius() != null) {
                    yaml.set(linePath + ".shadow-radius", line.getShadowRadius());
                }

                if (line.getShadowStrength() != null) {
                    yaml.set(linePath + ".shadow-strength", line.getShadowStrength());
                }

                if (line.getGlowColor() != null) {
                    yaml.set(linePath + ".glow-color", line.getGlowColor());
                }

                if (line.getChromaBackground() != null) {
                    yaml.set(linePath + ".chroma-background", line.getChromaBackground());
                }

                if (line.getChromaGlow() != null) {
                    yaml.set(linePath + ".chroma-glow", line.getChromaGlow());
                }

                if (line.getPermission() != null && !line.getPermission().isEmpty()) {
                    yaml.set(linePath + ".permission", line.getPermission());
                }

                if (!line.getFlags().isEmpty()) {
                    yaml.set(linePath + ".flags", line.getFlags().stream()
                            .map(EnumFlag::name)
                            .collect(Collectors.toList()));
                }

                if (line.hasActions()) {
                    saveActions(yaml, linePath + ".actions", line.getActions());
                }
            }
        }

        return yaml;
    }

    private void saveActions(YamlConfiguration yaml, String path, Map<ClickType, List<Action>> actionsMap) {
        for (Map.Entry<ClickType, List<Action>> entry : actionsMap.entrySet()) {
            ClickType clickType = entry.getKey();
            List<Action> actions = entry.getValue();
            if (actions == null || actions.isEmpty()) {
                continue;
            }
            List<String> actionStrings = new ArrayList<>();
            for (Action action : actions) {
                actionStrings.add(action.toString());
            }
            yaml.set(path + "." + clickType.name(), actionStrings);
        }
    }

    @Override
    public Hologram load(String id) {
        File file = getHologramFile(id);
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return loadHologram(id, yaml);
    }

    @Override
    public Map<String, Hologram> loadAll() {
        Map<String, Hologram> holograms = new HashMap<>();

        File[] files = hologramsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return holograms;
        }

        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4);
            Hologram hologram = load(id);
            if (hologram != null) {
                holograms.put(id, hologram);
            } else {
                // 尝试获取世界名，若世界未加载则加入 pending
                String worldName = getWorldNameFromFile(id);
                if (worldName != null && plugin.getServer().getWorld(worldName) == null) {
                    pendingHolograms.computeIfAbsent(worldName, k -> new ArrayList<>()).add(id);
                }
            }
        }

        return holograms;
    }

    /**
     * 从文件中读取全息图的世界名（不完整加载全息图）
     */
    private String getWorldNameFromFile(String id) {
        File file = getHologramFile(id);
        if (!file.exists()) return null;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        // 优先从 location 字符串解析
        String locStr = yaml.getString("location");
        if (locStr != null && !locStr.isEmpty()) {
            String[] parts = locStr.split(",");
            if (parts.length > 0) return parts[0];
        }
        // 兼容旧格式
        return yaml.getString("world");
    }

    /**
     * 加载指定世界的待处理全息图
     * 当世界加载后调用，尝试加载之前因世界未加载而失败的全息图
     *
     * @param worldName 世界名称
     * @return 成功加载的全息图映射
     */
    public Map<String, Hologram> loadPendingForWorld(String worldName) {
        List<String> pendingIds = pendingHolograms.remove(worldName);
        if (pendingIds == null || pendingIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Hologram> loaded = new HashMap<>();
        for (String id : pendingIds) {
            Hologram hologram = load(id);
            if (hologram != null) {
                loaded.put(id, hologram);
            }
        }
        return loaded;
    }

    @Override
    public boolean delete(String id) {
        File file = getHologramFile(id);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    @Override
    public boolean exists(String id) {
        return getHologramFile(id).exists();
    }

    @Override
    public int count() {
        File[] files = hologramsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        return files == null ? 0 : files.length;
    }

    @Override
    public boolean saveAsync(Hologram hologram) {
        String id = hologram.getId();
        File file = getHologramFile(id);

        // 在 visibilityMutex 内读取状态构建快照，保证与状态修改方法的一致性
        YamlConfiguration yaml;
        synchronized (hologram.getStateLock()) {
            yaml = buildYamlSnapshot(hologram);
        }
        if (yaml == null) {
            return false;
        }

        // 仅将文件写入操作提交到异步线程，saveLock 串行化文件写入避免交叉
        YamlConfiguration yamlSnapshot = yaml;
        File fileSnapshot = file;
        SchedulerUtil.runTaskAsynchronously(() -> {
            saveLock.lock();
            try {
                saveYaml(yamlSnapshot, fileSnapshot);
            } finally {
                saveLock.unlock();
            }
        });
        return true;
    }

    @Override
    public void saveAll() {
    }

    @Override
    public void saveAllAsync() {
    }

    private <T> T getCompatValue(ConfigurationSection section, String kebabKey, String camelKey,
                                  T defaultValue, BiFunction<ConfigurationSection, String, T> reader) {
        if (section.contains(kebabKey)) {
            return reader.apply(section, kebabKey);
        }
        if (section.contains(camelKey)) {
            return reader.apply(section, camelKey);
        }
        return defaultValue;
    }

    private boolean getCompatBoolean(ConfigurationSection section, String kebabKey, String camelKey, boolean defaultValue) {
        return getCompatValue(section, kebabKey, camelKey, defaultValue, ConfigurationSection::getBoolean);
    }

    private double getCompatDouble(ConfigurationSection section, String kebabKey, String camelKey, double defaultValue) {
        return getCompatValue(section, kebabKey, camelKey, defaultValue, ConfigurationSection::getDouble);
    }

    private int getCompatInt(ConfigurationSection section, String kebabKey, String camelKey, int defaultValue) {
        return getCompatValue(section, kebabKey, camelKey, defaultValue, ConfigurationSection::getInt);
    }

    private Location loadLocation(ConfigurationSection section) {
        if (section.contains("location")) {
            String locStr = section.getString("location");
            if (locStr != null && !locStr.isEmpty()) {
                Location loc = LocationUtil.asLocation(locStr);
                if (loc != null) {
                    return loc;
                }
            }
        }

        String worldName = section.getString("world");
        if (worldName == null || worldName.isEmpty()) {
            return null;
        }

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    private Hologram loadHologram(String id, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        Location location = loadLocation(section);
        if (location == null) {
            plugin.getLogger().warning(() -> "Cannot load hologram " + id + ": location is null");
            return null;
        }

        Hologram hologram = new Hologram(id, location, true);
        hologram.setStorage(this);

        hologram.setEnabled(section.getBoolean("enabled", true));
        String typeId = section.getString("type");
        if (typeId != null && !typeId.isEmpty()) {
            hologram.setType(HologramType.fromId(typeId));
        }
        hologram.setVisible(section.getBoolean("visible", true));
        hologram.setPersistent(section.getBoolean("persistent", true));
        hologram.setLineHeight(getCompatDouble(section, "line-height", "lineHeight", 0.25));
        hologram.setBillboard(Billboard.fromId(section.getString("billboard")));
        hologram.setFacing((float) section.getDouble("facing", 0));
        hologram.setDoubleSided(getCompatBoolean(section, "double-sided", "doubleSided", false));
        hologram.setDisplayRange(getCompatDouble(section, "display-range", "displayRange", 48.0));
        hologram.setUpdateRange(getCompatDouble(section, "update-range", "updateRange", 48.0));
        hologram.setUpdateInterval(getCompatInt(section, "update-interval", "updateInterval", 40));
        hologram.setAlignment(TextAlignment.fromId(section.getString("alignment", "LEFT")));
        hologram.setBackgroundAlpha(section.getInt("background-alpha", plugin.getConfigManager().getDefaultBackgroundAlpha()));
        hologram.setBackgroundColor(section.getInt("background-color", plugin.getConfigManager().getDefaultBackgroundColor()));
        hologram.setLineWidth(section.getInt("line-width", plugin.getConfigManager().getDefaultLineWidth()));

        // Display Entity 全局属性
        hologram.setScale(
                (float) getCompatDouble(section, "scale-x", "scaleX", 1.0),
                (float) getCompatDouble(section, "scale-y", "scaleY", 1.0),
                (float) getCompatDouble(section, "scale-z", "scaleZ", 1.0)
        );
        hologram.setTranslation(
                getCompatDouble(section, "translation-x", "translationX", 0),
                getCompatDouble(section, "translation-y", "translationY", 0),
                getCompatDouble(section, "translation-z", "translationZ", 0)
        );
        hologram.setShadowRadius((float) getCompatDouble(section, "shadow-radius", "shadowRadius", 0));
        hologram.setShadowStrength((float) getCompatDouble(section, "shadow-strength", "shadowStrength", 1.0));
        hologram.setGlowColor(getCompatInt(section, "glow-color", "glowColor", -1));
        hologram.setChromaBackground(getCompatBoolean(section, "chroma-background", "chromaBackground", false));
        hologram.setChromaGlow(getCompatBoolean(section, "chroma-glow", "chromaGlow", false));

        // 亮度覆盖
        String brightnessStr = section.contains("brightness") ? section.getString("brightness") : null;
        if (brightnessStr != null && !brightnessStr.isEmpty()) {
            String[] parts = brightnessStr.split(",");
            if (parts.length == 2) {
                try {
                    int sky = Integer.parseInt(parts[0].trim());
                    int block = Integer.parseInt(parts[1].trim());
                    hologram.setBrightness(Brightness.of(sky, block));
                } catch (NumberFormatException e) {
                    if (plugin.getConfigManager().isDebug()) {
                        plugin.getLogger().warning(() -> "Invalid brightness format for hologram " + hologram.getName() + ": " + brightnessStr);
                    }
                }
            }
        }

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
            List<String> sortedPageKeys = new ArrayList<>(pageKeys);
            sortedPageKeys.sort(Comparator.comparingInt(Integer::parseInt));
            for (String pageIndex : sortedPageKeys) {
                HologramPage page = hologram.addPage();
                ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageIndex);

                if (pageSection != null) {
                    loadPageActions(pageSection.getConfigurationSection("actions"), page);

                    // 加载页面级 flags
                    if (pageSection.contains("flags")) {
                        List<String> pageFlagList = pageSection.getStringList("flags");
                        for (String flagStr : pageFlagList) {
                            try {
                                EnumFlag flag = EnumFlag.valueOf(flagStr.toUpperCase());
                                page.addFlags(flag);
                            } catch (IllegalArgumentException e) {
                                if (plugin.getConfigManager().isDebug()) {
                                    plugin.getLogger().warning(() -> "Unknown flag '" + flagStr + "' for page " + pageIndex + " in hologram " + id);
                                }
                            }
                        }
                    }

                    ConfigurationSection linesSection = pageSection.getConfigurationSection("lines");
                    if (linesSection != null) {
                        Set<String> lineKeys = linesSection.getKeys(false);
                        List<String> sortedLineKeys = new ArrayList<>(lineKeys);
                        sortedLineKeys.sort(Comparator.comparingInt(Integer::parseInt));
                        for (String lineIndex : sortedLineKeys) {
                            ConfigurationSection lineSection = linesSection.getConfigurationSection(lineIndex);
                            if (lineSection != null) {
                                loadHologramLine(lineSection, page);
                            } else {
                                String content = linesSection.getString(lineIndex);
                                if (content != null) {
                                    page.addLine(content);
                                }
                            }
                        }
                        // 加载完所有行后统一重建分组（addLine 时偏移=0，setOffsetX/Y/Z 是裸 setter 不触发 rebuild）
                        page.onLoadComplete();
                    }
                }
            }
        } else if (section.contains("pages")) {
            List<?> pagesList = section.getList("pages");
            if (pagesList != null) {
                hologram.removePage(0);
                for (Object pageObj : pagesList) {
                    if (!(pageObj instanceof Map)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pageMap = (Map<String, Object>) pageObj;
                    HologramPage page = hologram.addPage();

                    Object actionsObj = pageMap.get("actions");
                    if (actionsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> actionsMap = (Map<String, Object>) actionsObj;
                        loadPageActionsFromMap(actionsMap, page);
                    }

                    // 加载页面级 flags
                    Object pageFlagsObj = pageMap.get("flags");
                    if (pageFlagsObj instanceof List<?> pageFlagList) {
                        for (Object flagObj : pageFlagList) {
                            if (flagObj instanceof String flagStr) {
                                try {
                                    page.addFlags(EnumFlag.valueOf(flagStr.toUpperCase()));
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        }
                    }

                    Object linesObj = pageMap.get("lines");
                    if (linesObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> linesList = (List<Object>) linesObj;
                        for (Object lineObj : linesList) {
                            if (lineObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> lineMap = (Map<String, Object>) lineObj;
                                loadHologramLineFromMap(lineMap, page);
                            } else if (lineObj instanceof String) {
                                page.addLine((String) lineObj);
                            }
                        }
                        // 加载完所有行后统一重建分组（addLine 时偏移=0，setOffsetX/Y/Z 是裸 setter 不触发 rebuild）
                        page.onLoadComplete();
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

    private void loadPageActionsFromMap(Map<String, Object> actionsMap, HologramPage page) {
        for (ClickType clickType : ClickType.values()) {
            Object actionListObj = actionsMap.get(clickType.name());
            if (!(actionListObj instanceof List)) continue;
            @SuppressWarnings("unchecked")
            List<Object> actionList = (List<Object>) actionListObj;
            for (Object actionObj : actionList) {
                if (actionObj instanceof String actionStr) {
                    Action action = Action.fromString(actionStr);
                    if (action != null) {
                        page.addAction(clickType, action);
                    }
                }
            }
        }
    }

    private void loadHologramLineFromMap(Map<String, Object> map, HologramPage page) {
        String content = map.getOrDefault("content", "") instanceof String s ? s : "";
        HologramLine line = page.addLine(content);

        Object heightObj = map.get("height");
        if (heightObj instanceof Number n) {
            line.setHeight(n.doubleValue());
        }

        Object offsetXObj = map.get("offsetX");
        if (offsetXObj instanceof Number n) line.setOffsetX(n.doubleValue());

        Object offsetYObj = map.get("offsetY");
        if (offsetYObj instanceof Number n) line.setOffsetY(n.doubleValue());

        Object offsetZObj = map.get("offsetZ");
        if (offsetZObj instanceof Number n) line.setOffsetZ(n.doubleValue());

        Object facingObj = map.get("facing");
        if (facingObj instanceof Number n) line.setFacing(n.floatValue());

        Object customYawObj = map.get("custom-yaw");
        if (customYawObj == null) customYawObj = map.get("customYaw");
        if (customYawObj instanceof Number n) line.setCustomYaw(n.floatValue());

        Object customPitchObj = map.get("custom-pitch");
        if (customPitchObj == null) customPitchObj = map.get("customPitch");
        if (customPitchObj instanceof Number n) line.setCustomPitch(n.floatValue());

        Object brightnessObj = map.get("brightness");
        if (brightnessObj instanceof String brightnessStr) {
            String[] parts = brightnessStr.split(",");
            if (parts.length == 2) {
                try {
                    line.setBrightness(Brightness.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        Object billboardObj = map.get("billboard");
        if (billboardObj instanceof String billboardStr) {
            line.setBillboard(Billboard.fromId(billboardStr));
        }

        // Display Entity 行级别属性
        Object scaleXObj = map.get("scale-x");
        if (scaleXObj == null) scaleXObj = map.get("scaleX");
        Object scaleYObj = map.get("scale-y");
        if (scaleYObj == null) scaleYObj = map.get("scaleY");
        Object scaleZObj = map.get("scale-z");
        if (scaleZObj == null) scaleZObj = map.get("scaleZ");
        if (scaleXObj instanceof Number || scaleYObj instanceof Number || scaleZObj instanceof Number) {
            Float sx = scaleXObj instanceof Number n ? n.floatValue() : null;
            Float sy = scaleYObj instanceof Number n ? n.floatValue() : null;
            Float sz = scaleZObj instanceof Number n ? n.floatValue() : null;
            line.setScale(sx, sy, sz);
        }

        Object translationXObj = map.get("translation-x");
        if (translationXObj == null) translationXObj = map.get("translationX");
        Object translationYObj = map.get("translation-y");
        if (translationYObj == null) translationYObj = map.get("translationY");
        Object translationZObj = map.get("translation-z");
        if (translationZObj == null) translationZObj = map.get("translationZ");
        if (translationXObj instanceof Number || translationYObj instanceof Number || translationZObj instanceof Number) {
            Double tx = translationXObj instanceof Number n ? n.doubleValue() : null;
            Double ty = translationYObj instanceof Number n ? n.doubleValue() : null;
            Double tz = translationZObj instanceof Number n ? n.doubleValue() : null;
            line.setTranslation(tx, ty, tz);
        }

        Object shadowRadiusObj = map.get("shadow-radius");
        if (shadowRadiusObj == null) shadowRadiusObj = map.get("shadowRadius");
        if (shadowRadiusObj instanceof Number n) line.setShadowRadius(n.floatValue());

        Object shadowStrengthObj = map.get("shadow-strength");
        if (shadowStrengthObj == null) shadowStrengthObj = map.get("shadowStrength");
        if (shadowStrengthObj instanceof Number n) line.setShadowStrength(n.floatValue());

        Object glowColorObj = map.get("glow-color");
        if (glowColorObj == null) glowColorObj = map.get("glowColor");
        if (glowColorObj instanceof Number n) line.setGlowColor(n.intValue());

        Object chromaBgObj = map.get("chroma-background");
        if (chromaBgObj == null) chromaBgObj = map.get("chromaBackground");
        if (chromaBgObj instanceof Boolean b) line.setChromaBackground(b);

        Object chromaGlowObj = map.get("chroma-glow");
        if (chromaGlowObj == null) chromaGlowObj = map.get("chromaGlow");
        if (chromaGlowObj instanceof Boolean b) line.setChromaGlow(b);

        Object permissionObj = map.get("permission");
        if (permissionObj instanceof String permStr && !permStr.isEmpty()) {
            line.setPermission(permStr);
        }

        Object flagsObj = map.get("flags");
        if (flagsObj instanceof List<?> flagList) {
            for (Object flagObj : flagList) {
                if (flagObj instanceof String flagStr) {
                    try {
                        line.addFlags(EnumFlag.valueOf(flagStr.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        Object actionsObj = map.get("actions");
        if (actionsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actionsMap = (Map<String, Object>) actionsObj;
            for (ClickType clickType : ClickType.values()) {
                Object actionListObj = actionsMap.get(clickType.name());
                if (!(actionListObj instanceof List)) continue;
                @SuppressWarnings("unchecked")
                List<Object> actionList = (List<Object>) actionListObj;
                for (Object actionObj : actionList) {
                    if (actionObj instanceof String actionStr) {
                        Action action = Action.fromString(actionStr);
                        if (action != null) {
                            line.addAction(clickType, action);
                        }
                    }
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

        if (section.contains("custom-yaw")) {
            line.setCustomYaw((float) section.getDouble("custom-yaw", 0));
        } else if (section.contains("customYaw")) {
            line.setCustomYaw((float) section.getDouble("customYaw", 0));
        }

        if (section.contains("custom-pitch")) {
            line.setCustomPitch((float) section.getDouble("custom-pitch", 0));
        } else if (section.contains("customPitch")) {
            line.setCustomPitch((float) section.getDouble("customPitch", 0));
        }

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

        if (section.contains("billboard")) {
            String billboardId = section.getString("billboard");
            line.setBillboard(billboardId != null && !billboardId.isEmpty() ? Billboard.fromId(billboardId) : null);
        }

        // Display Entity 行级别属性
        float sx = (float) getCompatDouble(section, "scale-x", "scaleX", 1.0);
        float sy = (float) getCompatDouble(section, "scale-y", "scaleY", 1.0);
        float sz = (float) getCompatDouble(section, "scale-z", "scaleZ", 1.0);

        boolean hasCustomScale = section.contains("scale-x") || section.contains("scaleX")
                || section.contains("scale-y") || section.contains("scaleY")
                || section.contains("scale-z") || section.contains("scaleZ");
        if (hasCustomScale) {
            line.setScale(sx, sy, sz);
        }

        if (section.contains("translation-x") || section.contains("translationX") ||
                section.contains("translation-y") || section.contains("translationY") ||
                section.contains("translation-z") || section.contains("translationZ")) {
            double tx = getCompatDouble(section, "translation-x", "translationX", 0);
            double ty = getCompatDouble(section, "translation-y", "translationY", 0);
            double tz = getCompatDouble(section, "translation-z", "translationZ", 0);
            line.setTranslation(tx, ty, tz);
        }

        if (section.contains("shadow-radius") || section.contains("shadowRadius")) {
            line.setShadowRadius((float) getCompatDouble(section, "shadow-radius", "shadowRadius", 0));
        }

        if (section.contains("shadow-strength") || section.contains("shadowStrength")) {
            line.setShadowStrength((float) getCompatDouble(section, "shadow-strength", "shadowStrength", 1.0));
        }

        if (section.contains("glow-color") || section.contains("glowColor")) {
            line.setGlowColor(getCompatInt(section, "glow-color", "glowColor", -1));
        }

        if (section.contains("chroma-background") || section.contains("chromaBackground")) {
            line.setChromaBackground(getCompatBoolean(section, "chroma-background", "chromaBackground", false));
        }

        if (section.contains("chroma-glow") || section.contains("chromaGlow")) {
            line.setChromaGlow(getCompatBoolean(section, "chroma-glow", "chromaGlow", false));
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

    private boolean saveYaml(YamlConfiguration yaml, File file) {
        try {
            yaml.save(file);
            return true;
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            plugin.getLogger().severe(() -> "Cannot save hologram file: " + errorMsg);
            return false;
        }
    }

    public void migrateFromOldFormat() {
        File oldDataDir = new File(plugin.getDataFolder(), "data");
        File oldFile = new File(oldDataDir, "holograms.yml");

        if (!oldFile.exists()) {
            return;
        }

        File[] existingFiles = hologramsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (existingFiles != null && existingFiles.length > 0) {
            return;
        }

        YamlConfiguration oldYaml = YamlConfiguration.loadConfiguration(oldFile);
        ConfigurationSection section = oldYaml.getConfigurationSection("holograms");
        if (section == null) {
            return;
        }

        Set<String> keys = section.getKeys(false);
        int count = 0;

        for (String id : keys) {
            ConfigurationSection holoSection = section.getConfigurationSection(id);
            if (holoSection == null) {
                continue;
            }

            Hologram hologram = loadHologram(id, holoSection);
            if (hologram == null) {
                continue;
            }

            if (save(hologram)) {
                count++;
            }
        }

        if (count > 0) {
            File backupFile = new File(oldDataDir, "holograms.yml.bak");
            if (oldFile.renameTo(backupFile)) {
                plugin.getLogger().info("Migrated " + count + " holograms from old format to new format");
            } else {
                plugin.getLogger().warning("Migrated " + count + " holograms but failed to rename old file to .bak");
            }
        }
    }

    @Override
    public void reload() {
    }
}
