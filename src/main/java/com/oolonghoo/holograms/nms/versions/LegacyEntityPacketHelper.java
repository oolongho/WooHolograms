package com.oolonghoo.holograms.nms.versions;

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
    private static final Constructor<?> TELEPORT_CONSTRUCTOR;

    static {
        try {
            SPAWN_CONSTRUCTOR = ClientboundAddEntityPacket.class.getConstructor(
                    int.class, UUID.class, double.class, double.class, double.class,
                    float.class, float.class, EntityType.class, int.class, Vec3.class
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find legacy spawn packet constructor", e);
        }
        try {
            TELEPORT_CONSTRUCTOR = ClientboundTeleportEntityPacket.class.getConstructor(
                    int.class, double.class, double.class, double.class,
                    float.class, float.class, Set.class, boolean.class
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find legacy teleport packet constructor", e);
        }
    }

    @Override
    public Packet<?> createSpawnPacket(int entityId, UUID uuid, double x, double y, double z,
                                       float pitch, float yaw, EntityType<?> type, int data,
                                       Vec3 delta, float headYaw) {
        try {
            return (Packet<?>) SPAWN_CONSTRUCTOR.newInstance(
                    entityId, uuid, x, y, z, pitch, yaw, type, data, delta
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
        try {
            return (Packet<?>) TELEPORT_CONSTRUCTOR.newInstance(
                    entityId, x, y, z, yaw, pitch, relatives, onGround
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create legacy teleport packet", e);
        }
    }
}
