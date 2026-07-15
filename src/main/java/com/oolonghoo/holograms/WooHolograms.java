package com.oolonghoo.holograms;

import com.oolonghoo.holograms.animation.AnimationManager;
import com.oolonghoo.holograms.api.WooHologramsAPI;
import com.oolonghoo.holograms.command.HologramCommand;
import com.oolonghoo.holograms.config.ConfigManager;
import com.oolonghoo.holograms.config.Messages;
import com.oolonghoo.holograms.gui.ChatInputManager;
import com.oolonghoo.holograms.gui.GuiManager;
import com.oolonghoo.holograms.hologram.HologramManager;
import com.oolonghoo.holograms.hook.PlaceholderHook;
import com.oolonghoo.holograms.listener.PacketListener;
import com.oolonghoo.holograms.listener.PlayerListener;
import com.oolonghoo.holograms.listener.WorldListener;
import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
import com.oolonghoo.holograms.util.Metrics;
import com.oolonghoo.holograms.nms.HologramRendererPool;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.HologramRendererFactoryImpl;
import com.oolonghoo.holograms.util.SchedulerUtil;
import com.oolonghoo.holograms.storage.HologramStorage;
import com.oolonghoo.holograms.storage.YamlHologramStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * WooHolograms 全息图插件主类
 * 
 */
public class WooHolograms extends JavaPlugin {

    private static WooHolograms instance;
    
    // 组件
    private ConfigManager configManager;
    private Messages messages;
    private HologramManager hologramManager;
    private AnimationManager animationManager;
    private GuiManager guiManager;
    private ChatInputManager chatInputManager;
    private PacketListener packetListener;
    private HologramStorage storage;
    private NmsHologramRendererFactory rendererFactory;
    private HologramRendererPool rendererPool;
    private PlaceholderHook placeholderHook;
    
    // 状态
    private boolean pluginEnabled = false;

    
    public WooHolograms() {
        instance = this;
    }
    
    @Override
    public void onEnable() {
        // 检查版本
        String mcVersion = getServer().getBukkitVersion();
        int dashIdx = mcVersion.indexOf('-');
        if (dashIdx > 0) mcVersion = mcVersion.substring(0, dashIdx);
        String[] parts = mcVersion.split("\\.");
        boolean supported = false;
        if (parts.length >= 2) {
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                // 1.21+ (旧格式) 或 26+ (新格式)
                if ((major == 1 && minor >= 21) || major >= 26) supported = true;
            } catch (NumberFormatException ignored) {}
        }
        if (!supported) {
            getLogger().severe("WooHolograms requires Paper/Folia 1.21+. Current server: " + mcVersion);
            getLogger().severe("Plugin will be disabled.");
            this.pluginEnabled = false;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化调度器工具
        SchedulerUtil.initialize(this);
        
        // 初始化配置
        configManager = new ConfigManager(this);
        configManager.initialize();
        
        // 初始化消息
        messages = new Messages(this);
        messages.initialize();
        
        // 初始化存储器
        storage = new YamlHologramStorage(this);
        ((YamlHologramStorage) storage).migrateFromOldFormat();
        
        // 初始化渲染器工厂
        EntityIdGenerator entityIdGenerator = new EntityIdGenerator();
        rendererFactory = new HologramRendererFactoryImpl(entityIdGenerator);
        
        // 初始化渲染器缓存池
        rendererPool = new HologramRendererPool(
            rendererFactory,
            configManager.getRendererPoolMaxSize(),
            configManager.isRendererPoolEnabled()
        );
        
        // 初始化全息图管理器
        hologramManager = new HologramManager(this, storage);
        
        // 初始化动画管理器
        animationManager = new AnimationManager(this);
        animationManager.loadAnimations();
        
        // 初始化 GUI 管理器
        guiManager = new GuiManager(this);
        
        // 初始化聊天输入管理器
        chatInputManager = new ChatInputManager(this);
        chatInputManager.register();
        
        // 初始化数据包监听器
        packetListener = new PacketListener(this);
        
        // 注册监听器
        registerListeners();
        
        // 注册命令
        HologramCommand hologramCommand = new HologramCommand(this);
        getCommand("hologram").setExecutor(hologramCommand);
        getCommand("wh").setAliases(Arrays.asList("holo", "wh"));
        getCommand("wooholograms").setExecutor(hologramCommand);
        getCommand("wooholograms").setDescription("全息图管理命令");
        
        // 初始化 API
        WooHologramsAPI.initialize(this);
        
        // 加载全息图
        hologramManager.loadAll();
        int hologramCount = hologramManager.getHologramCount();
        getLogger().info(() -> "已加载 " + hologramCount + " 个全息图");
        
        // 注册数据包监听器
        packetListener.register();
        
        // 检查 PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getLogger().info("PlaceholderAPI 扩展已注册");
        }
        
        // 检查 DecentHolograms 兼容层（provides 会让 getPlugin 返回自身，需排除）
        var dhPlugin = Bukkit.getPluginManager().getPlugin("DecentHolograms");
        if (dhPlugin != null && dhPlugin != this) {
            getLogger().info("检测到 DecentHolograms 已安装，DH 兼容层未激活");
        } else {
            getLogger().info("DecentHolograms 兼容层已激活（依赖 DH 的插件将使用 WooHolograms）");
        }

        // bStats 统计
        Metrics metrics = new Metrics(this, 31848);
        metrics.addCustomChart(new Metrics.SimplePie("hologram_count", () -> String.valueOf(hologramManager.getHologramCount())));

        pluginEnabled = true;
        String version = getPluginMeta().getVersion();
        getLogger().info(() -> "WooHolograms v" + version + " 已启用!");
    }
    
    @Override
    public void onDisable() {
        if (!pluginEnabled) {
            return;
        }
        
        // 同步保存所有 dirty 全息图（确保停服时数据不丢）
        try {
            hologramManager.flushAllSync();
        } catch (Exception e) {
            getLogger().severe("保存全息图时出错: " + e.getMessage());
        }
        
        // 注销数据包监听器
        try {
            packetListener.unregister();
        } catch (Exception e) {
            getLogger().severe("注销数据包监听器时出错: " + e.getMessage());
        }
        
        // 注销 PlaceholderAPI
        try {
            if (placeholderHook != null) {
                placeholderHook.unregister();
            }
        } catch (Exception e) {
            getLogger().severe("注销 PlaceholderAPI 时出错: " + e.getMessage());
        }
        
        // 清理 GUI
        try {
            guiManager.clear();
        } catch (Exception e) {
            getLogger().severe("清理 GUI 时出错: " + e.getMessage());
        }
        
        // 清理全息图
        try {
            hologramManager.clear();
        } catch (Exception e) {
            getLogger().severe("清理全息图时出错: " + e.getMessage());
        }
        
        // 清理动画
        try {
            animationManager.clear();
        } catch (Exception e) {
            getLogger().severe("清理动画时出错: " + e.getMessage());
        }
        
        // 清理渲染器缓存池
        try {
            if (rendererPool != null) {
                rendererPool.clear();
            }
        } catch (Exception e) {
            getLogger().severe("清理渲染器缓存池时出错: " + e.getMessage());
        }
        
        pluginEnabled = false;
        getLogger().info("WooHolograms 已禁用");
    }
    
    /*
     * Getter 方法
     */
    
    /**
     * 获取插件实例
     * 
     * @return 插件实例
     */
    @NotNull
    public static WooHolograms getInstance() {
        return instance;
    }
    
    /**
     * 获取配置管理器
     * 
     * @return 配置管理器
     */
    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取消息管理器
     * 
     * @return 消息管理器
     */
    @NotNull
    public Messages getMessages() {
        return messages;
    }
    
    /**
     * 获取全息图管理器
     * 
     * @return 全息图管理器
     */
    @NotNull
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    /**
     * 获取动画管理器
     * 
     * @return 动画管理器
     */
    @NotNull
    public AnimationManager getAnimationManager() {
        return animationManager;
    }
    
    /**
     * 获取 GUI 管理器
     * 
     * @return GUI 管理器
     */
    @NotNull
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    /**
     * 获取聊天输入管理器
     * 
     * @return 聊天输入管理器
     */
    @NotNull
    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
    
    /**
     * 获取数据包监听器
     * 
     * @return 数据包监听器
     */
    @NotNull
    public PacketListener getPacketListener() {
        return packetListener;
    }
    
    /**
     * 获取存储器
     * 
     * @return 存储器
     */
    @NotNull
    public HologramStorage getStorage() {
        return storage;
    }
    
    /**
     * 获取渲染器工厂
     * 
     * @return 渲染器工厂
     */
    @NotNull
    public NmsHologramRendererFactory getRendererFactory() {
        return rendererFactory;
    }
    
    /**
     * 获取渲染器缓存池
     * 
     * @return 渲染器缓存池
     */
    @NotNull
    public HologramRendererPool getRendererPool() {
        return rendererPool;
    }
    
    /**
     * 获取占位符钩子
     * 
     * @return 占位符钩子
     */
    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }
    
    /**
     * 检查插件是否启用
     * 
     * @return 是否启用
     */
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
    }
    
}
