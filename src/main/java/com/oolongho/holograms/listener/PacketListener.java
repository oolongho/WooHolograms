package com.oolongho.holograms.listener;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.api.event.HologramClickEvent;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.nms.versions.FriendlyByteBufWrapper;
import com.oolongho.holograms.util.SchedulerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据包监听器
 * 监听玩家交互数据包以检测全息图点击
 * 使用直接 NMS 引用实现
 */
public class PacketListener {

    private static final String HANDLER_NAME = "wooholograms_packet";

    private final WooHolograms plugin;
    private final Map<Player, Channel> playerChannels;

    public PacketListener(WooHolograms plugin) {
        this.plugin = plugin;
        this.playerChannels = new ConcurrentHashMap<>();
    }

    /**
     * 注册监听器，为所有在线玩家注入数据包处理器
     */
    public void register() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            inject(player);
        }
    }

    /**
     * 注销监听器，移除所有玩家的数据包处理器
     */
    public void unregister() {
        for (Player player : new ArrayList<>(playerChannels.keySet())) {
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
        Channel channel = getChannel(player);
        if (channel == null) {
            return;
        }

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof ServerboundInteractPacket packet) {
                    if (handleInteractPacket(player, packet)) {
                        return; // 取消数据包
                    }
                }
                super.channelRead(ctx, msg);
            }
        };

        // 在 Netty EventLoop 中执行管道操作和 map 记录，确保线程安全
        if (channel.eventLoop().inEventLoop()) {
            injectHandler(player, channel, handler);
        } else {
            channel.eventLoop().execute(() -> injectHandler(player, channel, handler));
        }
    }

    /**
     * 在管道中注入处理器，成功后记录到 playerChannels
     * 幂等：若处理器已存在则跳过
     */
    private void injectHandler(Player player, Channel channel, ChannelDuplexHandler handler) {
        try {
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                playerChannels.putIfAbsent(player, channel);
                return;
            }
            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
            playerChannels.putIfAbsent(player, channel);
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning(() -> "注入数据包处理器失败: " + e.getMessage());
            }
        }
    }

    /**
     * 为玩家移除数据包处理器
     * 在 EventLoop 中执行 map 移除和 handler 移除，避免与 inject 竞态
     *
     * @param player 玩家
     */
    public void uninject(Player player) {
        Channel channel = playerChannels.get(player);
        if (channel == null) {
            return;
        }

        if (channel.eventLoop().inEventLoop()) {
            playerChannels.remove(player);
            removeHandler(channel);
        } else {
            channel.eventLoop().execute(() -> {
                playerChannels.remove(player);
                removeHandler(channel);
            });
        }
    }

    /**
     * 从管道中移除处理器
     */
    private void removeHandler(Channel channel) {
        try {
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }
        } catch (RuntimeException e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning(() -> "移除数据包处理器失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取玩家的 Netty Channel
     * 通过 CraftPlayer -> ServerPlayer -> ServerGamePacketListenerImpl -> Connection -> Channel 链路获取
     */
    private Channel getChannel(Player player) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;
            if (connection == null) {
                return null;
            }
            // ServerCommonPacketListenerImpl 持有 Connection 字段
            Connection nmConnection = ((ServerCommonPacketListenerImpl) connection).connection;
            return nmConnection.channel;
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning(() -> "获取 Channel 失败: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * 处理实体交互数据包
     * 使用 STREAM_CODEC 解码数据包获取实体 ID、动作类型和交互坐标
     * 仅在 Netty 线程提取数据，所有 Bukkit API 调用调度到主线程
     *
     * 数据包格式:
     *   VarInt entityId
     *   VarInt actionOrdinal (0=INTERACT, 1=ATTACK, 2=INTERACT_AT)
     *   [Float targetX, Float targetY, Float targetZ] — 仅 INTERACT_AT
     *   [VarInt hand] — 仅 INTERACT 和 INTERACT_AT
     *   Boolean sneaking
     *
     * @param player 玩家
     * @param packet 交互数据包
     * @return 是否取消数据包
     */
    private boolean handleInteractPacket(Player player, ServerboundInteractPacket packet) {
        try {
            FriendlyByteBufWrapper buf = FriendlyByteBufWrapper.getInstance();
            ServerboundInteractPacket.STREAM_CODEC.encode(buf.getSerializer(), packet);

            int entityId = buf.readVarInt();
            int actionOrdinal = buf.readVarInt();

            if (entityId < 0) {
                return false;
            }

            // INTERACT_AT (2) 包含相对于实体原点的点击坐标
            Float hitY = null;
            if (actionOrdinal == 2) {
                buf.readFloat(); // targetX - 暂不使用
                hitY = buf.readFloat();
                buf.readFloat(); // targetZ - 暂不使用
            }

            // 节点 2: debug 日志 - 方法入口
            final int actionOrdinalFinal = actionOrdinal;
            final Float hitYFinal = hitY;
            plugin.debug(() -> String.format(
                    "[Node2 handleInteractPacket] player=%s, entityId=%d, actionOrdinal=%d, hitY=%s",
                    player.getName(), entityId, actionOrdinalFinal,
                    hitYFinal == null ? "null" : String.format("%.3f", hitYFinal)));

            // 切换到主线程处理点击逻辑（mapActionToClickType 需要调用 Bukkit API）
            final Float finalHitY = hitY;
            SchedulerUtil.runTask(player, () -> {
                ClickType clickType = mapActionToClickType(player, actionOrdinal);
                handleClick(player, entityId, clickType, finalHitY);
            });

            return false;
        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning(() -> "处理交互数据包时出错: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * 将 NMS 动作序号映射为 ClickType
     * 参考: https://minecraft.wiki/w/Java_Edition_protocol#Interact
     *
     * @param player        玩家（用于判断是否潜行）
     * @param actionOrdinal 动作序号 (0=INTERACT, 1=ATTACK, 2=INTERACT_AT)
     * @return 点击类型
     */
    private ClickType mapActionToClickType(Player player, int actionOrdinal) {
        return switch (actionOrdinal) {
            case 1 -> player.isSneaking() ? ClickType.SHIFT_LEFT : ClickType.LEFT;
            case 0, 2 -> player.isSneaking() ? ClickType.SHIFT_RIGHT : ClickType.RIGHT;
            default -> ClickType.RIGHT;
        };
    }

    /**
     * 处理全息图点击
     * 在主线程执行
     *
     * @param player    玩家
     * @param entityId  实体 ID
     * @param clickType 点击类型
     * @param hitY      点击位置相对于实体原点的 Y 坐标（仅 INTERACT_AT 时有值）
     */
    private void handleClick(Player player, int entityId, ClickType clickType, Float hitY) {
        if (!player.isOnline()) {
            return;
        }

        Hologram hologram = findHologramByEntityId(player, entityId);
        if (hologram == null) {
            // 节点 3: debug 日志 - hologram 未找到
            plugin.debug(() -> String.format(
                    "[Node3 handleClick] player=%s, entityId=%d, clickType=%s, hologramFound=false",
                    player.getName(), entityId, clickType));
            return;
        }

        // 检查是否禁用动作
        if (hologram.hasFlag(com.oolongho.holograms.hologram.EnumFlag.DISABLE_ACTIONS)) {
            return;
        }

        if (plugin.getHologramManager().checkAndSetCooldown(player)) {
            return;
        }

        HologramPage page = hologram.getPageByEntityId(entityId);
        // 当 page 为 null 时，entityId 可能属于 ClickableHologramRenderer（HEAD/SMALLHEAD/ICON/ENTITY 类型行）
        // ClickableHologramRenderer 的实体 ID 不在 HologramPage.hasEntity() 中，但在 Hologram.hasEntity() 中
        if (page == null && hologram.hasEntity(entityId)) {
            page = hologram.getPage(player);
        }
        HologramClickEvent event = new HologramClickEvent(hologram, page, player, clickType, entityId);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            // 节点 3: debug 日志 - 事件被取消
            final boolean pageFound = page != null;
            plugin.debug(() -> String.format(
                    "[Node3 handleClick] player=%s, entityId=%d, clickType=%s, hologramFound=true, pageFound=%s, eventCancelled=true",
                    player.getName(), entityId, clickType, pageFound));
            return;
        }

        if (page != null) {
            // 优先使用 Y 坐标路由（合并 TextDisplay 组的场景）
            HologramLine line = page.getLineByEntityId(entityId, hitY);
            boolean lineFound = line != null && line.hasActions();
            int actionCount = lineFound
                    ? countActions(line, clickType)
                    : countActions(page, clickType);
            // 节点 3: debug 日志 - 页面内路由结果
            plugin.debug(() -> String.format(
                    "[Node3 handleClick] player=%s, entityId=%d, clickType=%s, hologramFound=true, pageFound=true, lineFound=%s, actionCount=%d",
                    player.getName(), entityId, clickType, lineFound, actionCount));
            if (line != null && line.hasActions()) {
                line.executeActions(player, clickType);
                return;
            }
            // ClickableHologramRenderer 无法映射到具体行，执行页面级动作
            page.executeActions(player, clickType);
            return;
        }

        // 节点 3: debug 日志 - 全息图级动作
        HologramPage playerPage = hologram.getPage(player);
        int holoActionCount = playerPage != null ? countActions(playerPage, clickType) : 0;
        plugin.debug(() -> String.format(
                "[Node3 handleClick] player=%s, entityId=%d, clickType=%s, hologramFound=true, pageFound=false, lineFound=false, actionCount=%d",
                player.getName(), entityId, clickType, holoActionCount));
        hologram.executeActions(player, clickType);
    }

    /**
     * 计算指定 ClickType 实际会执行的动作数量（包含 ANY 类型，避免重复计算）
     * 用于 debug 日志输出
     *
     * @param provider 动作持有者（HologramPage 或 HologramLine）
     * @param clickType 点击类型
     * @return 动作数量
     */
    private int countActions(Object provider, ClickType clickType) {
        if (provider instanceof HologramPage page) {
            int count = page.getActions(clickType).size();
            if (clickType != ClickType.ANY) {
                count += page.getActions(ClickType.ANY).size();
            }
            return count;
        } else if (provider instanceof HologramLine line) {
            int count = line.getActions(clickType).size();
            if (clickType != ClickType.ANY) {
                count += line.getActions(ClickType.ANY).size();
            }
            return count;
        }
        return 0;
    }

    /**
     * 根据实体 ID 查找全息图
     */
    private Hologram findHologramByEntityId(Player player, int entityId) {
        Hologram hologram = plugin.getHologramManager().getHologramByEntityId(entityId);
        if (hologram != null && hologram.isEnabled() && hologram.isVisible(player)) {
            return hologram;
        }
        return null;
    }
}
