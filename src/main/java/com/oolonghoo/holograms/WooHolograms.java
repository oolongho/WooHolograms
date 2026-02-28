package com.oolonghoo.holograms;

import com.oolonghoo.holograms.animation.AnimationManager;
import com.oolonghoo.holograms.api.WooHologramsAPI;
import com.oolonghoo.holograms.command.HologramCommand;
import com.oolonghoo.holograms.config.ConfigManager;
import com.oolonghoo.holograms.config.Messages;
import com.oolonghoo.holograms.gui.ChatInputManager;
import com.oolonghoo.holograms.gui.GuiListener;
import com.oolonghoo.holograms.gui.GuiManager;
import com.oolonghoo.holograms.hologram.HologramManager;
import com.oolonghoo.holograms.hook.PlaceholderHook;
import com.oolonghoo.holograms.listener.PacketListener;
import com.oolonghoo.holograms.listener.PlayerListener;
import com.oolonghoo.holograms.listener.WorldListener;
import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.HologramRendererFactoryImpl;
import com.oolonghoo.holograms.storage.HologramStorage;
import com.oolonghoo.holograms.storage.YamlHologramStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

import java.util.logging.Logger;

/**
 * WooHolograms 全息图插件主类
 * 
 * @author oolongho
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
    private PlaceholderHook placeholderHook;
    
    // 状态
    private boolean pluginEnabled = false;

    
    public WooHolograms() {
        instance = this;
    }
    
    @Override
    public void onEnable() {
        // 检查版本
        if (!checkVersion()) {
            getLogger().severe("========================================");
            getLogger().severe("此插件仅支持 Minecraft 1.21+");
            getLogger().severe("========================================");
            this.pluginEnabled = false;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 初始化配置
        configManager = new ConfigManager(this);
        configManager.initialize();
        
        // 初始化消息
        messages = new Messages(this);
        messages.initialize();
        
        // 初始化存储器
        storage = new YamlHologramStorage(this);
        
        // 初始化渲染器工厂
        EntityIdGenerator entityIdGenerator = new EntityIdGenerator();
        rendererFactory = new HologramRendererFactoryImpl(entityIdGenerator);
        
        // 初始化全息图管理器
        hologramManager = new HologramManager(this, storage);
        
        // 初始化动画管理器
        animationManager = new AnimationManager(this);
        animationManager.loadAnimations();
        
        // 初始化 GUI 管理器
        guiManager = new GuiManager(this);
        
        // 初始化聊天输入管理器
        chatInputManager = new ChatInputManager(this);
        
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
        
        // 注册数据包监听器
        packetListener.register();
        
        // 检查 PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
            getLogger().info("PlaceholderAPI 扩展已注册");
        }
        
        pluginEnabled = true;
        getLogger().info("WooHolograms 已启用");
    }
    
    @Override
    public void onDisable() {
        if (!pluginEnabled) {
            return;
        }
        
        // 保存所有全息图
        hologramManager.saveAll();
        
        // 注销数据包监听器
        packetListener.unregister();
        
        // 注销 PlaceholderAPI
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        
        // 清理 GUI
        guiManager.clear();
        
        // 清理全息图
        hologramManager.clear();
        
        // 清理动画
        animationManager.clear();
        
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
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
    }
    
    private boolean checkVersion() {
        String bukkitVersion = getServer().getBukkitVersion();
        String mcVersion = bukkitVersion.split("-", 2)[0];
        String[] parts = mcVersion.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int minor = Integer.parseInt(parts[1]);
            return minor >= 21;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
    
    private void warn(String message) {
        getLogger().warning(message);
    }
}
