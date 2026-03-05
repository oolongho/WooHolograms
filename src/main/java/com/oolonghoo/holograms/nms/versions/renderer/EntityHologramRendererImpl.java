package com.oolonghoo.holograms.nms.versions.renderer;

import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.renderer.NmsEntityHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class EntityHologramRendererImpl implements NmsEntityHologramRenderer {

    private final int entityId;
    private EntityType entityType = EntityType.ZOMBIE;
    private boolean destroyed = false;

    public EntityHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.singletonList(entityId);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (destroyed || location == null || location.getWorld() == null) {
            return;
        }

        if (line != null && line.getEntityType() != null) {
            this.entityType = line.getEntityType();
        }

        Hologram hologram = line != null ? line.getHologram() : null;
        Billboard billboard = hologram != null ? hologram.getBillboard() : Billboard.CENTER;
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        float yaw = line != null && line.getCustomYaw() != null ? line.getCustomYaw() : hologramFacing;
        float pitch = line != null && line.getCustomPitch() != null ? line.getCustomPitch() : 0;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withSilent();

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, entityType,
                        new DecentPosition(
                                location.getX(), location.getY(), location.getZ()),
                        yaw, pitch)
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
                .sendTo(player);
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
    }

    @Override
    public void updateText(Collection<Player> players, HologramLine line) {
    }

    @Override
    public void destroy(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
    }

    @Override
    public void destroy(Collection<Player> players) {
        for (Player player : players) {
            destroy(player);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        if (destroyed || location == null) {
            return;
        }

        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, new DecentPosition(
                        location.getX(), location.getY(), location.getZ()))
                .sendTo(player);
    }

    @Override
    public void teleport(Collection<Player> players, Location location) {
        for (Player player : players) {
            teleport(player, location);
        }
    }

    @Override
    public NmsAdapter getAdapter() {
        return null;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
}
