package com.oolongho.holograms.util;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.hologram.HeadTexture;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * #ICON: 行共享解析器
 *
 * <p>将行内容拆分为「基础 ID + 尾参数」并解析为 {@link ItemStack}，
 * 吸收原 {@code ItemNbtUtil} 声明的物品参数语义：
 * {@code custom-model-data:}/{@code cmd:}、{@code color:}、{@code name:}、
 * {@code lore:}、{@code glow}、{@code unbreakable}。</p>
 *
 * <p>ID 解析顺序（保持旧行为不变）：
 * 特殊头颅形式 → CraftEngine 自定义物品（含 {@code :} 且 CE 在线且命中）→
 * 原版 Material → {@code minecraft:material[:amount]} 三段式 → 未识别。</p>
 *
 * <p>兼容旧格式：{@code :glow} / 空格 {@code glow} 后缀（实体发光标志由调用方使用），
 * {@code PLAYER_HEAD} / {@code PLAYER_HEAD(name)} / {@code PLAYER_HEAD name} / {@code SKULL:} / {@code HEAD:}。</p>
 *
 * @author oolongho
 */
public final class ItemLineParser {

    /**
     * 解析结果
     *
     * @param item     解析出的物品（未识别时为 STONE 兜底）
     * @param glow     glow 参数标志（供调用方设置实体发光，兼容旧行为）
     * @param resolved 基础 ID 是否被识别（false 表示走了兜底）
     */
    public record Result(ItemStack item, boolean glow, boolean resolved) {}

    private ItemLineParser() {
    }

    /**
     * 解析 #ICON: 行内容
     *
     * @param content 完整行内容（以 #ICON: 开头）
     * @param player  观看者（用于占位符替换与默认头颅），可为 null
     * @return 解析结果，永不返回 null
     */
    public static Result parse(String content, Player player) {
        if (content == null || content.isEmpty()) {
            return new Result(new ItemStack(Material.STONE), false, false);
        }

        String upperContent = content.toUpperCase(Locale.ROOT);
        if (!upperContent.startsWith("#ICON:")) {
            return new Result(new ItemStack(Material.STONE), false, false);
        }

        String body = content.substring(6).trim();

        // 兼容旧 ":glow" 后缀格式（如 DIAMOND:glow），保留实体发光标志（旧行为）
        boolean legacyGlow = body.toLowerCase(Locale.ROOT).endsWith(":glow");
        if (legacyGlow) {
            body = body.substring(0, body.length() - 5);
        }

        // 占位符替换（ID 与参数均生效，与旧解析路径一致）
        if (player != null) {
            body = PlaceholderUtil.replace(body, player);
        }

        String upper = body.toUpperCase(Locale.ROOT);

        // 特殊头颅形式优先，保持旧语义（参数不参与头颅值解析）
        ItemStack head = parseHeadForm(body, upper, player);
        if (head != null) {
            return new Result(head, legacyGlow, true);
        }

        // 空格拆分：首个 token 为基础 ID，其余为参数
        String[] tokens = body.split("\\s+");
        String baseId = tokens[0];
        List<String> params = new ArrayList<>(tokens.length - 1);
        for (int i = 1; i < tokens.length; i++) {
            params.add(tokens[i]);
        }

        ItemStack item = resolveBaseItem(baseId);
        boolean resolved = item != null;
        if (item == null) {
            item = new ItemStack(Material.STONE);
        }
        applyParams(item, params);

        return new Result(item, legacyGlow || hasGlowParam(params), resolved);
    }

    /**
     * 解析特殊头颅形式
     *
     * @return 头颅物品；非头颅形式返回 null
     */
    private static ItemStack parseHeadForm(String body, String upper, Player player) {
        if (upper.equals("PLAYER_HEAD")) {
            return player != null ? createPlayerHead(player.getName())
                    : new ItemStack(Material.PLAYER_HEAD);
        }
        if (upper.startsWith("PLAYER_HEAD(")) {
            int start = body.indexOf('(');
            int end = body.indexOf(')');
            if (start != -1 && end != -1 && end > start) {
                return createPlayerHead(body.substring(start + 1, end).trim());
            }
            // 括号无效：保持旧行为，整个字符串作为玩家名
            return createPlayerHead(body);
        }
        if (upper.startsWith("PLAYER_HEAD ")) {
            return createPlayerHead(body.substring(12).trim());
        }
        if (upper.startsWith("SKULL:") || upper.startsWith("HEAD:")) {
            String skullValue = body.substring(body.indexOf(':') + 1).trim();
            if (skullValue.length() > 50) {
                return HeadTexture.createHeadFromBase64(skullValue);
            }
            return createPlayerHead(skullValue);
        }
        return null;
    }

    /**
     * 解析基础 ID：CE 自定义物品 → 原版 Material → 旧三段式
     *
     * @return 物品；未识别返回 null
     */
    private static ItemStack resolveBaseItem(String baseId) {
        // CraftEngine 自定义物品（namespace:path，需 CE 在线）
        if (baseId.indexOf(':') >= 0) {
            var hook = WooHolograms.getInstance().getCraftEngineHook();
            if (hook != null && hook.isReady()) {
                ItemStack ceItem = hook.resolveItem(baseId);
                if (ceItem != null) {
                    return ceItem;
                }
            }
        }

        // 原版 Material
        Material material = Material.matchMaterial(baseId);
        if (material != null) {
            return new ItemStack(material);
        }

        // 旧三段式 minecraft:material[:amount]
        if (baseId.contains(":")) {
            String[] parts = baseId.split(":");
            if (parts.length >= 2) {
                material = Material.matchMaterial(parts[0] + ":" + parts[1]);
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    if (parts.length >= 3) {
                        try {
                            item.setAmount(Math.max(1, Integer.parseInt(parts[2])));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return item;
                }
            }
        }

        return null;
    }

    /**
     * 应用尾参数到物品
     *
     * @param item   物品（原地修改）
     * @param params 参数 token 列表
     */
    private static void applyParams(ItemStack item, List<String> params) {
        if (item == null || params.isEmpty()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        boolean changed = false;
        List<Component> lore = null;

        int i = 0;
        while (i < params.size()) {
            String token = params.get(i);
            int colon = token.indexOf(':');
            String key = (colon > 0 ? token.substring(0, colon) : token)
                    .toLowerCase(Locale.ROOT).replace('-', '_');
            String value = colon > 0 ? token.substring(colon + 1) : "";

            switch (key) {
                case "custom_model_data", "custommodeldata", "cmd" -> {
                    try {
                        meta.setCustomModelData(Integer.parseInt(value));
                        changed = true;
                    } catch (NumberFormatException ignored) {
                    }
                    i++;
                }
                case "color" -> {
                    Color color = parseColor(value);
                    if (color != null && meta instanceof LeatherArmorMeta leather) {
                        leather.setColor(color);
                        changed = true;
                    }
                    i++;
                }
                case "name", "lore" -> {
                    // 文本值支持空格：合并到下一个已知参数 token 前
                    StringBuilder text = new StringBuilder(value);
                    int j = i + 1;
                    while (j < params.size() && !isKnownParam(params.get(j))) {
                        text.append(' ').append(params.get(j));
                        j++;
                    }
                    if (key.equals("name")) {
                        meta.displayName(ColorUtil.toComponent(text.toString()));
                    } else {
                        lore = lore != null ? lore : new ArrayList<>();
                        for (String line : text.toString().split("\\\\n|\n")) {
                            lore.add(ColorUtil.toComponent(line));
                        }
                        meta.lore(lore);
                    }
                    changed = true;
                    i = j;
                }
                case "glow" -> {
                    if (value.isEmpty() || Boolean.parseBoolean(value)) {
                        // 附魔光效：LURE 附魔 + 隐藏附魔列表（与 GUI 按钮发光方案一致）
                        meta.addEnchant(Enchantment.LURE, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        changed = true;
                    }
                    i++;
                }
                case "unbreakable" -> {
                    if (value.isEmpty() || Boolean.parseBoolean(value)) {
                        meta.setUnbreakable(true);
                        changed = true;
                    }
                    i++;
                }
                default -> i++;
            }
        }

        if (changed) {
            item.setItemMeta(meta);
        }
    }

    /**
     * 检查参数列表是否含 glow 参数
     */
    private static boolean hasGlowParam(List<String> params) {
        for (String token : params) {
            int colon = token.indexOf(':');
            String key = (colon > 0 ? token.substring(0, colon) : token).toLowerCase(Locale.ROOT);
            if (key.equals("glow")) {
                return colon <= 0 || Boolean.parseBoolean(token.substring(colon + 1));
            }
        }
        return false;
    }

    /**
     * 判断 token 是否为已知参数（含 {@code glow:true} 形式）
     */
    private static boolean isKnownParam(String token) {
        int colon = token.indexOf(':');
        String key = (colon > 0 ? token.substring(0, colon) : token)
                .toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (key) {
            case "custom_model_data", "custommodeldata", "cmd", "color",
                    "name", "lore", "glow", "unbreakable" -> true;
            default -> false;
        };
    }

    /**
     * 解析颜色值（支持 #RRGGBB / 0xRRGGBB / RRGGBB）
     *
     * @return 颜色；无效返回 null
     */
    private static Color parseColor(String value) {
        try {
            String hex = value.startsWith("#") ? value.substring(1)
                    : (value.startsWith("0x") || value.startsWith("0X")) ? value.substring(2)
                    : value;
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 创建指定玩家的头颅
     */
    private static ItemStack createPlayerHead(String playerName) {
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
