package com.oolonghoo.holograms.nms.versions.renderer;

import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramPartData;
import com.oolonghoo.holograms.nms.renderer.NmsHeadHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 头颅全息图渲染器实现
 *
 * @author oolongho
 * @since 1.0.0
 */
public class HeadHologramRendererImpl implements NmsHeadHologramRenderer {

    protected final int entityId;
    protected final boolean small;
    protected boolean destroyed = false;

    public HeadHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this(entityIdGenerator, false);
    }

    protected HeadHologramRendererImpl(EntityIdGenerator entityIdGenerator, boolean small) {
        this.entityId = entityIdGenerator.getFreeEntityId();
        this.small = small;
    }

    public void display(Player player, NmsHologramPartData<ItemStack> data) {
        DecentPosition position = data.getPosition();
        ItemStack content = data.getContent();
        DecentPosition offsetPosition = offsetPosition(position);
        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, EntityType.ARMOR_STAND, offsetPosition)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withArmorStandProperties(small, true)
                        .toWatchableObjects())
                .withHelmet(entityId, content)
                .sendTo(player);
    }

    public void updateContent(Player player, NmsHologramPartData<ItemStack> data) {
        EntityPacketsBuilder.create()
                .withHelmet(entityId, data.getContent())
                .sendTo(player);
    }

    public void move(Player player, NmsHologramPartData<ItemStack> data) {
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, offsetPosition(data.getPosition()))
                .sendTo(player);
    }

    public void hide(Player player) {
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
    }

    public double getHeight(NmsHologramPartData<ItemStack> data) {
        return small ? 0.5d : 0.7d;
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
        // Head renderer uses display() method with NmsHologramPartData
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        // Head renderer uses updateContent() method
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
        // Head renderer uses move() method
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

    protected DecentPosition offsetPosition(DecentPosition position) {
        double offsetY = small ? 1.1875d : 2.0d;
        return position.subtractY(offsetY);
    }
}
