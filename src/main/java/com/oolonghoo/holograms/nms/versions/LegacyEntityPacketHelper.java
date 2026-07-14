package com.oolonghoo.holograms.nms.versions;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.UUID;

final class LegacyEntityPacketHelper implements EntityPacketHelper {

    private static final Constructor<?> SPAWN_CONSTRUCTOR;

    static {
        try {
            SPAWN_CONSTRUCTOR = ClientboundAddEntityPacket.class.getDeclaredConstructor(
                    int.class, UUID.class, double.class, double.class, double.class,
                    float.class, float.class, EntityType.class, int.class, Vec3.class,
                    float.class
            );
            SPAWN_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find legacy spawn packet constructor", e);
        }
    }

    @Override
    public Packet<?> createSpawnPacket(int entityId, UUID uuid, double x, double y, double z,
                                       float pitch, float yaw, EntityType<?> type, int data,
                                       Vec3 delta, float headYaw) {
        try {
            return (Packet<?>) SPAWN_CONSTRUCTOR.newInstance(
                    entityId, uuid, x, y, z, pitch, yaw, type, data, delta, headYaw
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create legacy spawn packet", e);
        }
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
