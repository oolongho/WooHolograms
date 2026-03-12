package com.oolonghoo.holograms.nms.versions;

import com.oolonghoo.holograms.nms.NmsPacketListener;
import com.oolonghoo.holograms.nms.event.NmsEntityInteractAction;
import com.oolonghoo.holograms.nms.event.NmsEntityInteractEvent;
import com.oolonghoo.holograms.nms.util.WooHologramsException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.entity.Player;

/**
 * 入站数据包处理器
 * 用于处理玩家发送的数据包
 *
 * 
 * 
 */
class InboundPacketHandler extends ChannelInboundHandlerAdapter {

    private final Player player;
    private final NmsPacketListener listener;

    InboundPacketHandler(Player player, NmsPacketListener listener) {
        this.player = player;
        this.listener = listener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        if (packet instanceof ServerboundInteractPacket serverboundInteractPacket) {
            FriendlyByteBufWrapper serializer = FriendlyByteBufWrapper.getInstance();
            ServerboundInteractPacket.STREAM_CODEC.encode(serializer.getSerializer(), serverboundInteractPacket);

            int entityId = serializer.readVarInt();
            int actionEnumValueOrdinal = serializer.readVarInt();

            NmsEntityInteractAction action = mapActionEnumValueOrdinalToNmsEntityInteractionAction(actionEnumValueOrdinal);
            NmsEntityInteractEvent event = new NmsEntityInteractEvent(player, entityId, action);
            listener.onEntityInteract(event);
            if (event.isHandled()) {
                return;
            }
        }
        super.channelRead(ctx, packet);
    }

    private NmsEntityInteractAction mapActionEnumValueOrdinalToNmsEntityInteractionAction(int ordinal) {
        // 0 = INTERACT
        // 1 = ATTACK
        // 2 = INTERACT_AT
        //
        // https://minecraft.wiki/w/Java_Edition_protocol#Interact
        return switch (ordinal) {
            case 1 -> player.isSneaking() ? NmsEntityInteractAction.SHIFT_LEFT_CLICK : NmsEntityInteractAction.LEFT_CLICK;
            case 0, 2 -> player.isSneaking() ? NmsEntityInteractAction.SHIFT_RIGHT_CLICK : NmsEntityInteractAction.RIGHT_CLICK;
            default -> throw new WooHologramsException("Unknown entity use action: " + ordinal);
        };
    }
}
