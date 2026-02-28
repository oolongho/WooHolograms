package com.oolonghoo.holograms.nms.versions.renderer;

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

/**
 * 实体全息图渲染器实现
 *
 * @author oolongho
 * @since 1.0.0
 */
public class EntityHologramRendererImpl implements NmsEntityHologramRenderer {

    private final int entityId;
    private boolean destroyed = false;

    public EntityHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    public void display(Player player, NmsHologramPartData<EntityType> data) {
        DecentPosition position = data.getPosition();
        EntityType content = data.getContent();
        DecentPosition offsetPosition = offsetPosition(position);
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
        hide(player);
        display(player, data);
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
        // Entity renderer uses display() method with NmsHologramPartData
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        // Entity renderer uses updateContent() method
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
        // Entity renderer uses move() method
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

    private DecentPosition offsetPosition(DecentPosition position) {
        return position.subtractY(0.25d);
    }
}
