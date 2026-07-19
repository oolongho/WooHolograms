package com.oolongho.holograms.nms.versions.renderer;
import com.oolongho.holograms.nms.renderer.NmsClickableHologramRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * 可点击全息图渲染器实现
 *
 * 
 * 
 */
public class ClickableHologramRendererImpl implements NmsClickableHologramRenderer {

    private final int entityId;
    private volatile boolean destroyed = false;

    public ClickableHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
    }

    @Override
    public void display(Player player, HologramPosition position, float width, float height) {
        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, EntityType.INTERACTION, position)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withSilent()
                        .withInteractionWidth(width)
                        .withInteractionHeight(height)
                        .withInteractionResponse(false)
                        .toWatchableObjects())
                .sendTo(player);
    }

    @Override
    public void move(Player player, HologramPosition position) {
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, position)
                .sendTo(player);
    }

    @Override
    public void hide(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void destroy(Player player) {
        hide(player);
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            hide(player);
        }
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void reset() {
        destroyed = false;
    }
}
