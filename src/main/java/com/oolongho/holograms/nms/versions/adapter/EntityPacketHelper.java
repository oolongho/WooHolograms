package com.oolongho.holograms.nms.versions.adapter;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public interface EntityPacketHelper {
    Packet<?> createSpawnPacket(int entityId, UUID uuid, double x, double y, double z,
                                 float pitch, float yaw, EntityType<?> type, int data,
                                 Vec3 delta, float headYaw);

    Packet<?> createTeleportPacket(int entityId, double x, double y, double z,
                                    float yaw, float pitch,
                                    Set<net.minecraft.world.entity.Relative> relatives,
                                    boolean onGround);
}
