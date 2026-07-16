package com.oolongho.holograms.nms.versions.adapter.v1_21_0;

import com.oolongho.holograms.nms.versions.adapter.EntityPacketHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public final class LegacyEntityPacketHelper implements EntityPacketHelper {

    @Override
    public Packet<?> createSpawnPacket(int entityId, UUID uuid, double x, double y, double z,
                                       float pitch, float yaw, EntityType<?> type, int data,
                                       Vec3 delta, float headYaw) {
        return new ClientboundAddEntityPacket(entityId, uuid, x, y, z, pitch, yaw, type, data, delta, headYaw);
    }

    @Override
    public Packet<?> createTeleportPacket(int entityId, double x, double y, double z,
                                          float yaw, float pitch,
                                          Set<net.minecraft.world.entity.Relative> relatives,
                                          boolean onGround) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte((byte) (yaw * 256.0F / 360.0F));
        buf.writeByte((byte) (pitch * 256.0F / 360.0F));
        buf.writeBoolean(onGround);
        return ClientboundTeleportEntityPacket.STREAM_CODEC.decode(buf);
    }
}
