package com.oolonghoo.holograms.listener;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.api.event.HologramClickEvent;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据包监听器
 * 监听玩家交互数据包以检测全息图点击
 * 使用反射实现版本兼容
 * 
 * @author oolongho
 */
public class PacketListener {

    private final WooHolograms plugin;
    private final Map<Player, Channel> playerChannels;
    
    // 反射缓存
    private Class<?> craftPlayerClass;
    private Class<?> serverPlayerClass;
    private Class<?> connectionClass;
    private Class<?> packetClass;
    private Method getHandleMethod;
    private Method getConnectionMethod;
    private Method getChannelMethod;
    private Field entityIdField;
    private Field actionField;

    public PacketListener(WooHolograms plugin) {
        this.plugin = plugin;
        this.playerChannels = new HashMap<>();
        initReflection();
    }
    
    /**
     * 初始化反射
     */
    private void initReflection() {
        try {
            String version;
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            
            if (packageName.contains(".v")) {
                String[] parts = packageName.split("\\.");
                version = parts.length > 3 ? parts[3] : "";
            } else {
                version = "";
            }
            
            if (!version.isEmpty()) {
                craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            } else {
                craftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            }
            
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
            
            serverPlayerClass = getHandleMethod.getReturnType();
            
            try {
                connectionClass = serverPlayerClass.getField("connection").getType();
                getConnectionMethod = null;
            } catch (NoSuchFieldException e) {
                try {
                    connectionClass = serverPlayerClass.getField("b").getType();
                } catch (NoSuchFieldException e2) {
                    for (Method m : serverPlayerClass.getMethods()) {
                        if (m.getReturnType().getSimpleName().contains("Connection") || 
                            m.getReturnType().getSimpleName().contains("PlayerConnection")) {
                            getConnectionMethod = m;
                            connectionClass = m.getReturnType();
                            break;
                        }
                    }
                }
            }
            
            if (connectionClass != null) {
                for (Field f : connectionClass.getDeclaredFields()) {
                    if (f.getType() == Channel.class) {
                        f.setAccessible(true);
                        getChannelMethod = null;
                        break;
                    }
                }
            }
            
            try {
                packetClass = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket");
            } catch (ClassNotFoundException e) {
                try {
                    packetClass = Class.forName("net.minecraft.server.network.PacketListenerInUseEntity");
                } catch (ClassNotFoundException e2) {
                    try {
                        packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity");
                    } catch (ClassNotFoundException e3) {
                        plugin.getLogger().warning("无法找到交互数据包类，点击功能可能无法正常工作");
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("初始化反射失败: " + e.getMessage());
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注册监听器
     */
    public void register() {
        // 为所有在线玩家注册
        for (Player player : Bukkit.getOnlinePlayers()) {
            inject(player);
        }
    }

    /**
     * 注销监听器
     */
    public void unregister() {
        for (Player player : playerChannels.keySet()) {
            uninject(player);
        }
        playerChannels.clear();
    }

    /**
     * 为玩家注入数据包处理器
     * 
     * @param player 玩家
     */
    public void inject(Player player) {
        try {
            Channel channel = getChannel(player);
            
            if (channel != null && !playerChannels.containsKey(player)) {
                ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (packetClass != null && packetClass.isInstance(msg)) {
                            if (handleUseEntity(player, msg)) {
                                return; // 取消数据包
                            }
                        }
                        super.channelRead(ctx, msg);
                    }
                };
                
                channel.pipeline().addBefore("packet_handler", "wooholograms_packet", handler);
                playerChannels.put(player, channel);
            }
        } catch (Exception e) {
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("无法为玩家 " + player.getName() + " 注入数据包监听器: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取玩家的 Channel
     */
    private Channel getChannel(Player player) {
        try {
            if (craftPlayerClass == null || getHandleMethod == null) {
                return null;
            }
            
            Object craftPlayer = craftPlayerClass.cast(player);
            Object entityPlayer = getHandleMethod.invoke(craftPlayer);
            
            if (entityPlayer == null) {
                return null;
            }
            
            Object connection = null;
            
            // 尝试获取连接
            if (getConnectionMethod != null) {
                connection = getConnectionMethod.invoke(entityPlayer);
            } else {
                // 尝试直接访问字段
                for (Field f : entityPlayer.getClass().getDeclaredFields()) {
                    if (f.getType().getSimpleName().contains("Connection") || 
                        f.getType().getSimpleName().contains("PlayerConnection")) {
                        f.setAccessible(true);
                        connection = f.get(entityPlayer);
                        break;
                    }
                }
            }
            
            if (connection == null) {
                return null;
            }
            
            // 获取 Channel
            for (Field f : connection.getClass().getDeclaredFields()) {
                if (f.getType() == Channel.class) {
                    f.setAccessible(true);
                    return (Channel) f.get(connection);
                }
            }
            
        } catch (Exception e) {
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("获取 Channel 失败: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 为玩家移除数据包处理器
     * 
     * @param player 玩家
     */
    public void uninject(Player player) {
        Channel channel = playerChannels.remove(player);
        if (channel != null && channel.pipeline().get("wooholograms_packet") != null) {
            try {
                channel.pipeline().remove("wooholograms_packet");
            } catch (Exception e) {
                if (plugin.getConfigManager().isDebug()) {
                    plugin.getLogger().warning("Failed to remove packet handler for player " + player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * 处理实体交互数据包
     * 
     * @param player 玩家
     * @param packet 数据包
     * @return 是否取消数据包
     */
    private boolean handleUseEntity(Player player, Object packet) {
        try {
            // 获取实体 ID
            int entityId = getEntityIdFromPacket(packet);
            
            if (entityId < 0) {
                return false;
            }
            
            // 获取点击类型
            ClickType clickType = getClickType(packet);
            
            // 查找对应的全息图
            Hologram hologram = findHologramByEntityId(player, entityId);
            if (hologram == null) {
                return false;
            }
            
            // 触发事件
            HologramClickEvent event = new HologramClickEvent(hologram, player, clickType);
            Bukkit.getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                return true;
            }
            
            // 查找被点击的行
            HologramPage page = hologram.getPageByEntityId(entityId);
            if (page != null) {
                HologramLine line = page.getLineByEntityId(entityId);
                if (line != null && line.hasActions()) {
                    // 执行行级别动作
                    line.executeActions(player, clickType);
                    return false;
                }
            }
            
            // 执行页面级别动作
            hologram.executeActions(player, clickType);
            
            return false;
        } catch (Exception e) {
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 从数据包获取实体 ID
     * 
     * @param packet 数据包
     * @return 实体 ID
     */
    private int getEntityIdFromPacket(Object packet) {
        try {
            // 尝试不同的字段名
            String[] fieldNames = {"a", "entityId", "b", "id"};
            
            for (String fieldName : fieldNames) {
                try {
                    Field field = packet.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(packet);
                    
                    if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value != null) {
                        // 可能是一个包含 entityId 的对象
                        try {
                            Method getEntityId = value.getClass().getMethod("getEntityId");
                            return (Integer) getEntityId.invoke(value);
                        } catch (NoSuchMethodException ignored) {}
                    }
                } catch (NoSuchFieldException ignored) {}
            }
            
            // 尝试通过方法获取
            for (Method m : packet.getClass().getDeclaredMethods()) {
                if (m.getReturnType() == int.class || m.getReturnType() == Integer.class) {
                    m.setAccessible(true);
                    try {
                        Object result = m.invoke(packet);
                        if (result instanceof Integer) {
                            return (Integer) result;
                        }
                    } catch (Exception ignored) {}
                }
            }
            
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Failed to get entity ID from packet: " + e.getMessage());
            }
        }
        return -1;
    }

    /**
     * 获取点击类型
     * 
     * @param packet 数据包
     * @return 点击类型
     */
    private ClickType getClickType(Object packet) {
        try {
            // 尝试获取 action 字段
            String[] fieldNames = {"b", "action", "c"};
            
            for (String fieldName : fieldNames) {
                try {
                    Field field = packet.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object action = field.get(packet);
                    
                    if (action != null) {
                        String actionName = action.toString();
                        
                        if (actionName.contains("ATTACK") || actionName.contains("attack")) {
                            return ClickType.LEFT;
                        } else if (actionName.contains("INTERACT") || actionName.contains("interact")) {
                            return ClickType.RIGHT;
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // 继续尝试下一个字段名
                }
            }
            
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Failed to get click type from packet: " + e.getMessage());
            }
        }
        
        return ClickType.RIGHT;
    }

    /**
     * 根据实体 ID 查找全息图
     * 
     * @param player 玩家
     * @param entityId 实体 ID
     * @return 全息图
     */
    private Hologram findHologramByEntityId(Player player, int entityId) {
        for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
            if (!hologram.isEnabled() || !hologram.isVisible(player)) {
                continue;
            }
            
            // 检查实体 ID 是否属于此全息图
            if (hologram.hasEntity(entityId)) {
                return hologram;
            }
        }
        return null;
    }
}
