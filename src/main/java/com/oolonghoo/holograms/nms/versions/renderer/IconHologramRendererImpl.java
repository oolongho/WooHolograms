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
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
                        .withNoGravity()
                        .withArmorStandProperties(false, true)
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
        if (location == null || line == null) {
            return;
        }

        ItemStack item = parseItem(line.getContent());
        DecentPosition position = DecentPosition.fromLocation(location);

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
        ItemStack item = parseItem(line.getContent());
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
                .withTeleportEntity(armorStandEntityId, offsetPosition(position))
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

    private DecentPosition offsetPosition(DecentPosition position) {
        return position.subtractY(0.55);
    }

    private ItemStack parseItem(String content) {
        if (content == null || content.isEmpty()) {
            return new ItemStack(Material.STONE);
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (upperContent.startsWith("#ICON:")) {
            String itemName = content.substring(6).trim();
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
}
