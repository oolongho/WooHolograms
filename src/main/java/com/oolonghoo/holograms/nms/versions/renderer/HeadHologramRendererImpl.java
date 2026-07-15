package com.oolonghoo.holograms.nms.versions.renderer;
import com.oolonghoo.holograms.hologram.Billboard;
import com.oolonghoo.holograms.hologram.HeadTexture;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.nms.renderer.NmsHeadHologramRenderer;
import com.oolonghoo.holograms.nms.util.HologramPosition;
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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeadHologramRendererImpl implements NmsHeadHologramRenderer {

    /** 缓存的 HeadDatabaseAPI 实例，避免每次调用都通过反射创建 */
    private static volatile Object cachedHeadDatabaseApi;
    private static volatile Method cachedGetItemMethod;

    protected final int entityId;
    protected final boolean small;
    protected volatile boolean destroyed = false;
    protected final Map<UUID, String> lastContentPerPlayer = new ConcurrentHashMap<>();

    public HeadHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this(entityIdGenerator, false);
    }

    protected HeadHologramRendererImpl(EntityIdGenerator entityIdGenerator, boolean small) {
        this.entityId = entityIdGenerator.getFreeEntityId();
        this.small = small;
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
        if (destroyed || location == null || line == null) {
            return;
        }

        ItemStack headItem = createHeadItem(line, player);
        HologramPosition position = HologramPosition.fromLocation(location);
        HologramPosition offsetPosition = offsetPosition(position);
        
        Hologram hologram = line.getHologram();
        Billboard billboard = line.getBillboard() != null ? line.getBillboard() : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);
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
        String rawContent = line.getContent();
        if (rawContent != null && player != null) {
            lastContentPerPlayer.put(player.getUniqueId(), com.oolonghoo.holograms.util.PlaceholderUtil.replace(rawContent, player));
        }
    }

    @Override
    public void render(Collection<Player> players, Location location, HologramLine line) {
        for (Player player : players) {
            render(player, location, line);
        }
    }

    @Override
    public void updateText(Player player, HologramLine line) {
        if (destroyed) return;
        String rawContent = line.getContent();
        String resolvedContent = rawContent;
        if (rawContent != null && player != null) {
            resolvedContent = com.oolonghoo.holograms.util.PlaceholderUtil.replace(rawContent, player);
        }
        String lastContent = lastContentPerPlayer.get(player.getUniqueId());
        if (resolvedContent != null && resolvedContent.equals(lastContent)) return;
        lastContentPerPlayer.put(player.getUniqueId(), resolvedContent);

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
        EntityPacketsBuilder.create()
                .withRemoveEntity(entityId)
                .sendTo(player);
        lastContentPerPlayer.remove(player.getUniqueId());
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            EntityPacketsBuilder.create()
                    .withRemoveEntity(entityId)
                    .sendTo(player);
        }
        lastContentPerPlayer.clear();
    }

    @Override
    public void teleport(Player player, Location location) {
        if (destroyed || location == null) {
            return;
        }
        HologramPosition position = HologramPosition.fromLocation(location);
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
    public void reset() {
        destroyed = false;
        lastContentPerPlayer.clear();
    }

    protected HologramPosition offsetPosition(HologramPosition position) {
        double offsetY = small ? 1.1875d : 2.0d;
        return position.subtractY(offsetY);
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
            case BASE64 -> HeadTexture.createHeadFromBase64(value);
            case PLAYER -> createHeadFromPlayerName(value);
            case HDB -> createHeadFromHDB(value);
            default -> new ItemStack(Material.PLAYER_HEAD);
        };
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
                if (cachedHeadDatabaseApi == null || cachedGetItemMethod == null) {
                    Object api = Class.forName("ar.com.zir.libs.headdatabase.api.HeadDatabaseAPI").getDeclaredConstructor().newInstance();
                    Method getItemMethod = api.getClass().getMethod("getItem", String.class);
                    cachedHeadDatabaseApi = api;
                    cachedGetItemMethod = getItemMethod;
                }
                ItemStack head = (ItemStack) cachedGetItemMethod.invoke(cachedHeadDatabaseApi, hdbId);
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
