package com.oolongho.holograms.nms.versions.renderer;
import com.oolongho.holograms.hologram.HeadTexture;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.nms.renderer.NmsIconHologramRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import com.oolongho.holograms.util.PlaceholderUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class IconHologramRendererImpl implements NmsIconHologramRenderer {

    private final int itemEntityId;
    private final int armorStandEntityId;
    private volatile boolean destroyed = false;
    private final Map<UUID, String> lastContentPerPlayer = new ConcurrentHashMap<>();

    public IconHologramRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.itemEntityId = entityIdGenerator.getFreeEntityId();
        this.armorStandEntityId = entityIdGenerator.getFreeEntityId();
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
        if (destroyed || location == null || line == null) {
            return;
        }

        ItemStack item = parseItem(line.getContent(), player);
        String rawContent = line.getContent();
        if (rawContent != null && player != null) {
            lastContentPerPlayer.put(player.getUniqueId(), PlaceholderUtil.replace(rawContent, player));
        }
        HologramPosition position = HologramPosition.fromLocation(location);

        EntityPacketsBuilder.create()
                .withSpawnEntity(armorStandEntityId, EntityType.ARMOR_STAND, offsetPosition(position))
                .withEntityMetadata(armorStandEntityId, EntityMetadataBuilder.create()
                        .withInvisible()
                        .withNoGravity()
                        .withArmorStandProperties(false, true)
                        .toWatchableObjects())
                .withSpawnEntity(itemEntityId, EntityType.ITEM, position)
                .withEntityMetadata(itemEntityId, EntityMetadataBuilder.create()
                        .withItemStack(item)
                        .toWatchableObjects())
                .withPassenger(armorStandEntityId, itemEntityId)
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
        if (destroyed) return;
        String rawContent = line.getContent();
        String resolvedContent = rawContent;
        if (rawContent != null && player != null) {
            resolvedContent = PlaceholderUtil.replace(rawContent, player);
        }
        String lastContent = lastContentPerPlayer.get(player.getUniqueId());
        if (resolvedContent != null && resolvedContent.equals(lastContent)) return;
        lastContentPerPlayer.put(player.getUniqueId(), resolvedContent);

        ItemStack item = parseItem(line.getContent(), player);
        EntityPacketsBuilder.create()
                .withEntityMetadata(itemEntityId, EntityMetadataBuilder.create()
                        .withItemStack(item)
                        .toWatchableObjects())
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
                .withRemovePassenger(armorStandEntityId)
                .withRemoveEntity(itemEntityId)
                .withRemoveEntity(armorStandEntityId)
                .sendTo(player);
        lastContentPerPlayer.remove(player.getUniqueId());
    }

    @Override
    public void destroy(Collection<Player> players) {
        destroyed = true;
        for (Player player : players) {
            EntityPacketsBuilder.create()
                    .withRemovePassenger(armorStandEntityId)
                    .withRemoveEntity(itemEntityId)
                    .withRemoveEntity(armorStandEntityId)
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
                .withRemovePassenger(armorStandEntityId)
                .withTeleportEntity(armorStandEntityId, offsetPosition(position))
                .withTeleportEntity(itemEntityId, position)
                .withPassenger(armorStandEntityId, itemEntityId)
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

    private HologramPosition offsetPosition(HologramPosition position) {
        return position.subtractY(0.55);
    }

    private ItemStack parseItem(String content, Player player) {
        if (content == null || content.isEmpty()) {
            return new ItemStack(Material.STONE);
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#ICON:")) {
            String itemName = content.substring(6).trim();
            
            if (player != null) {
                itemName = PlaceholderUtil.replace(itemName, player);
            }
            
            String upperItemName = itemName.toUpperCase(Locale.ROOT);
            
            if (upperItemName.equals("PLAYER_HEAD") || upperItemName.startsWith("PLAYER_HEAD(") || upperItemName.startsWith("PLAYER_HEAD ")) {
                String playerName = extractPlayerName(itemName);
                if (playerName != null && !playerName.isEmpty()) {
                    return createPlayerHead(playerName);
                } else if (player != null) {
                    return createPlayerHead(player.getName());
                }
                return new ItemStack(Material.PLAYER_HEAD);
            }
            
            if (upperItemName.startsWith("SKULL:") || upperItemName.startsWith("HEAD:")) {
                String skullValue = itemName.substring(itemName.indexOf(':') + 1).trim();
                if (player != null) {
                    skullValue = PlaceholderUtil.replace(skullValue, player);
                }
                if (skullValue.length() > 50) {
                    return HeadTexture.createHeadFromBase64(skullValue);
                }
                return createPlayerHead(skullValue);
            }
            
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                return new ItemStack(material);
            }
            
            if (itemName.contains(":")) {
                String[] parts = itemName.split(":");
                if (parts.length >= 2) {
                    try {
                        material = Material.matchMaterial(parts[0] + ":" + parts[1]);
                        if (material != null) {
                            ItemStack item = new ItemStack(material);
                            if (parts.length >= 3) {
                                try {
                                    int amount = Integer.parseInt(parts[2]);
                                    item.setAmount(amount);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            return item;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return new ItemStack(Material.STONE);
    }
    
    private String extractPlayerName(String itemName) {
        String upperName = itemName.toUpperCase(Locale.ROOT);
        if (upperName.startsWith("PLAYER_HEAD(")) {
            int start = itemName.indexOf('(');
            int end = itemName.indexOf(')');
            if (start != -1 && end != -1 && end > start) {
                return itemName.substring(start + 1, end).trim();
            }
        } else if (upperName.startsWith("PLAYER_HEAD ")) {
            return itemName.substring(12).trim();
        } else if (upperName.equals("PLAYER_HEAD")) {
            return null;
        }
        return itemName;
    }
    
    private ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (playerName == null || playerName.isEmpty()) {
            return head;
        }
        
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }
        return head;
    }
}
