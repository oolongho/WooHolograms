package com.oolonghoo.holograms.nms.versions.renderer;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.TextAlignment;
import com.oolonghoo.holograms.nms.renderer.NmsTextHologramRenderer;
import com.oolonghoo.holograms.nms.util.HologramPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本全息图渲染器实现
 * 使用 TextDisplay 实体显示文本
 * 支持双面渲染（创建两个背对背的 TextDisplay）
 *
 * 注意：TEXT 行现在由 PageTextRendererImpl 统一管理渲染，
 * 此类仅作为渲染器池的兼容保留，不再用于实际 TEXT 行渲染。
 *
 *
 */
public class TextHologramRendererImpl implements NmsTextHologramRenderer {

    private final int frontEntityId;
    private final int backEntityId;
    private volatile boolean destroyed = false;
    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private boolean currentDoubleSided = false;
    private final Map<UUID, String> lastTextPerPlayer = new ConcurrentHashMap<>();

    public TextHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.frontEntityId = entityIdGenerator.getFreeEntityId();
        this.backEntityId = entityIdGenerator.getFreeEntityId();
    }

    @Override
    public List<Integer> getEntityIds() {
        return Arrays.asList(frontEntityId, backEntityId);
    }

    @Override
    public void render(Player player, Location location, HologramLine line) {
        if (destroyed || location == null || location.getWorld() == null) {
            return;
        }

        String text = line.getDisplayText(player);

        Hologram hologram = line.getHologram();
        Billboard billboard = line.getBillboard() != null ? line.getBillboard() : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);
        boolean doubleSided = hologram != null && hologram.isDoubleSided();
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;
        TextAlignment alignment = hologram != null ? hologram.getAlignment() : TextAlignment.LEFT;
        int lineWidth = hologram != null ? hologram.getLineWidth() : 300;
        int backgroundColor = hologram != null ? (hologram.getBackgroundAlpha() << 24) | hologram.getBackgroundColor() : 0;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withTextDisplayText(text)
                .withBillboard(billboard)
                .withTextAlignment(alignment)
                .withTextBackgroundColor(backgroundColor)
                .withTextLineWidth(lineWidth)
                .withDisplayProperties(line, hologram);

        List<SynchedEntityData.DataItem<?>> metadata = metadataBuilder.toWatchableObjects();

        float pitch;
        float yaw;

        if (billboard == Billboard.FIXED_ANGLE) {
            Float customYaw = line.getCustomYaw();
            Float customPitch = line.getCustomPitch();
            yaw = customYaw != null ? customYaw : hologramFacing;
            pitch = customPitch != null ? customPitch : 0;
        } else {
            Float customYaw = line.getCustomYaw();
            Float customPitch = line.getCustomPitch();
            yaw = customYaw != null ? customYaw : location.getYaw();
            pitch = customPitch != null ? customPitch : location.getPitch();
        }

        this.currentYaw = yaw;
        this.currentPitch = pitch;
        this.currentDoubleSided = doubleSided;

        EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                .withSpawnEntity(frontEntityId, org.bukkit.entity.EntityType.TEXT_DISPLAY,
                        new HologramPosition(
                                location.getX(), location.getY(), location.getZ()),
                        yaw, pitch)
                .withEntityMetadata(frontEntityId, metadata);

        if (doubleSided) {
            packetsBuilder.withSpawnEntity(backEntityId, org.bukkit.entity.EntityType.TEXT_DISPLAY,
                            new HologramPosition(
                                    location.getX(), location.getY(), location.getZ()),
                            yaw + 180.0f, pitch)
                    .withEntityMetadata(backEntityId, metadata);
        }

        packetsBuilder.sendTo(player);
        lastTextPerPlayer.put(player.getUniqueId(), text);
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

        String text = line.getDisplayText(player);
        lastTextPerPlayer.put(player.getUniqueId(), text);

        Hologram hologram = line.getHologram();
        Billboard billboard = line.getBillboard() != null ? line.getBillboard() : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);
        boolean doubleSided = hologram != null && hologram.isDoubleSided();
        TextAlignment alignment = hologram != null ? hologram.getAlignment() : TextAlignment.LEFT;
        int lineWidth = hologram != null ? hologram.getLineWidth() : 300;
        int backgroundColor = hologram != null ? (hologram.getBackgroundAlpha() << 24) | hologram.getBackgroundColor() : 0;

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withTextDisplayText(text)
                .withBillboard(billboard)
                .withTextAlignment(alignment)
                .withTextBackgroundColor(backgroundColor)
                .withTextLineWidth(lineWidth)
                .withDisplayProperties(line, hologram);

        List<SynchedEntityData.DataItem<?>> metadata = metadataBuilder.toWatchableObjects();

        EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                .withEntityMetadata(frontEntityId, metadata);

        if (doubleSided) {
            packetsBuilder.withEntityMetadata(backEntityId, metadata);
        }

        packetsBuilder.sendTo(player);
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
                .withRemoveEntity(frontEntityId)
                .withRemoveEntity(backEntityId)
                .sendTo(player);
        lastTextPerPlayer.remove(player.getUniqueId());
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            EntityPacketsBuilder.create()
                    .withRemoveEntity(frontEntityId)
                    .withRemoveEntity(backEntityId)
                    .sendTo(player);
        }
        lastTextPerPlayer.clear();
    }

    @Override
    public void teleport(Player player, Location location) {
        if (destroyed || location == null) {
            return;
        }

        EntityPacketsBuilder packetsBuilder = EntityPacketsBuilder.create()
                .withTeleportEntity(frontEntityId, new HologramPosition(
                        location.getX(), location.getY(), location.getZ(),
                        currentYaw, currentPitch));

        if (currentDoubleSided) {
            packetsBuilder.withTeleportEntity(backEntityId, new HologramPosition(
                    location.getX(), location.getY(), location.getZ(),
                    currentYaw + 180.0f, currentPitch));
        }

        packetsBuilder.sendTo(player);
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
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        currentDoubleSided = false;
        lastTextPerPlayer.clear();
    }
}
