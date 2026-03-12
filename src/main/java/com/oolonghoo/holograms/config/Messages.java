package com.oolonghoo.holograms.config;

import com.oolonghoo.holograms.WooHolograms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器
 * 负责加载和管理语言消息
 * 
 */
public class Messages {

    private final WooHolograms plugin;
    private final Map<String, String> messages;
    private FileConfiguration langConfig;
    private File langFile;

    public Messages(WooHolograms plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    /**
     * 初始化消息
     */
    public void initialize() {
        loadLanguage();
    }

    /**
     * 加载语言文件
     */
    private void loadLanguage() {
        String language = plugin.getConfigManager().getLanguage();
        
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        String defaultFile = "lang/zh-CN.yml";
        File defaultLangFile = new File(langFolder, "zh-CN.yml");
        if (!defaultLangFile.exists() && plugin.getResource(defaultFile) != null) {
            plugin.saveResource(defaultFile, false);
        }
        
        langFile = new File(langFolder, language + ".yml");
        
        if (!langFile.exists()) {
            String resourcePath = "lang/" + language + ".yml";
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
            } else {
                plugin.getLogger().warning("语言文件 " + language + ".yml 不存在，使用默认语言 zh-CN");
                langFile = new File(langFolder, "zh-CN.yml");
                if (!langFile.exists() && plugin.getResource(defaultFile) != null) {
                    plugin.saveResource(defaultFile, false);
                }
            }
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        InputStream defaultStream = plugin.getResource("lang/zh-CN.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultConfig);
        }
        
        loadMessages();
    }

    /**
     * 加载消息到内存
     */
    private void loadMessages() {
        messages.clear();
        
        for (String key : langConfig.getKeys(true)) {
            if (!key.contains(".")) continue;
            String value = langConfig.getString(key);
            if (value != null) {
                messages.put(key, value);
            }
        }
    }

    /**
     * 获取消息
     * @param key 消息键
     * @return 消息内容
     */
    public String get(String key) {
        String message = messages.get(key);
        if (message == null) {
            message = langConfig.getString(key);
        }
        return message != null ? message : key;
    }

    /**
     * 获取消息（带前缀）
     * @param key 消息键
     * @return 带前缀的消息内容
     */
    public String getWithPrefix(String key) {
        String prefix = get("prefix");
        String message = get(key);
        return prefix + message;
    }
    
    /**
     * 获取消息（带前缀和替换）
     * @param key 消息键
     * @param replacements 替换内容（键值对）
     * @return 带前缀和替换的消息内容
     */
    public String getWithPrefix(String key, String... replacements) {
        String prefix = get("prefix");
        String message = get(key, replacements);
        return prefix + message;
    }

    /**
     * 获取消息并替换占位符
     * @param key 消息键
     * @param replacements 替换内容（键值对）
     * @return 替换后的消息
     */
    public String get(String key, String... replacements) {
        String message = get(key);
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        
        return message;
    }

    /**
     * 重新加载消息
     */
    public void reload() {
        loadLanguage();
    }

    /**
     * 获取原始语言配置
     * @return 语言配置
     */
    public FileConfiguration getLangConfig() {
        return langConfig;
    }
}
