package com.oolonghoo.holograms.nms.versions;

import com.mojang.datafixers.util.Pair;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 实体数据包构建器
 * 用于构建和发送实体相关的数据包
 *
 * 
 * 
 */
public class EntityPacketsBuilder {

    private final List<Packet<?>> packets;

    private EntityPacketsBuilder() {
        this.packets = new ArrayList<>();
    }

    /**
     * 发送所有数据包给玩家
     *
     * @param player 目标玩家
     */
    public void sendTo(Player player) {
        for (Packet<?> packet : packets) {
            sendPacket(player, packet);
        }
    }

    /**
     * 生成实体
     *
     * @param entityId 实体 ID
     * @param type     实体类型
     * @param position 位置
     * @return this
     */
    public EntityPacketsBuilder withSpawnEntity(int entityId, EntityType type, DecentPosition position) {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                entityId,
                UUID.randomUUID(),
                position.getX(),
                position.getY(),
                position.getZ(),
                position.getPitch(),
                position.getYaw(),
                EntityTypeRegistry.findEntityTypes(type),
                type == EntityType.ITEM ? 1 : 0,
                Vec3.ZERO,
                position.getYaw()
        );

        packets.add(packet);
        return this;
    }

    /**
     * 生成实体（带自定义朝向）
     *
     * @param entityId 实体 ID
     * @param type     实体类型
     * @param position 位置
     * @param yaw      偏航角
     * @param pitch    俯仰角
     * @return this
     */
    public EntityPacketsBuilder withSpawnEntity(int entityId, EntityType type, DecentPosition position, float yaw, float pitch) {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                entityId,
                UUID.randomUUID(),
                position.getX(),
                position.getY(),
                position.getZ(),
                pitch,
                yaw,
                EntityTypeRegistry.findEntityTypes(type),
                type == EntityType.ITEM ? 1 : 0,
                Vec3.ZERO,
                yaw
        );

        packets.add(packet);
        return this;
    }

    /**
     * 设置实体元数据
     *
     * @param entityId 实体 ID
     * @param items    元数据项列表
     * @return this
     */
    public EntityPacketsBuilder withEntityMetadata(int entityId, List<SynchedEntityData.DataItem<?>> items) {
        List<SynchedEntityData.DataValue<?>> cs = new ArrayList<>();
        for (SynchedEntityData.DataItem<?> item : items) {
            cs.add(item.value());
        }

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entityId, cs);
        packets.add(packet);
        return this;
    }

    /**
     * 设置头盔
     *
     * @param entityId  实体 ID
     * @param itemStack 物品
     * @return this
     */
    public EntityPacketsBuilder withHelmet(int entityId, ItemStack itemStack) {
        Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack> equipmentPair = new Pair<>(
                CraftEquipmentSlot.getNMS(EquipmentSlot.HEAD),
                itemStackToNms(itemStack)
        );
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(
                entityId,
                Collections.singletonList(equipmentPair)
        );
        packets.add(packet);
        return this;
    }

    /**
     * 传送实体
     *
     * @param entityId 实体 ID
     * @param position 位置
     * @return this
     */
    public EntityPacketsBuilder withTeleportEntity(int entityId, DecentPosition position) {
        Vec3 locationVec3 = new Vec3(position.getX(), position.getY(), position.getZ());
        Vec3 zeroVec3 = new Vec3(0, 0, 0);
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(
                entityId,
                new PositionMoveRotation(locationVec3, zeroVec3, position.getYaw(), position.getPitch()),
                Set.of(),
                false
        );
        packets.add(packet);
        return this;
    }

    /**
     * 设置乘客
     *
     * @param entityId  载具实体 ID
     * @param passenger 乘客实体 ID
     * @return this
     */
    public EntityPacketsBuilder withPassenger(int entityId, int passenger) {
        return updatePassenger(entityId, passenger);
    }

    /**
     * 移除乘客
     *
     * @param entityId 载具实体 ID
     * @return this
     */
    public EntityPacketsBuilder withRemovePassenger(int entityId) {
        return updatePassenger(entityId, -1);
    }

    private EntityPacketsBuilder updatePassenger(int entityId, int... passengers) {
        FriendlyByteBufWrapper serializer = FriendlyByteBufWrapper.getInstance();
        serializer.writeVarInt(entityId);
        serializer.writeIntArray(passengers);

        ClientboundSetPassengersPacket packet = ClientboundSetPassengersPacket.STREAM_CODEC.decode(serializer.getSerializer());
        packets.add(packet);
        return this;
    }

    /**
     * 移除实体
     *
     * @param entityId 实体 ID
     * @return this
     */
    public EntityPacketsBuilder withRemoveEntity(int entityId) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityId);
        packets.add(packet);
        return this;
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private net.minecraft.world.item.ItemStack itemStackToNms(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    /**
     * 创建一个新的构建器
     *
     * @return 新的构建器实例
     */
    public static EntityPacketsBuilder create() {
        return new EntityPacketsBuilder();
    }
}
