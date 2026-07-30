package com.oolongho.holograms.config;

import com.oolongho.holograms.WooHolograms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息管理器
 * 负责加载和管理语言消息，统一使用 MiniMessage + Adventure Component
 *
 * <p>消息风格规范：
 * <ul>
 *   <li>命令反馈消息体中显式书写 {prefix} 占位符，由 {@link #get(String, String...)} 自动替换</li>
 *   <li>GUI 标题、装饰性文本不带 {prefix}</li>
 *   <li>所有颜色使用 MiniMessage 标签（&lt;color:#xxxxxx&gt;），不再使用 Legacy &amp;c 等代码</li>
 * </ul>
 */
public class Messages {

    /** prefix 占位符，写在消息体中由 get 自动替换 */
    private static final String PREFIX_PLACEHOLDER = "{prefix}";

    /** prefix 键名，用于获取前缀值（即使 replacements 中传 prefix 也会被忽略） */
    private static final String PREFIX_KEY = "prefix";

    private final WooHolograms plugin;
    private final MiniMessage miniMessage;
    private final Map<String, String> messages;
    private FileConfiguration langConfig;
    private File langFile;

    public Messages(WooHolograms plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = new ConcurrentHashMap<>();
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
        if (!defaultLangFile.exists() && resourceExists(defaultFile)) {
            plugin.saveResource(defaultFile, false);
        }

        langFile = new File(langFolder, language + ".yml");

        if (!langFile.exists()) {
            String resourcePath = "lang/" + language + ".yml";
            if (resourceExists(resourcePath)) {
                plugin.saveResource(resourcePath, false);
            } else {
                plugin.getLogger().warning("语言文件 " + language + ".yml 不存在，使用默认语言 zh-CN");
                langFile = new File(langFolder, "zh-CN.yml");
                if (!langFile.exists() && resourceExists(defaultFile)) {
                    plugin.saveResource(defaultFile, false);
                }
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // try-with-resources 确保 InputStream/Reader 关闭（loadConfiguration 不负责关闭）
        try (InputStream defaultStream = plugin.getResource("lang/zh-CN.yml");
             InputStreamReader reader = defaultStream == null ? null
                     : new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
            if (reader != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                langConfig.setDefaults(defaultConfig);
            }
        } catch (IOException e) {
            plugin.getLogger().warning(() -> "无法加载内置默认语言文件: " + e.getMessage());
        }

        loadMessages();
    }

    /**
     * 检查 jar 内是否存在指定资源（确保 InputStream 关闭，避免资源泄漏）。
     *
     * @param path 资源路径
     * @return 存在返回 true
     */
    private boolean resourceExists(String path) {
        try (InputStream is = plugin.getResource(path)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 加载消息到内存
     * 仅缓存字符串叶子节点，过滤掉 section 节点（避免缓存整个 lang 树）
     */
    private void loadMessages() {
        messages.clear();

        for (String key : langConfig.getKeys(true)) {
            // 跳过顶层键（不含 "."）和 section 节点（非字符串值）
            if (!key.contains(".")) continue;
            if (!langConfig.isString(key)) continue;
            String value = langConfig.getString(key);
            if (value != null) {
                messages.put(key, value);
            }
        }
    }

    /**
     * 获取消息原始字符串（未经占位符替换与 MiniMessage 解析）
     * 用于 PlaceholderAPI / 日志等需要纯文本的场景
     *
     * @param key 消息键
     * @return 原始字符串，找不到返回 key 本身
     */
    @NotNull
    public String getRaw(String key) {
        String message = messages.get(key);
        if (message == null) {
            message = langConfig.getString(key);
        }
        return message != null ? message : key;
    }

    /**
     * 获取解析后的 Component
     * <p>内部流程：
     * <ol>
     *   <li>用 replacements（键值对序列）替换 {xxx} 占位符（传 prefix 会被忽略并告警）</li>
     *   <li>自动替换 {prefix} 为 prefix 键值</li>
     *   <li>用 MiniMessage 解析为 Component</li>
     * </ol>
     *
     * @param key 消息键
     * @param replacements 占位符键值对（{k1, v1, k2, v2, ...}）
     * @return 解析后的 Component，解析失败降级为 Component.text(raw)
     */
    @NotNull
    public Component get(String key, String... replacements) {
        String raw = getRaw(key);
        String parsed = applyReplacements(key, raw, replacements);
        return parseComponent(key, parsed);
    }

    /**
     * 获取占位符替换后的字符串（不进行 MiniMessage 解析）
     * <p>用于需要字符串而非 Component 的场景，如 {@code GuiButton.name(String)} /
     * {@code GuiButton.lore(List<String)}}（GuiButton 内部会自行用 MiniMessage 解析）。
     *
     * @param key 消息键
     * @param replacements 占位符键值对（{k1, v1, k2, v2, ...}）
     * @return 替换占位符后的字符串（含 MiniMessage 标签，未解析）
     */
    @NotNull
    public String getString(String key, String... replacements) {
        String raw = getRaw(key);
        return applyReplacements(key, raw, replacements);
    }

    /**
     * 用 MiniMessage 解析任意字符串为 Component（不经过语言键查找与 {prefix} 替换）
     * 用于解析非语言键的 MiniMessage 字符串（如动态生成的性能报告、玩家输入的动作消息）
     *
     * @param input MiniMessage 格式字符串
     * @return 解析后的 Component，解析失败降级为 Component.text(input)
     */
    @NotNull
    public Component parse(@NotNull String input) {
        if (input.isEmpty()) {
            return Component.empty();
        }
        try {
            return miniMessage.deserialize(input);
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "MiniMessage 解析失败 (parse): " + e.getMessage()
                            + " | raw=" + input + "，已降级为纯文本。");
            return Component.text(input);
        }
    }

    /**
     * 发送消息给 CommandSender（自动调用 {@link #get(String, String...)} 并 sendMessage）
     *
     * @param sender 接收者
     * @param key 消息键
     * @param replacements 占位符键值对
     */
    public void send(@NotNull CommandSender sender, String key, String... replacements) {
        sender.sendMessage(get(key, replacements));
    }

    /**
     * 发送消息给 Player（Paper 的 Player 已实现 Audience，重载以提高可见性）
     *
     * @param player 玩家
     * @param key 消息键
     * @param replacements 占位符键值对
     */
    public void send(@NotNull Player player, String key, String... replacements) {
        player.sendMessage(get(key, replacements));
    }

    /**
     * @deprecated 前缀由消息体中的 {prefix} 占位符决定，直接使用 {@link #get(String, String...)}
     * 或 {@link #send(CommandSender, String, String...)}。该方法保留用于兼容期。
     *
     * @param key 消息键
     * @param replacements 占位符键值对
     * @return 解析后的 Component（与 {@link #get(String, String...)} 完全一致）
     */
    @Deprecated
    @NotNull
    public Component getWithPrefix(String key, String... replacements) {
        return get(key, replacements);
    }

    /**
     * 应用占位符替换
     * <p>规则：
     * <ul>
     *   <li>先按 replacements（键值对）替换 {xxx} 占位符</li>
     *   <li>若 replacements 中传入了 prefix 键，则忽略并打印告警</li>
     *   <li>最后自动替换 {prefix} 为 prefix 键值</li>
     * </ul>
     */
    private String applyReplacements(String key, String raw, String... replacements) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String result = raw;

        if (replacements != null && replacements.length > 0) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                String placeholder = replacements[i];
                String value = replacements[i + 1];
                if (placeholder == null) continue;

                if (PREFIX_KEY.equalsIgnoreCase(placeholder)) {
                    plugin.getLogger().warning(
                            "Messages.get('" + key + "') 被显式传入 prefix 替换值，已忽略。"
                                    + "请在消息体中使用 {prefix} 占位符，不要在代码中传 prefix。");
                    continue;
                }
                result = result.replace("{" + placeholder + "}", value == null ? "" : value);
            }
        }

        // 自动替换 {prefix}（即使 replacements 未传）
        if (result.indexOf('{') >= 0 && result.contains(PREFIX_PLACEHOLDER)) {
            String prefix = getRaw(PREFIX_KEY);
            result = result.replace(PREFIX_PLACEHOLDER, prefix);
        }

        return result;
    }

    /**
     * 用 MiniMessage 解析字符串为 Component，失败时降级为 Component.text(raw) 并告警
     */
    @NotNull
    private Component parseComponent(String key, String parsed) {
        if (parsed == null || parsed.isEmpty()) {
            return Component.empty();
        }
        try {
            return miniMessage.deserialize(parsed);
        } catch (Exception e) {
            plugin.getLogger().warning(
                    "MiniMessage 解析失败 key='" + key + "': " + e.getMessage()
                            + " | raw=" + parsed + "，已降级为纯文本。");
            return Component.text(parsed);
        }
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
