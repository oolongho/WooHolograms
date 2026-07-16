package com.oolongho.holograms.nms.versions.adapter.v1_21_2;

import com.oolongho.holograms.nms.versions.adapter.EntityPacketHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public final class ModernEntityPacketHelper implements EntityPacketHelper {

    @Override
    public Packet<?> createSpawnPacket(int entityId, UUID uuid, double x, double y, double z,
                                       float pitch, float yaw, EntityType<?> type, int data,
                                       Vec3 delta, float headYaw) {
        return new ClientboundAddEntityPacket(
                entityId, uuid, x, y, z, pitch, yaw, type, data, delta, headYaw
        );
    }

    @Override
    public Packet<?> createTeleportPacket(int entityId, double x, double y, double z,
                                          float yaw, float pitch,
                                          Set<net.minecraft.world.entity.Relative> relatives,
                                          boolean onGround) {
        return new ClientboundTeleportEntityPacket(
                entityId,
                new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yaw, pitch),
                relatives,
                onGround
        );
    }
}
