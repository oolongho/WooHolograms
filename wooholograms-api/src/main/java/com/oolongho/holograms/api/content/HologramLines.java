package com.oolongho.holograms.api.content;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * 行内容工厂
 *
 * <p>用类型化方式构造行内容字符串，替代手写 {@code #ICON:} 等魔法前缀。
 * 产物即 {@code HologramPage#addLine(String)} / {@code HologramBuilder#line(String)}
 * 接受的内容格式。</p>
 *
 * <pre>{@code
 * page.addLine(HologramLines.text("&a在线玩家: %playerlist%"));
 * page.addLine(HologramLines.icon(Material.DIAMOND));
 * page.addLine(HologramLines.block(Material.BEACON));
 * page.addLine(HologramLines.head("Notch"));
 * }</pre>
 */
public final class HologramLines {

    private HologramLines() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 普通文本行。支持 {@code &} 颜色代码、MiniMessage 风格标签、
     * PlaceholderAPI 占位符与内置占位符（{@code {player}}、{@code {page}}、{@code {pages}}）
     *
     * @param content 文本内容
     * @return 内容字符串
     */
    public static String text(@NotNull String content) {
        return content;
    }

    /**
     * 物品图标行（ItemDisplay 掉落物形态）
     *
     * @param material 原版物品
     * @return 内容字符串
     */
    public static String icon(@NotNull Material material) {
        return "#ICON:" + material.name();
    }

    /**
     * 自定义物品图标行（需服务器安装 CraftEngine）
     *
     * @param itemId CE 物品 ID（格式 namespace:path）
     * @return 内容字符串
     */
    public static String iconCustom(@NotNull String itemId) {
        return "#ICON:" + itemId;
    }

    /**
     * 玩家头颅行（正常大小）
     *
     * @param value 头颅来源：玩家名、Base64 纹理串或 {@code HDB:<id>}（需 HeadDatabase）
     * @return 内容字符串
     */
    public static String head(@NotNull String value) {
        return "#HEAD:" + value;
    }

    /**
     * 玩家头颅行（小尺寸）
     *
     * @param value 头颅来源：玩家名、Base64 纹理串或 {@code HDB:<id>}（需 HeadDatabase）
     * @return 内容字符串
     */
    public static String smallHead(@NotNull String value) {
        return "#SMALLHEAD:" + value;
    }

    /**
     * 方块行（BlockDisplay 渲染）
     *
     * @param material 原版方块
     * @return 内容字符串
     */
    public static String block(@NotNull Material material) {
        if (!material.isBlock()) {
            throw new IllegalArgumentException("材质不是方块: " + material);
        }
        return "#BLOCK:" + material.name();
    }

    /**
     * 自定义方块行（需服务器安装 CraftEngine）
     *
     * @param blockId CE 方块 ID（格式 namespace:path）
     * @return 内容字符串
     */
    public static String blockCustom(@NotNull String blockId) {
        return "#BLOCK:" + blockId;
    }

    /**
     * 实体行（实体渲染）
     *
     * @param type Bukkit 实体类型
     * @return 内容字符串
     */
    public static String entity(@NotNull EntityType type) {
        return "#ENTITY:" + type.name();
    }

    /**
     * 将文本包裹为动画
     *
     * @param name    动画名称（内置：wave、typewriter、blink、scroll、gradient；或自定义动画）
     * @param content 动画作用的文本
     * @param args    动画参数（以逗号拼接为单个参数串；可为空）
     * @return 内容字符串
     */
    public static String animation(@NotNull String name, @NotNull String content, String... args) {
        StringBuilder sb = new StringBuilder("<#ANIM:").append(name);
        if (args != null && args.length > 0) {
            sb.append(':').append(String.join(",", args));
        }
        sb.append('>').append(content).append("</#ANIM>");
        return sb.toString();
    }
}
