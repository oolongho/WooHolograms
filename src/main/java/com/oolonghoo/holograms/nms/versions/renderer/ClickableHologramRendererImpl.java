package com.oolonghoo.holograms.nms.versions.renderer;
import com.oolonghoo.holograms.nms.renderer.NmsClickableHologramRenderer;
import com.oolonghoo.holograms.nms.util.HologramPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
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
    public void display(Player player, HologramPosition position) {
        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, EntityType.ARMOR_STAND, position)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withArmorStandProperties(false, true)
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
