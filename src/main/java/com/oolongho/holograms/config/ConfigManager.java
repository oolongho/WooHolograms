package com.oolongho.holograms.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.oolongho.holograms.WooHolograms;

/**
 * 配置管理器
 * 负责加载和管理插件配置
 * 
 * 
 */
public class ConfigManager {

    private final WooHolograms plugin;
    private FileConfiguration config;

    // 基本设置
    private boolean debug;
    private String language;
    private int autoSaveInterval;

    // 默认值设置
    private double defaultDisplayRange;
    private double defaultUpdateRange;
    private int defaultUpdateInterval;
    private double defaultLineHeight;
    private double defaultTextHeight;
    private double defaultItemHeight;
    private double defaultHeadHeight;
    private int defaultBackgroundAlpha;
    private int defaultBackgroundColor;
    private int defaultLineWidth;

    // Display Entity 默认值
    private float defaultScaleX;
    private float defaultScaleY;
    private float defaultScaleZ;
    private double defaultTranslationX;
    private double defaultTranslationY;
    private double defaultTranslationZ;
    private float defaultShadowRadius;
    private float defaultShadowStrength;
    private int defaultGlowColor;
    private boolean defaultChromaBackground;
    private boolean defaultChromaGlow;

    // 性能设置
    private long renderInterval;
    private long placeholderInterval;
    private int cacheSize;
    private int maxUpdatesPerTick;

    // 动画设置
    private boolean animationEnabled;
    private int animationFrameInterval;

    // 交互设置
    private boolean interactionEnabled;
    private int clickCooldown;

    // 限制设置
    private int maxHologramsPerWorld;
    private int maxLinesPerHologram;

    // 渲染器缓存池设置
    private boolean rendererPoolEnabled;
    private int rendererPoolMaxSize;
    
    // 安全设置
    private List<String> commandBlacklist;
    private int maxInputLength;

    public ConfigManager(WooHolograms plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化配置
     */
    public void initialize() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        loadValues();
    }

    /**
     * 加载配置值
     */
    private void loadValues() {
        // 基本设置
        debug = config.getBoolean("settings.debug", false);
        language = config.getString("settings.language", "zh-CN");
        autoSaveInterval = Math.max(1, config.getInt("settings.auto-save-interval", 300));

        // 默认值设置
        defaultDisplayRange = config.getDouble("defaults.display-range", 48.0);
        defaultUpdateRange = config.getDouble("defaults.update-range", 48.0);
        defaultUpdateInterval = config.getInt("defaults.update-interval", 0);
        defaultLineHeight = config.getDouble("defaults.line-height", 0.3);
        defaultTextHeight = config.getDouble("defaults.text-height", 0.3);
        defaultItemHeight = config.getDouble("defaults.item-height", 0.6);
        defaultHeadHeight = config.getDouble("defaults.head-height", 0.6);
        defaultBackgroundAlpha = config.getInt("defaults.default-background-alpha", 128);
        defaultBackgroundColor = config.getInt("defaults.default-background-color", 0);
        defaultLineWidth = config.getInt("defaults.default-line-width", 300);

        // Display Entity 默认值
        defaultScaleX = (float) config.getDouble("defaults.default-scale-x", 1.0);
        defaultScaleY = (float) config.getDouble("defaults.default-scale-y", 1.0);
        defaultScaleZ = (float) config.getDouble("defaults.default-scale-z", 1.0);
        defaultTranslationX = config.getDouble("defaults.default-translation-x", 0.0);
        defaultTranslationY = config.getDouble("defaults.default-translation-y", 0.0);
        defaultTranslationZ = config.getDouble("defaults.default-translation-z", 0.0);
        defaultShadowRadius = (float) config.getDouble("defaults.default-shadow-radius", 0.0);
        defaultShadowStrength = (float) config.getDouble("defaults.default-shadow-strength", 1.0);
        defaultGlowColor = config.getInt("defaults.default-glow-color", -1);
        defaultChromaBackground = config.getBoolean("defaults.default-chroma-background", false);
        defaultChromaGlow = config.getBoolean("defaults.default-chroma-glow", false);

        // 性能设置
        renderInterval = config.getLong("performance.render-interval", 2L);
        placeholderInterval = config.getLong("performance.placeholder-interval", 40L);
        cacheSize = Math.max(1, config.getInt("performance.cache-size", 500));
        maxUpdatesPerTick = config.getInt("performance.max-updates-per-tick", 50);

        // 动画设置
        animationEnabled = config.getBoolean("animation.enabled", true);
        animationFrameInterval = config.getInt("animation.frame-interval", 4);

        // 交互设置
        interactionEnabled = config.getBoolean("interaction.enabled", true);
        clickCooldown = config.getInt("interaction.click-cooldown", 500);

        // 限制设置
        maxHologramsPerWorld = Math.max(1, config.getInt("limits.max-holograms-per-world", 100));
        maxLinesPerHologram = Math.max(1, config.getInt("limits.max-lines-per-hologram", 20));

        // 渲染器缓存池设置
        rendererPoolEnabled = config.getBoolean("renderer-pool.enabled", true);
        rendererPoolMaxSize = config.getInt("renderer-pool.max-size", 100);
        
        // 安全设置
        commandBlacklist = config.getStringList("security.command-blacklist");
        if (commandBlacklist.isEmpty()) {
            commandBlacklist = new ArrayList<>(java.util.Arrays.asList(
                    "op", "deop", "stop", "reload", "save-all", "save-off", "save-on",
                    "whitelist", "ban", "ban-ip", "pardon", "pardon-ip", "kick",
                    "execute", "function", "debug", "perf"
            ));
        }
        maxInputLength = config.getInt("security.max-input-length", 256);
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadValues();
    }

    /**
     * 保存配置
     */
    public void save() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe(() -> "无法保存配置文件: " + e.getMessage());
        }
    }

    /*
     * Getter 方法
     */

    public boolean isDebug() {
        return debug;
    }

    public String getLanguage() {
        return language;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public double getDefaultDisplayRange() {
        return defaultDisplayRange;
    }

    public double getDefaultUpdateRange() {
        return defaultUpdateRange;
    }

    public int getDefaultUpdateInterval() {
        return defaultUpdateInterval;
    }

    public double getDefaultLineHeight() {
        return defaultLineHeight;
    }

    public double getDefaultTextHeight() {
        return defaultTextHeight;
    }

    public double getDefaultItemHeight() {
        return defaultItemHeight;
    }

    public double getDefaultHeadHeight() {
        return defaultHeadHeight;
    }

    public int getDefaultBackgroundAlpha() {
        return defaultBackgroundAlpha;
    }

    public int getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public int getDefaultLineWidth() {
        return defaultLineWidth;
    }

    public float getDefaultScaleX() {
        return defaultScaleX;
    }

    public float getDefaultScaleY() {
        return defaultScaleY;
    }

    public float getDefaultScaleZ() {
        return defaultScaleZ;
    }

    public double getDefaultTranslationX() {
        return defaultTranslationX;
    }

    public double getDefaultTranslationY() {
        return defaultTranslationY;
    }

    public double getDefaultTranslationZ() {
        return defaultTranslationZ;
    }

    public float getDefaultShadowRadius() {
        return defaultShadowRadius;
    }

    public float getDefaultShadowStrength() {
        return defaultShadowStrength;
    }

    public int getDefaultGlowColor() {
        return defaultGlowColor;
    }

    public boolean isDefaultChromaBackground() {
        return defaultChromaBackground;
    }

    public boolean isDefaultChromaGlow() {
        return defaultChromaGlow;
    }

    public long getRenderInterval() {
        return renderInterval;
    }

    public long getPlaceholderInterval() {
        return placeholderInterval;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getMaxUpdatesPerTick() {
        return maxUpdatesPerTick;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public int getAnimationFrameInterval() {
        return animationFrameInterval;
    }

    public boolean isInteractionEnabled() {
        return interactionEnabled;
    }

    public int getClickCooldown() {
        return clickCooldown;
    }

    public int getMaxHologramsPerWorld() {
        return maxHologramsPerWorld;
    }

    public int getMaxLinesPerHologram() {
        return maxLinesPerHologram;
    }

    public boolean isRendererPoolEnabled() {
        return rendererPoolEnabled;
    }

    public int getRendererPoolMaxSize() {
        return rendererPoolMaxSize;
    }
    
    public List<String> getCommandBlacklist() {
        return commandBlacklist;
    }
    
    public int getMaxInputLength() {
        return maxInputLength;
    }
    
    public boolean isCommandBlacklisted(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        String cmd = command.toLowerCase().split(" ")[0].replace("/", "");
        return commandBlacklist.contains(cmd);
    }

    /**
     * 获取更新间隔（兼容旧方法）
     * @return 更新间隔（tick）
     */
    public long getUpdateInterval() {
        return renderInterval;
    }

    /**
     * 获取可视距离（兼容旧方法）
     * @return 可视距离
     */
    public double getViewDistance() {
        return defaultDisplayRange;
    }

    /**
     * 获取原始配置
     * @return 配置对象
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 是否启用 DecentHolograms 伪装层
     * @return true 表示其他插件可将 WooHolograms 识别为 DecentHolograms
     */
    public boolean isDecentHologramsCompatEnabled() {
        return config.getBoolean("compat.decentholograms", true);
    }
}
