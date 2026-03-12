package com.oolonghoo.holograms.nms.versions.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.HeadTexture;
import com.oolonghoo.holograms.hologram.Hologram;
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

        ItemStack headItem = createHeadItem(line, player);
        DecentPosition position = DecentPosition.fromLocation(location);
        DecentPosition offsetPosition = offsetPosition(position);
        
        Hologram hologram = line.getHologram();
        Billboard billboard = hologram != null ? hologram.getBillboard() : Billboard.CENTER;
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;
        
        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withInvisible()
                .withNoGravity()
                .withArmorStandProperties(small, true);
        
        float yaw;
        float pitch;
        
        Float customYaw = line.getCustomYaw();
        Float customPitch = line.getCustomPitch();
        
        switch (billboard) {
            case FIXED_ANGLE:
                yaw = customYaw != null ? customYaw : hologramFacing;
                pitch = customPitch != null ? customPitch : 0;
                break;
            case HORIZONTAL:
                yaw = customYaw != null ? customYaw : calculateYawToPlayer(location, player);
                pitch = customPitch != null ? customPitch : 0;
                break;
            case VERTICAL:
                yaw = customYaw != null ? customYaw : hologramFacing;
                pitch = customPitch != null ? customPitch : calculatePitchToPlayer(location, player);
                break;
            case CENTER:
            default:
                yaw = customYaw != null ? customYaw : calculateYawToPlayer(location, player);
                pitch = customPitch != null ? customPitch : calculatePitchToPlayer(location, player);
                break;
        }
        
        metadataBuilder.withHeadRotation(pitch, 0, 0);

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, EntityType.ARMOR_STAND, offsetPosition, yaw, 0)
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
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
        ItemStack headItem = createHeadItem(line, player);
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
            headTexture = HeadTexture.parse(content);
        }

        if (headTexture == null) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        return switch (headTexture.getType()) {
            case BASE64 -> createHeadFromBase64(headTexture.getValue());
            case PLAYER -> createHeadFromPlayerName(headTexture.getValue());
            case HDB -> createHeadFromHDB(headTexture.getValue());
            default -> new ItemStack(Material.PLAYER_HEAD);
        };
    }
    
    protected ItemStack createHeadItem(HologramLine line, Player player) {
        HeadTexture headTexture = line.getHeadTexture();
        String content = line.getContent();
        
        if (headTexture == null && content != null) {
            content = com.oolonghoo.holograms.util.PlaceholderUtil.replace(content, player);
            headTexture = HeadTexture.parse(content);
        }

        if (headTexture == null) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        String value = headTexture.getValue();
        if (value != null) {
            value = com.oolonghoo.holograms.util.PlaceholderUtil.replace(value, player);
        }

        return switch (headTexture.getType()) {
            case BASE64 -> createHeadFromBase64(value);
            case PLAYER -> createHeadFromPlayerName(value);
            case HDB -> createHeadFromHDB(value);
            default -> new ItemStack(Material.PLAYER_HEAD);
        };
    }

    protected ItemStack createHeadFromBase64(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null && base64 != null && !base64.isEmpty()) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64));
            
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                try {
                    Method setProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                    setProfileMethod.setAccessible(true);
                    setProfileMethod.invoke(meta, profile);
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
                }
            }
            
            head.setItemMeta(meta);
        }
        
        return head;
    }

    protected ItemStack createHeadFromPlayerName(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null && playerName != null && !playerName.isEmpty()) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }
        
        return head;
    }

    protected ItemStack createHeadFromHDB(String hdbId) {
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            try {
                Object api = Class.forName("ar.com.zir.libs.headdatabase.api.HeadDatabaseAPI").newInstance();
                Method getItemMethod = api.getClass().getMethod("getItem", String.class);
                ItemStack head = (ItemStack) getItemMethod.invoke(api, hdbId);
                if (head != null) {
                    return head;
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException ignored) {
            }
        }
        return new ItemStack(Material.PLAYER_HEAD);
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
