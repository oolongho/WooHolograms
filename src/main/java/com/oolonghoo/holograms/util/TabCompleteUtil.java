package com.oolonghoo.holograms.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TAB 补全工具类
 * 提供各种类型的补全提示
 *
 * @author oolongho
 */
public class TabCompleteUtil {

    // 行类型前缀
    public static final List<String> LINE_TYPE_PREFIXES = Arrays.asList(
            "#ICON:",
            "#HEAD:",
            "#SMALLHEAD:",
            "#ENTITY:",
            "#NEXT",
            "#PREV"
    );

    // 常用变量
    public static final Map<String, String> PLACEHOLDERS = new HashMap<>();
    static {
        PLACEHOLDERS.put("{player}", "玩家名称");
        PLACEHOLDERS.put("{player_name}", "玩家名称");
        PLACEHOLDERS.put("{player_uuid}", "玩家 UUID");
        PLACEHOLDERS.put("{player_displayname}", "玩家显示名称");
        PLACEHOLDERS.put("{player_x}", "玩家 X 坐标");
        PLACEHOLDERS.put("{player_y}", "玩家 Y 坐标");
        PLACEHOLDERS.put("{player_z}", "玩家 Z 坐标");
        PLACEHOLDERS.put("{player_world}", "玩家所在世界");
        PLACEHOLDERS.put("{player_health}", "玩家生命值");
        PLACEHOLDERS.put("{player_level}", "玩家等级");
    }

    // 物品参数
    public static final Map<String, String> ITEM_PARAMS = new HashMap<>();
    static {
        ITEM_PARAMS.put("custom-model-data:", "自定义模型数据 (如 custom-model-data:10000)");
        ITEM_PARAMS.put("custom_model_data:", "自定义模型数据 (如 custom_model_data:10000)");
        ITEM_PARAMS.put("cmd:", "自定义模型数据简写 (如 cmd:10000)");
        ITEM_PARAMS.put("color:", "皮革颜色 (如 color:FF0000)");
        ITEM_PARAMS.put("name:", "自定义名称 (如 name:&6传说之剑)");
        ITEM_PARAMS.put("lore:", "物品描述 (如 lore:&7这是描述)");
        ITEM_PARAMS.put("glow", "发光效果");
        ITEM_PARAMS.put("unbreakable", "无法破坏");
    }

    // 动作类型
    public static final Map<String, String> ACTION_TYPES = new HashMap<>();
    static {
        ACTION_TYPES.put("COMMAND:", "以玩家身份执行命令");
        ACTION_TYPES.put("CONSOLE:", "以控制台身份执行命令");
        ACTION_TYPES.put("MESSAGE:", "发送消息");
        ACTION_TYPES.put("SOUND:", "播放音效");
        ACTION_TYPES.put("TELEPORT:", "传送玩家");
        ACTION_TYPES.put("CONNECT:", "连接到其他服务器");
        ACTION_TYPES.put("NEXT_PAGE:", "下一页");
        ACTION_TYPES.put("PREV_PAGE:", "上一页");
        ACTION_TYPES.put("PAGE:", "跳转到指定页");
    }

    // 点击类型
    public static final List<String> CLICK_TYPES = Arrays.asList(
            "ANY",
            "LEFT",
            "RIGHT",
            "SHIFT_LEFT",
            "SHIFT_RIGHT"
    );

    // 常用物品材质
    public static final List<String> COMMON_MATERIALS = Arrays.asList(
            "DIAMOND_SWORD",
            "DIAMOND_PICKAXE",
            "DIAMOND_AXE",
            "DIAMOND_SHOVEL",
            "DIAMOND_HOE",
            "IRON_SWORD",
            "IRON_PICKAXE",
            "IRON_AXE",
            "IRON_SHOVEL",
            "IRON_HOE",
            "GOLDEN_SWORD",
            "GOLDEN_PICKAXE",
            "GOLDEN_AXE",
            "GOLDEN_SHOVEL",
            "GOLDEN_HOE",
            "STONE_SWORD",
            "STONE_PICKAXE",
            "STONE_AXE",
            "STONE_SHOVEL",
            "STONE_HOE",
            "WOODEN_SWORD",
            "WOODEN_PICKAXE",
            "WOODEN_AXE",
            "WOODEN_SHOVEL",
            "WOODEN_HOE",
            "PLAYER_HEAD",
            "DIAMOND",
            "GOLD_INGOT",
            "IRON_INGOT",
            "EMERALD",
            "REDSTONE",
            "LAPIS_LAZULI",
            "NETHERITE_INGOT",
            "BOOK",
            "ENCHANTED_BOOK",
            "PAPER",
            "COMPASS",
            "CLOCK",
            "MAP",
            "ENDER_PEARL",
            "BLAZE_ROD",
            "BREWING_STAND",
            "CRAFTING_TABLE",
            "FURNACE",
            "CHEST",
            "ENDER_CHEST",
            "ANVIL",
            "GRINDSTONE",
            "SMITHING_TABLE",
            "STONECUTTER",
            "LEATHER_HELMET",
            "LEATHER_CHESTPLATE",
            "LEATHER_LEGGINGS",
            "LEATHER_BOOTS",
            "CHAINMAIL_HELMET",
            "CHAINMAIL_CHESTPLATE",
            "CHAINMAIL_LEGGINGS",
            "CHAINMAIL_BOOTS",
            "IRON_HELMET",
            "IRON_CHESTPLATE",
            "IRON_LEGGINGS",
            "IRON_BOOTS",
            "DIAMOND_HELMET",
            "DIAMOND_CHESTPLATE",
            "DIAMOND_LEGGINGS",
            "DIAMOND_BOOTS",
            "NETHERITE_HELMET",
            "NETHERITE_CHESTPLATE",
            "NETHERITE_LEGGINGS",
            "NETHERITE_BOOTS",
            "GOLDEN_HELMET",
            "GOLDEN_CHESTPLATE",
            "GOLDEN_LEGGINGS",
            "GOLDEN_BOOTS",
            "TURTLE_HELMET",
            "ELYTRA",
            "SHIELD",
            "BOW",
            "CROSSBOW",
            "TRIDENT",
            "ARROW",
            "SPECTRAL_ARROW",
            "TIPPED_ARROW",
            "FIRE_CHARGE",
            "FIREWORK_ROCKET",
            "FIREWORK_STAR",
            "POTION",
            "SPLASH_POTION",
            "LINGERING_POTION",
            "EXPERIENCE_BOTTLE",
            "GLASS_BOTTLE",
            "BUCKET",
            "WATER_BUCKET",
            "LAVA_BUCKET",
            "MILK_BUCKET",
            "COD_BUCKET",
            "SALMON_BUCKET",
            "TROPICAL_FISH_BUCKET",
            "PUFFERFISH_BUCKET",
            "AXOLOTL_BUCKET",
            "POWDER_SNOW_BUCKET",
            "BEETROOT_SOUP",
            "MUSHROOM_STEW",
            "RABBIT_STEW",
            "SUSPICIOUS_STEW",
            "APPLE",
            "GOLDEN_APPLE",
            "ENCHANTED_GOLDEN_APPLE",
            "MELON_SLICE",
            "GLISTERING_MELON_SLICE",
            "SWEET_BERRIES",
            "GLOW_BERRIES",
            "CHORUS_FRUIT",
            "POPPED_CHORUS_FRUIT",
            "BREAD",
            "COOKIE",
            "CAKE",
            "PUMPKIN_PIE",
            "COOKED_BEEF",
            "COOKED_PORKCHOP",
            "COOKED_CHICKEN",
            "COOKED_MUTTON",
            "COOKED_RABBIT",
            "COOKED_COD",
            "COOKED_SALMON",
            "BEEF",
            "PORKCHOP",
            "CHICKEN",
            "MUTTON",
            "RABBIT",
            "COD",
            "SALMON",
            "TROPICAL_FISH",
            "PUFFERFISH",
            "SPIDER_EYE",
            "ROTTEN_FLESH",
            "POISONOUS_POTATO",
            "POTATO",
            "BAKED_POTATO",
            "CARROT",
            "GOLDEN_CARROT",
            "BEETROOT",
            "WHEAT",
            "WHEAT_SEEDS",
            "PUMPKIN_SEEDS",
            "MELON_SEEDS",
            "BEETROOT_SEEDS",
            "BONE_MEAL",
            "BONE",
            "STRING",
            "FEATHER",
            "GUNPOWDER",
            "FLINT",
            "COAL",
            "CHARCOAL",
            "STICK",
            "BAMBOO",
            "SUGAR_CANE",
            "SUGAR",
            "CACTUS",
            "VINE",
            "LILY_PAD",
            "COCOA_BEANS",
            "INK_SAC",
            "GLOW_INK_SAC",
            "COPPER_INGOT",
            "AMETHYST_SHARD",
            "QUARTZ",
            "NETHER_STAR",
            "BLAZE_POWDER",
            "BLAZE_ROD",
            "GHAST_TEAR",
            "ENDER_EYE",
            "SLIME_BALL",
            "SLIME_BLOCK",
            "HONEYCOMB",
            "HONEY_BOTTLE",
            "NAUTILUS_SHELL",
            "HEART_OF_THE_SEA",
            "PHANTOM_MEMBRANE",
            "SCUTE",
            "PRISMARINE_SHARD",
            "PRISMARINE_CRYSTALS",
            "SPONGE",
            "WET_SPONGE",
            "DRAGON_BREATH",
            "SHULKER_SHELL",
            "ECHORITE_SHARD",
            "DISC_FRAGMENT_5"
    );

    // 常用实体类型
    public static final List<String> COMMON_ENTITY_TYPES = Arrays.asList(
            "ZOMBIE",
            "SKELETON",
            "CREEPER",
            "SPIDER",
            "ENDERMAN",
            "WITCH",
            "BLAZE",
            "GHAST",
            "PIG_ZOMBIE",
            "WITHER_SKELETON",
            "STRAY",
            "HUSK",
            "DROWNED",
            "PHANTOM",
            "VINDICATOR",
            "EVOKER",
            "VEX",
            "ILLUSIONER",
            "PILLAGER",
            "RAVAGER",
            "GUARDIAN",
            "ELDER_GUARDIAN",
            "SHULKER",
            "SLIME",
            "MAGMA_CUBE",
            "PIG",
            "COW",
            "SHEEP",
            "CHICKEN",
            "HORSE",
            "DONKEY",
            "MULE",
            "LLAMA",
            "TRADER_LLAMA",
            "WOLF",
            "CAT",
            "OCELOT",
            "FOX",
            "PANDA",
            "RABBIT",
            "POLAR_BEAR",
            "TURTLE",
            "DOLPHIN",
            "PARROT",
            "BEE",
            "STRIDER",
            "HOGLIN",
            "ZOGLIN",
            "AXOLOTL",
            "GOAT",
            "GLOW_SQUID",
            "SQUID",
            "COD",
            "SALMON",
            "TROPICAL_FISH",
            "PUFFERFISH",
            "IRON_GOLEM",
            "SNOW_GOLEM",
            "VILLAGER",
            "WANDERING_TRADER",
            "BAT",
            "ENDER_DRAGON",
            "WITHER",
            "ELDER_GUARDIAN",
            "PLAYER",
            "ITEM",
            "FALLING_BLOCK",
            "PRIMED_TNT",
            "MINECART",
            "BOAT",
            "ARMOR_STAND",
            "ITEM_FRAME",
            "GLOW_ITEM_FRAME",
            "PAINTING",
            "LEASH_HITCH",
            "ENDER_CRYSTAL",
            "EVOKER_FANGS",
            "FISHING_HOOK",
            "LIGHTNING",
            "EXPERIENCE_ORB",
            "FIREWORK",
            "SPECTRAL_ARROW",
            "SHULKER_BULLET",
            "DRAGON_FIREBALL",
            "FIREBALL",
            "SMALL_FIREBALL",
            "WITHER_SKULL",
            "ARROW",
            "SNOWBALL",
            "EGG",
            "TRIDENT",
            "LLAMA_SPIT",
            "POTION",
            "EXPERIENCE_BOTTLE"
    );

    /**
     * 获取行内容补全提示
     */
    public static List<String> getLineContentCompletions(String input) {
        List<String> completions = new ArrayList<>();
        String upperInput = input.toUpperCase();

        // 如果输入为空，显示所有行类型前缀
        if (input.isEmpty()) {
            completions.addAll(LINE_TYPE_PREFIXES);
            return completions;
        }

        // 检查是否正在输入行类型前缀
        for (String prefix : LINE_TYPE_PREFIXES) {
            if (prefix.startsWith(upperInput)) {
                completions.add(prefix);
            }
        }

        // 如果已经输入了 #ICON:，补全物品材质
        if (upperInput.startsWith("#ICON:")) {
            String afterPrefix = input.substring(6);
            String materialPart = afterPrefix.split("\\s+")[0];
            completions.addAll(getMaterialCompletions(materialPart));
        }

        // 如果已经输入了 #HEAD: 或 #SMALLHEAD:，补全头颅类型
        if (upperInput.startsWith("#HEAD:") || upperInput.startsWith("#SMALLHEAD:")) {
            completions.add("PLAYER_HEAD");
            completions.add("URL:");
            completions.add("PLAYER:");
            completions.add("HDB:");
        }

        // 如果已经输入了 #ENTITY:，补全实体类型
        if (upperInput.startsWith("#ENTITY:")) {
            String afterPrefix = input.substring(8);
            completions.addAll(getEntityCompletions(afterPrefix));
        }

        // 补全变量
        String lastPart = input;
        int lastSpace = input.lastIndexOf(' ');
        if (lastSpace >= 0) {
            lastPart = input.substring(lastSpace + 1);
        }
        
        if (lastPart.startsWith("{") || lastPart.startsWith("%")) {
            String varPrefix = lastPart.substring(1).toLowerCase();
            for (String placeholder : PLACEHOLDERS.keySet()) {
                String varName = placeholder.substring(1, placeholder.length() - 1).toLowerCase();
                if (varName.startsWith(varPrefix) || varPrefix.isEmpty()) {
                    completions.add(placeholder);
                    completions.add("%" + placeholder.substring(1, placeholder.length() - 1) + "%");
                }
            }
        }

        return completions;
    }

    /**
     * 获取物品材质补全
     */
    public static List<String> getMaterialCompletions(String input) {
        String upperInput = input.toUpperCase();
        return COMMON_MATERIALS.stream()
                .filter(m -> m.startsWith(upperInput))
                .collect(Collectors.toList());
    }

    /**
     * 获取实体类型补全
     */
    public static List<String> getEntityCompletions(String input) {
        String upperInput = input.toUpperCase();
        return COMMON_ENTITY_TYPES.stream()
                .filter(e -> e.startsWith(upperInput))
                .collect(Collectors.toList());
    }

    /**
     * 获取动作类型补全
     */
    public static List<String> getActionCompletions(String input) {
        String upperInput = input.toUpperCase();
        return ACTION_TYPES.keySet().stream()
                .filter(a -> a.startsWith(upperInput))
                .collect(Collectors.toList());
    }

    /**
     * 获取物品参数补全
     */
    public static List<String> getItemParamCompletions(String input) {
        String lowerInput = input.toLowerCase();
        return ITEM_PARAMS.keySet().stream()
                .filter(p -> p.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }

    /**
     * 获取变量补全
     */
    public static List<String> getPlaceholderCompletions(String input) {
        List<String> completions = new ArrayList<>();
        for (String placeholder : PLACEHOLDERS.keySet()) {
            completions.add(placeholder);
        }
        return completions;
    }

    /**
     * 获取帮助信息
     */
    public static List<String> getHelpMessages() {
        List<String> messages = new ArrayList<>();
        messages.add("&e========== 行内容格式帮助 ==========");
        messages.add("&7行类型:");
        messages.add("&f#ICON:<材质> &7- 显示物品图标");
        messages.add("&f#HEAD:<类型> &7- 显示大头颅");
        messages.add("&f#SMALLHEAD:<类型> &7- 显示小头颅");
        messages.add("&f#ENTITY:<类型> &7- 显示实体");
        messages.add("&f#NEXT &7- 下一页按钮");
        messages.add("&f#PREV &7- 上一页按钮");
        messages.add("");
        messages.add("&7头颅类型:");
        messages.add("&fPLAYER_HEAD (玩家名) &7- 玩家头颅");
        messages.add("&fURL (Base64) &7- URL 材质");
        messages.add("&fHDB (ID) &7- HeadDatabase");
        messages.add("");
        messages.add("&7变量:");
        messages.add("&f{player} &7- 玩家名称");
        messages.add("&f%player_name% &7- PlaceholderAPI 变量");
        messages.add("");
        messages.add("&7物品参数:");
        messages.add("&fcustom-model-data:<值> &7- 自定义模型");
        messages.add("&fcolor:<RGB> &7- 皮革颜色");
        messages.add("&fname:<名称> &7- 自定义名称");
        messages.add("&fglow &7- 发光效果");
        messages.add("&e====================================");
        return messages;
    }
}
