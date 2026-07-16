package com.oolongho.holograms.nms.versions.renderer;

import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.HeadTexture;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.nms.renderer.NmsItemDisplayRenderer;
import com.oolongho.holograms.nms.util.HologramPosition;
import com.oolongho.holograms.nms.versions.EntityIdGenerator;
import com.oolongho.holograms.nms.versions.EntityMetadataBuilder;
import com.oolongho.holograms.nms.versions.EntityPacketsBuilder;
import com.oolongho.holograms.util.PlaceholderUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ItemDisplay 物品全息图渲染器实现
 * 使用 ItemDisplay 实体替代 ArmorStand+Item 骑乘方案
 * 支持附魔光效、Billboard、Display 属性
 */
public class ItemDisplayRendererImpl implements NmsItemDisplayRenderer {

    private final int entityId;
    private volatile boolean destroyed = false;
    private float currentYaw = 0.0f;
    private float currentPitch = 0.0f;
    private final Map<UUID, String> lastContentPerPlayer = new ConcurrentHashMap<>();

    public ItemDisplayRendererImpl(EntityIdGenerator entityIdGenerator) {
        this.entityId = entityIdGenerator.getFreeEntityId();
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

        ItemStack item = parseItem(line.getContent(), player);
        String rawContent = line.getContent();
        if (rawContent != null && player != null) {
            lastContentPerPlayer.put(player.getUniqueId(), PlaceholderUtil.replace(rawContent, player));
        }

        Hologram hologram = line.getHologram();
        Billboard billboard = line.getBillboard() != null ? line.getBillboard()
                : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);
        float hologramFacing = hologram != null ? hologram.getFacing() : 0f;

        // 计算朝向
        float yaw;
        float pitch;
        Float customYaw = line.getCustomYaw();
        Float customPitch = line.getCustomPitch();

        if (billboard == Billboard.FIXED_ANGLE) {
            yaw = customYaw != null ? customYaw : hologramFacing;
            pitch = customPitch != null ? customPitch : 0;
        } else {
            yaw = customYaw != null ? customYaw : location.getYaw();
            pitch = customPitch != null ? customPitch : location.getPitch();
        }

        this.currentYaw = yaw;
        this.currentPitch = pitch;

        // 构建元数据：ItemDisplay 需要可见（不设置 invisible），无重力，Billboard，物品，Display 属性
        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withBillboard(billboard)
                .withItemDisplayItem(item)
                .withDisplayProperties(line, hologram);

        // 附魔光效：检查内容中是否包含 glow 参数
        if (hasGlow(line.getContent())) {
            metadataBuilder.withGlow();
        }

        HologramPosition position = HologramPosition.fromLocation(location);

        EntityPacketsBuilder.create()
                .withSpawnEntity(entityId, org.bukkit.entity.EntityType.ITEM_DISPLAY,
                        position, yaw, pitch)
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
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

        Hologram hologram = line.getHologram();
        Billboard billboard = line.getBillboard() != null ? line.getBillboard()
                : (hologram != null ? hologram.getBillboard() : Billboard.CENTER);

        EntityMetadataBuilder metadataBuilder = EntityMetadataBuilder.create()
                .withNoGravity()
                .withBillboard(billboard)
                .withItemDisplayItem(item)
                .withDisplayProperties(line, hologram);

        if (hasGlow(line.getContent())) {
            metadataBuilder.withGlow();
        }

        EntityPacketsBuilder.create()
                .withEntityMetadata(entityId, metadataBuilder.toWatchableObjects())
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
        EntityPacketsBuilder.create()
                .withTeleportEntity(entityId, new HologramPosition(
                        location.getX(), location.getY(), location.getZ(),
                        currentYaw, currentPitch))
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
        currentYaw = 0.0f;
        currentPitch = 0.0f;
        lastContentPerPlayer.clear();
    }

    /**
     * 检查内容是否包含 glow 参数
     * 格式：#ICON:ITEM_NAME:glow 或 #ICON:ITEM_NAME glow
     */
    private boolean hasGlow(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase(Locale.ROOT);
        return lower.contains(":glow") || lower.contains(" glow");
    }

    /**
     * 解析物品内容
     * 复用 IconHologramRendererImpl 的解析逻辑
     */
    private ItemStack parseItem(String content, Player player) {
        if (content == null || content.isEmpty()) {
            return new ItemStack(Material.STONE);
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#ICON:")) {
            String itemName = content.substring(6).trim();

            // 移除 glow 标记，不影响物品解析
            itemName = removeGlowTag(itemName);

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

    /**
     * 移除 glow 标记，避免影响物品名称解析
     */
    private String removeGlowTag(String itemName) {
        // 移除 ":glow" 后缀（如 DIAMOND:glow）
        if (itemName.toLowerCase(Locale.ROOT).endsWith(":glow")) {
            return itemName.substring(0, itemName.length() - 5);
        }
        // 移除 " glow" 后缀（如 DIAMOND glow）
        String lower = itemName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(" glow")) {
            return itemName.substring(0, itemName.length() - 5);
        }
        return itemName;
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
