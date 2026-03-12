package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramRendererFactory;
import com.oolonghoo.holograms.nms.NmsPacketListener;
import com.oolonghoo.holograms.nms.util.ReflectField;
import com.oolonghoo.holograms.nms.util.WooHologramsException;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * NMS 适配器 - 1.21 版本
 *
 * <p>此实现使用 Paper 1.21+ NMS API。</p>
 *
 * 
 * 
 */
@SuppressWarnings("unused") // 通过反射实例化
public class Nms_1_21 implements NmsAdapter {

    private static final String PACKET_HANDLER_NAME = "woo_holograms_packet_handler";
    private static final String DEFAULT_PIPELINE_TAIL = "DefaultChannelPipeline$TailContext#0";
    private static final ReflectField<Connection> NETWORK_MANAGER_FIELD = new ReflectField<>(
            ServerCommonPacketListenerImpl.class, "connection");

    private final HologramRendererFactoryImpl hologramRendererFactory;

    public Nms_1_21() {
        EntityIdGenerator entityIdGenerator = new EntityIdGenerator();
        this.hologramRendererFactory = new HologramRendererFactoryImpl(entityIdGenerator);
    }

    @Override
    public NmsHologramRendererFactory getHologramRendererFactory() {
        return hologramRendererFactory;
    }

    @Override
    public void registerPacketListener(Player player, NmsPacketListener listener) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        executeOnPipelineInEventLoop(player, pipeline -> {
            if (pipeline.get(PACKET_HANDLER_NAME) != null) {
                pipeline.remove(PACKET_HANDLER_NAME);
            }
            pipeline.addBefore("packet_handler", PACKET_HANDLER_NAME, new InboundPacketHandler(player, listener));
        });
    }

    @Override
    public void unregisterPacketListener(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        executeOnPipelineInEventLoop(player, pipeline -> {
            if (pipeline.get(PACKET_HANDLER_NAME) != null) {
                pipeline.remove(PACKET_HANDLER_NAME);
            }
        });
    }

    @Override
    public String getVersion() {
        return "1.21";
    }

    private void executeOnPipelineInEventLoop(Player player, Consumer<ChannelPipeline> task) {
        ChannelPipeline pipeline = getPipeline(player);
        EventLoop eventLoop = pipeline.channel().eventLoop();

        if (eventLoop.inEventLoop()) {
            executeOnPipeline(player, task, pipeline);
        } else {
            eventLoop.execute(() -> executeOnPipeline(player, task, pipeline));
        }
    }

    private ChannelPipeline getPipeline(Player player) {
        ServerGamePacketListenerImpl playerConnection = ((CraftPlayer) player).getHandle().connection;
        Connection networkManager = NETWORK_MANAGER_FIELD.get(playerConnection);
        return networkManager.channel.pipeline();
    }

    private void executeOnPipeline(Player player, Consumer<ChannelPipeline> task, ChannelPipeline pipeline) {
        if (!player.isOnline()) {
            return;
        }

        try {
            task.accept(pipeline);
        } catch (NoSuchElementException e) {
            List<String> handlers = pipeline.names();
            if (handlers.size() == 1 && handlers.getFirst().equals(DEFAULT_PIPELINE_TAIL)) {
                // 玩家加入后立即断开连接
                return;
            }
            throwFailedToModifyPipelineException(player, e);
        } catch (Exception e) {
            throwFailedToModifyPipelineException(player, e);
        }
    }

    private void throwFailedToModifyPipelineException(Player player, Exception e) {
        throw new WooHologramsException("Failed to modify player's pipeline. player: " + player.getName(), e);
    }
}
