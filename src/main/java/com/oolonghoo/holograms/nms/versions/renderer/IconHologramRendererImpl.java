package com.oolonghoo.holograms.nms.versions.renderer;

import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramPartData;
import com.oolonghoo.holograms.nms.renderer.NmsIconHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 图标全息图渲染器实现
 *
 * @author oolongho
 * @since 1.0.0
 */
public class IconHologramRendererImpl implements NmsIconHologramRenderer {

    private final int itemEntityId;
    private final int armorStandEntityId;
    private boolean destroyed = false;

    public IconHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.itemEntityId = entityIdGenerator.getFreeEntityId();
        this.armorStandEntityId = entityIdGenerator.getFreeEntityId();
    }

    public void display(Player player, NmsHologramPartData<ItemStack> data) {
        DecentPosition position = data.getPosition();
        ItemStack content = data.getContent();
        EntityPacketsBuilder.create()
                .withSpawnEntity(armorStandEntityId, EntityType.ARMOR_STAND, offsetPosition(position))
                .withEntityMetadata(armorStandEntityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withArmorStandProperties(true, true)
                        .toWatchableObjects())
                .withSpawnEntity(itemEntityId, EntityType.ITEM, position)
                .withEntityMetadata(itemEntityId, EntityMetadataBuilder.create()
                        .withItemStack(content)
                        .toWatchableObjects())
                .withTeleportEntity(itemEntityId, position)
                .withPassenger(armorStandEntityId, itemEntityId)
                .sendTo(player);
    }

    public void updateContent(Player player, NmsHologramPartData<ItemStack> data) {
        EntityPacketsBuilder.create()
                .withEntityMetadata(itemEntityId, EntityMetadataBuilder.create()
                        .withItemStack(data.getContent())
                        .toWatchableObjects())
                .sendTo(player);
    }

    public void move(Player player, NmsHologramPartData<ItemStack> data) {
        EntityPacketsBuilder.create()
                .withTeleportEntity(armorStandEntityId, offsetPosition(data.getPosition()))
                .sendTo(player);
    }

    public void hide(Player player) {
        EntityPacketsBuilder.create()
                .withRemovePassenger(armorStandEntityId)
                .withRemoveEntity(itemEntityId)
                .withRemoveEntity(armorStandEntityId)
                .sendTo(player);
    }

    public double getHeight(NmsHologramPartData<ItemStack> data) {
        return 0.5d;
    }

    @Override
    public List<Integer> getEntityIds() {
        return Arrays.asList(armorStandEntityId, itemEntityId);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        // Icon renderer uses display() method with NmsHologramPartData
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        // Icon renderer uses updateContent() method
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
        // Icon renderer uses move() method
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
        return position.subtractY(0.55);
    }
}
