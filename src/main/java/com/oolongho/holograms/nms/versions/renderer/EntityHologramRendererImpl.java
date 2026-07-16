package com.oolongho.holograms.nms.versions.renderer;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.nms.renderer.NmsEntityHologramRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class EntityHologramRendererImpl implements NmsEntityHologramRenderer {

    private final int entityId;
    private EntityType entityType = EntityType.ZOMBIE;
    private volatile boolean destroyed = false;

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
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        Float customYaw = line != null ? line.getCustomYaw() : null;
        Float customPitch = line != null ? line.getCustomPitch() : null;
        float yaw = customYaw != null ? customYaw : hologramFacing;
        float pitch = customPitch != null ? customPitch : 0;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withSilent();

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, entityType,
                        new HologramPosition(
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
        if (destroyed || line == null) return;
        EntityType newType = line.getEntityType();
        if (newType != null && newType != this.entityType) {
            destroy(player);
            this.entityType = newType;
            render(player, line.getLocation(), line);
        }
    }

    @Override
    public void updateText(Collection<Player> players, HologramLine line) {
        if (destroyed || line == null) return;
        EntityType newType = line.getEntityType();
        if (newType != null && newType != this.entityType) {
            this.entityType = newType;
            for (Player player : players) {
                destroy(player);
                render(player, line.getLocation(), line);
            }
        }
    }

    @Override
    public void destroy(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            EntityPacketsBuilder.create()
                    .withRemoveEntity(entityId)
                    .sendTo(player);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        if (destroyed || location == null) {
            return;
        }

        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, new HologramPosition(
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
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void reset() {
        destroyed = false;
        entityType = EntityType.ZOMBIE;
    }
}
