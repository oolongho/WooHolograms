package com.oolonghoo.holograms.nms.versions.renderer;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramRenderer;
import com.oolonghoo.holograms.nms.renderer.NmsTextHologramRenderer;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import com.oolonghoo.holograms.util.ColorUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 文本全息图渲染器实现
 * 使用 TextDisplay 实体显示文本
 *
 * @author oolongho
 * @since 1.0.0
 */
public class TextHologramRendererImpl implements NmsTextHologramRenderer {

    private final int entityId;
    private final UUID entityUUID;
    private boolean destroyed = false;

    public TextHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
        this.entityUUID = UUID.randomUUID();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Collections.singletonList(entityId);
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (destroyed || location == null || location.getWorld() == null) {
            return;
        }

        String text = line.getContent();
        text = ColorUtil.colorize(text);

        List<SynchedEntityData.DataItem<?>> metadata = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withCustomName(text, true)
                .toWatchableObjects();

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, org.bukkit.entity.EntityType.ARMOR_STAND, 
                        new com.oolonghoo.holograms.nms.util.DecentPosition(
                                location.getX(), location.getY(), location.getZ()))
                .withEntityMetadata(entityId, metadata)
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
        if (destroyed) {
            return;
        }

        String text = line.getContent();
        text = ColorUtil.colorize(text);

        List<SynchedEntityData.DataItem<?>> metadata = EntityMetadataBuilder.create()
                .withCustomName(text, true)
                .toWatchableObjects();

        EntityPacketsBuilder.create()
                .withEntityMetadata(entityId, metadata)
                .sendTo(player);
    }

    @Override
    public void updateText(Collection<Player> players, HologramLine line) {
        for (Player player : players) {
            updateText(player, line);
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
                .withTeleportEntity(entityId, new com.oolonghoo.holograms.nms.util.DecentPosition(
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
