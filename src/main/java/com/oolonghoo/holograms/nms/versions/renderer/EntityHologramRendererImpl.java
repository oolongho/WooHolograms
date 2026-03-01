package com.oolonghoo.holograms.nms.versions.renderer;

import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramPartData;
import com.oolonghoo.holograms.nms.renderer.NmsEntityHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import com.oolonghoo.holograms.nms.versions.EntityTypeRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EntityHologramRendererImpl implements NmsEntityHologramRenderer {

    private final int entityId;
    private boolean destroyed = false;
    private EntityType currentEntityType;
    private float currentYaw;
    private float currentPitch;

    public EntityHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    public void display(Player player, NmsHologramPartData<EntityType> data) {
        DecentPosition position = data.getPosition();
        EntityType content = data.getContent();
        this.currentEntityType = content;
        DecentPosition offsetPosition = offsetPosition(position, content);
        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, content, offsetPosition)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withSilent()
                        .withNoGravity()
                        .toWatchableObjects())
                .sendTo(player);
    }

    public void updateContent(Player player, NmsHologramPartData<EntityType> data) {
        hide(player);
        display(player, data);
    }

    public void move(Player player, NmsHologramPartData<EntityType> data) {
        DecentPosition offsetPosition = offsetPosition(data.getPosition(), data.getContent());
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, offsetPosition)
                .sendTo(player);
    }

    public void hide(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
    }

    public double getHeight(NmsHologramPartData<EntityType> data) {
        return EntityTypeRegistry.getEntityTypeHeight(data.getContent());
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.singletonList(entityId);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (location == null || line == null) {
            return;
        }

        EntityType entityType = parseEntityType(line.getContent());
        this.currentEntityType = entityType;
        DecentPosition position = DecentPosition.fromLocation(location);
        DecentPosition offsetPosition = offsetPosition(position, entityType);
        
        Hologram hologram = line.getHologram();
        Billboard billboard = hologram != null ? hologram.getBillboard() : Billboard.CENTER;
        float facing = hologram != null ? hologram.getFacing() : 0f;
        
        float yaw;
        float pitch;
        
        if (billboard == Billboard.FIXED_ANGLE) {
            yaw = location.getYaw() + facing;
            pitch = location.getPitch();
        } else {
            yaw = calculateYawToPlayer(location, player);
            pitch = calculatePitchToPlayer(location, player);
        }
        
        this.currentYaw = yaw;
        this.currentPitch = pitch;

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, entityType, offsetPosition, yaw, pitch)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withSilent()
                        .withNoGravity()
                        .toWatchableObjects())
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
        EntityType newType = parseEntityType(line.getContent());
        if (newType != currentEntityType) {
            hide(player);
            this.currentEntityType = newType;
            DecentPosition position = DecentPosition.fromLocation(line.getLocation());
            DecentPosition offsetPosition = offsetPosition(position, newType);
            EntityPacketsBuilder.create()
                    .withSpawnEntity(entityId, newType, offsetPosition)
                    .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                            .withSilent()
                            .withNoGravity()
                            .toWatchableObjects())
                    .sendTo(player);
        }
    }

    @Override
    public void updateText(Collection<Player> players, HologramLine line) {
        for (Player player : players) {
            updateText(player, line);
        }
    }

    @Override
    public void destroy(Player player) {
        hide(player);
    }

    @Override
    public void destroy(Collection<Player> players) {
        for (Player player : players) {
            destroy(player);
        }
    }

    @Override
    public void teleport(Player player, Location location) {
        if (location == null) {
            return;
        }
        DecentPosition position = DecentPosition.fromLocation(location);
        DecentPosition offsetPosition = offsetPosition(position, currentEntityType);
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, offsetPosition)
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

    private DecentPosition offsetPosition(DecentPosition position, EntityType entityType) {
        double height = EntityTypeRegistry.getEntityTypeHeight(entityType != null ? entityType : EntityType.ZOMBIE);
        return position.subtractY(height / 2);
    }

    private EntityType parseEntityType(String content) {
        if (content == null || content.isEmpty()) {
            return EntityType.ZOMBIE;
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#ENTITY:")) {
            String entityName = content.substring(8).trim().toUpperCase(Locale.ROOT);
            try {
                return EntityType.valueOf(entityName);
            } catch (IllegalArgumentException e) {
                for (EntityType type : EntityType.values()) {
                    if (type.name().contains(entityName) || entityName.contains(type.name())) {
                        return type;
                    }
                }
            }
        }

        return EntityType.ZOMBIE;
    }
    
    private float calculateYawToPlayer(Location hologramLoc, Player player) {
        Location playerLoc = player.getEyeLocation();
        double dx = playerLoc.getX() - hologramLoc.getX();
        double dz = playerLoc.getZ() - hologramLoc.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
    }
    
    private float calculatePitchToPlayer(Location hologramLoc, Player player) {
        Location playerLoc = player.getEyeLocation();
        double dx = playerLoc.getX() - hologramLoc.getX();
        double dy = playerLoc.getY() - hologramLoc.getY();
        double dz = playerLoc.getZ() - hologramLoc.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, distance));
    }
}
