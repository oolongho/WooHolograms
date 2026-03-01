package com.oolonghoo.holograms.nms.versions.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.oolonghoo.holograms.hologram.HeadTexture;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.NmsAdapter;
import com.oolonghoo.holograms.nms.NmsHologramPartData;
import com.oolonghoo.holograms.nms.renderer.NmsHeadHologramRenderer;
import com.oolonghoo.holograms.nms.util.DecentPosition;
import com.oolonghoo.holograms.nms.versions.EntityIdGenerator;
import com.oolonghoo.holograms.nms.versions.EntityMetadataBuilder;
import com.oolonghoo.holograms.nms.versions.EntityPacketsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        if (location == null || line == null) {
            return;
        }

        ItemStack headItem = createHeadItem(line);
        DecentPosition position = DecentPosition.fromLocation(location);
        DecentPosition offsetPosition = offsetPosition(position);

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, EntityType.ARMOR_STAND, offsetPosition)
                .withEntityMetadata(entityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withArmorStandProperties(small, true)
                        .toWatchableObjects())
                .withHelmet(entityId, headItem)
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
        ItemStack headItem = createHeadItem(line);
        EntityPacketsBuilder.create()
                .withHelmet(entityId, headItem)
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
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, offsetPosition(position))
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

    protected DecentPosition offsetPosition(DecentPosition position) {
        double offsetY = small ? 1.1875d : 2.0d;
        return position.subtractY(offsetY);
    }

    protected ItemStack createHeadItem(HologramLine line) {
        HeadTexture headTexture = line.getHeadTexture();
        
        if (headTexture == null) {
            String content = line.getContent();
            return createHeadFromContent(content);
        }

        switch (headTexture.getType()) {
            case BASE64:
                return createHeadFromBase64(headTexture.getTextureValue());
            case PLAYER:
                return createHeadFromPlayerName(headTexture.getValue());
            case HDB:
                return createHeadFromHDB(headTexture.getValue());
            default:
                return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    protected ItemStack createHeadFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        String upperContent = content.toUpperCase();
        if (upperContent.startsWith("#HEAD:") || upperContent.startsWith("#SMALLHEAD:")) {
            String textureData = extractTextureData(content);
            if (textureData != null && !textureData.isEmpty()) {
                if (isBase64Texture(textureData)) {
                    return createHeadFromBase64(textureData);
                } else {
                    return createHeadFromPlayerName(textureData);
                }
            }
        }

        return new ItemStack(Material.PLAYER_HEAD);
    }

    protected String extractTextureData(String content) {
        String upperContent = content.toUpperCase();
        if (upperContent.startsWith("#HEAD:")) {
            return content.substring(6);
        } else if (upperContent.startsWith("#SMALLHEAD:")) {
            return content.substring(11);
        }
        return null;
    }

    protected boolean isBase64Texture(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.length() > 50;
    }

    protected ItemStack createHeadFromBase64(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64));
            
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                try {
                    Method setProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                    setProfileMethod.setAccessible(true);
                    setProfileMethod.invoke(meta, profile);
                } catch (Exception ignored) {
                }
            }
            
            head.setItemMeta(meta);
        }
        
        return head;
    }

    protected ItemStack createHeadFromPlayerName(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }
        
        return head;
    }

    protected ItemStack createHeadFromHDB(String hdbId) {
        return new ItemStack(Material.PLAYER_HEAD);
    }
}
