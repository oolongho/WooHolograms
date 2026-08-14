package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Billboard;
import com.oolongho.holograms.hologram.Brightness;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.hologram.TextAlignment;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据转换命令
 * 支持从 HolographicDisplays、CMI、FancyHolograms 插件导入全息图数据
 * /wh convert holographicdisplays 或 /wh convert hd 或 /wh convert cmi
 * /wh convert fancyholograms 或 /wh convert fh
 *
 */
public class ConvertCommand extends Subcommand {

    private final WooHolograms plugin;

    public ConvertCommand(WooHolograms plugin) {
        super("convert", "cmd.desc-convert", "cmd.usage-convert", "wooholograms.convert");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.getMessages().send(sender, "convert.usage");
            return true;
        }

        String source = args[0].toLowerCase();
        switch (source) {
            case "holographicdisplays", "hd" -> convertHolographicDisplays(sender);
            case "cmi" -> convertCMI(sender);
            case "fancyholograms", "fh" -> convertFancyHolograms(sender);
            default -> plugin.getMessages().send(sender, "convert.unknown-source", "source", args[0]);
        }
        return true;
    }

    /**
     * 从 HolographicDisplays 2.x 导入全息图数据
     * HD 2.x 的数据存储在 plugins/HolographicDisplays/ 目录下的 YAML 文件中
     */
    private void convertHolographicDisplays(CommandSender sender) {
        File pluginsDir = plugin.getDataFolder().getParentFile();
        File hdDir = new File(pluginsDir, "HolographicDisplays");

        if (!hdDir.exists() || !hdDir.isDirectory()) {
            plugin.getMessages().send(sender, "convert.hd-not-found");
            return;
        }

        // 检查是否为 HD 3.x（数据库存储）
        File databaseFile = new File(hdDir, "database.db");
        if (databaseFile.exists()) {
            plugin.getMessages().send(sender, "convert.hd-v3-unsupported");
            return;
        }

        // 扫描所有 .yml 文件
        File[] ymlFiles = hdDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (ymlFiles == null || ymlFiles.length == 0) {
            plugin.getMessages().send(sender, "convert.hd-no-data");
            return;
        }

        int totalFiles = ymlFiles.length;
        int imported = 0;
        int skipped = 0;
        int failed = 0;

        plugin.getMessages().send(sender, "convert.hd-start", "count", String.valueOf(totalFiles));

        for (File ymlFile : ymlFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(ymlFile);

            for (String holoName : config.getKeys(false)) {
                // 检查名称合法性
                if (!plugin.getHologramManager().isValidName(holoName)) {
                    plugin.getLogger().warning("跳过非法名称的全息图: " + holoName);
                    skipped++;
                    continue;
                }

                // 检查是否已存在
                if (plugin.getHologramManager().containsHologram(holoName)) {
                    plugin.getLogger().warning("跳过已存在的全息图: " + holoName);
                    skipped++;
                    continue;
                }

                // 解析位置
                String locationStr = config.getString(holoName + ".location");
                Location location = parseHdLocation(locationStr);
                if (location == null) {
                    plugin.getLogger().warning("跳过位置无效的全息图: " + holoName + " (location: " + locationStr + ")");
                    failed++;
                    continue;
                }

                // 解析行内容
                List<String> lines = config.getStringList(holoName + ".lines");
                if (lines.isEmpty()) {
                    plugin.getLogger().warning("跳过无内容的全息图: " + holoName);
                    skipped++;
                    continue;
                }

                // 创建全息图
                Hologram hologram = plugin.getHologramManager().createHologram(holoName, location);
                if (hologram == null) {
                    failed++;
                    continue;
                }

                // 添加行内容
                HologramPage page = hologram.getPage(0);
                if (page != null) {
                    for (String line : lines) {
                        // HD 的 #ICON: 行格式与 WooHolograms 兼容
                        page.addLine(line);
                    }
                }

                hologram.save();
                imported++;
            }
        }

        // 输出统计结果
        plugin.getMessages().send(sender, "convert.hd-result",
                "imported", String.valueOf(imported),
                "skipped", String.valueOf(skipped),
                "failed", String.valueOf(failed));
    }

    /**
     * 通用位置解析方法
     * 支持自定义分隔符，统一处理位置字符串解析
     *
     * @param locationStr 位置字符串，格式为 world<delim>x<delim>y<delim>z
     * @param delimiter 分隔符正则（如 "," 或 ":"）
     * @return Location 对象，解析失败返回 null
     */
    private Location parseLocation(String locationStr, String delimiter) {
        if (locationStr == null || locationStr.isEmpty()) {
            return null;
        }

        String[] parts = locationStr.split(delimiter);
        if (parts.length < 4) {
            return null;
        }

        try {
            String worldName = parts[0].trim();
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }

            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析 HD 的位置格式: world,x,y,z
     *
     * @param locationStr 位置字符串
     * @return Location 对象，解析失败返回 null
     */
    private Location parseHdLocation(String locationStr) {
        return parseLocation(locationStr, ",");
    }

    /**
     * 从 CMI 导入全息图数据
     * CMI 的数据存储在 plugins/CMI/Saves/holograms.yml 或 plugins/CMI/holograms.yml
     */
    private void convertCMI(CommandSender sender) {
        File pluginsDir = plugin.getDataFolder().getParentFile();

        // 优先查找 Saves 子目录下的文件，回退到 CMI 根目录
        File cmiFile = new File(pluginsDir, "CMI/Saves/holograms.yml");
        if (!cmiFile.exists()) {
            cmiFile = new File(pluginsDir, "CMI/holograms.yml");
        }

        if (!cmiFile.exists()) {
            plugin.getMessages().send(sender, "convert.cmi-not-found");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(cmiFile);

        int imported = 0;
        int skipped = 0;
        int failed = 0;

        for (String holoName : config.getKeys(false)) {
            // 跳过 CMI 自动生成的翻页按钮全息图
            if (holoName.endsWith("#>") || holoName.endsWith("#<")) {
                plugin.getLogger().info("跳过 CMI 自动生成的翻页按钮全息图: " + holoName);
                skipped++;
                continue;
            }

            // 检查名称合法性
            if (!plugin.getHologramManager().isValidName(holoName)) {
                plugin.getLogger().warning("跳过非法名称的全息图: " + holoName);
                skipped++;
                continue;
            }

            // 检查是否已存在
            if (plugin.getHologramManager().containsHologram(holoName)) {
                plugin.getLogger().warning("跳过已存在的全息图: " + holoName);
                skipped++;
                continue;
            }

            // 解析位置：CMI 格式为 world;x;y;z，需将分号替换为冒号后解析
            String locationStr = config.getString(holoName + ".Loc");
            Location location = parseCmiLocation(locationStr);
            if (location == null) {
                plugin.getLogger().warning("跳过位置无效的全息图: " + holoName + " (Loc: " + locationStr + ")");
                failed++;
                continue;
            }

            // 解析行内容
            List<String> rawLines = config.getStringList(holoName + ".Lines");
            if (rawLines.isEmpty()) {
                plugin.getLogger().warning("跳过无内容的全息图: " + holoName);
                skipped++;
                continue;
            }

            // 按 !nextpage! 分割为多页
            List<List<String>> pages = new ArrayList<>();
            List<String> currentPage = new ArrayList<>();
            for (String line : rawLines) {
                if (line.equalsIgnoreCase("!nextpage!")) {
                    pages.add(currentPage);
                    currentPage = new ArrayList<>();
                } else {
                    // CMI 的 ICON: 行需要加 # 前缀以兼容 WooHolograms
                    if (line.toUpperCase().startsWith("ICON:")) {
                        line = "#" + line;
                    }
                    currentPage.add(line);
                }
            }
            if (!currentPage.isEmpty()) {
                pages.add(currentPage);
            }

            if (pages.isEmpty()) {
                skipped++;
                continue;
            }

            // 创建全息图
            Hologram hologram = plugin.getHologramManager().createHologram(holoName, location);
            if (hologram == null) {
                failed++;
                continue;
            }

            // 第一页内容
            HologramPage firstPage = hologram.getPage(0);
            if (firstPage != null) {
                for (String line : pages.get(0)) {
                    firstPage.addLine(line);
                }
            }

            // 多页时添加后续页面和翻页行
            boolean hasMultiplePages = pages.size() > 1;
            for (int i = 1; i < pages.size(); i++) {
                HologramPage newPage = hologram.addPage();
                for (String line : pages.get(i)) {
                    newPage.addLine(line);
                }
            }

            // 多页时在每页末尾添加翻页行（TEXT + 翻页动作）
            if (hasMultiplePages) {
                for (int i = 0; i < hologram.getPageCount(); i++) {
                    HologramPage page = hologram.getPage(i);
                    if (page == null) continue;

                    if (i > 0) {
                        HologramLine prevLine = page.addLine(plugin.getMessages().getRaw("gui.convert.prev-page"));
                        if (prevLine != null) {
                            prevLine.addAction(ClickType.ANY, new Action(ActionType.PREV_PAGE, hologram.getName()));
                        }
                    }
                    if (i < hologram.getPageCount() - 1) {
                        HologramLine nextLine = page.addLine(plugin.getMessages().getRaw("gui.convert.next-page"));
                        if (nextLine != null) {
                            nextLine.addAction(ClickType.ANY, new Action(ActionType.NEXT_PAGE, hologram.getName()));
                        }
                    }
                }
            }

            hologram.save();
            imported++;
        }

        // 输出统计结果
        plugin.getMessages().send(sender, "convert.cmi-result",
                "imported", String.valueOf(imported),
                "skipped", String.valueOf(skipped),
                "failed", String.valueOf(failed));
    }

    /**
     * 解析 CMI 的位置格式: world;x;y;z（分号分隔）
     * 内部将分号替换为冒号后按 world:x:y:z 解析
     *
     * @param locationStr 位置字符串
     * @return Location 对象，解析失败返回 null
     */
    private Location parseCmiLocation(String locationStr) {
        // CMI 使用分号分隔，替换为冒号后统一解析
        String normalized = locationStr.replace(";", ":");
        return parseLocation(normalized, ":");
    }

    /**
     * 从 FancyHolograms v2 导入全息图数据
     * 数据存储在 plugins/FancyHolograms/holograms.yml，顶层 version 必须为 2
     */
    private void convertFancyHolograms(CommandSender sender) {
        File fhFile = new File(plugin.getDataFolder().getParentFile(), "FancyHolograms/holograms.yml");

        if (!fhFile.exists()) {
            plugin.getMessages().send(sender, "convert.fh-not-found");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(fhFile);

        // v1 旧格式字段结构完全不同，不支持
        if (config.getInt("version", 1) != 2) {
            plugin.getMessages().send(sender, "convert.fh-v1-unsupported");
            return;
        }

        ConfigurationSection hologramsSection = config.getConfigurationSection("holograms");
        if (hologramsSection == null) {
            plugin.getMessages().send(sender, "convert.fh-no-data");
            return;
        }

        int imported = 0;
        int skipped = 0;
        int failed = 0;

        plugin.getMessages().send(sender, "convert.fh-start", "count", String.valueOf(hologramsSection.getKeys(false).size()));

        for (String holoName : hologramsSection.getKeys(false)) {
            // 检查名称合法性
            if (!plugin.getHologramManager().isValidName(holoName)) {
                plugin.getLogger().warning("跳过非法名称的全息图: " + holoName);
                skipped++;
                continue;
            }

            // 检查是否已存在
            if (plugin.getHologramManager().containsHologram(holoName)) {
                plugin.getLogger().warning("跳过已存在的全息图: " + holoName);
                skipped++;
                continue;
            }

            ConfigurationSection holoSection = hologramsSection.getConfigurationSection(holoName);
            if (holoSection == null) {
                plugin.getLogger().warning("跳过数据节缺失的全息图: " + holoName);
                failed++;
                continue;
            }

            // 解析类型，FH v2 仅支持 TEXT/ITEM/BLOCK
            String type = holoSection.getString("type");
            if (type == null || (!type.equalsIgnoreCase("TEXT") && !type.equalsIgnoreCase("ITEM") && !type.equalsIgnoreCase("BLOCK"))) {
                plugin.getLogger().warning("跳过类型无法识别的全息图: " + holoName + " (type: " + type + ")");
                failed++;
                continue;
            }

            // 解析行内容，TEXT 无文本行则无内容可导入
            List<String> textLines = holoSection.getStringList("text");
            if (type.equalsIgnoreCase("TEXT") && textLines.isEmpty()) {
                plugin.getLogger().warning("跳过无内容的全息图: " + holoName);
                skipped++;
                continue;
            }

            // 解析位置
            String worldName = holoSection.getString("location.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("跳过世界未加载的全息图: " + holoName + " (world: " + worldName + ")");
                failed++;
                continue;
            }
            Location location = new Location(world,
                    holoSection.getDouble("location.x", 0),
                    holoSection.getDouble("location.y", 0),
                    holoSection.getDouble("location.z", 0),
                    (float) holoSection.getDouble("location.yaw", 0),
                    (float) holoSection.getDouble("location.pitch", 0));

            // 创建全息图
            Hologram hologram = plugin.getHologramManager().createHologram(holoName, location);
            if (hologram == null) {
                failed++;
                continue;
            }

            // 按类型生成行内容
            HologramPage page = hologram.getPage(0);
            if (page == null) {
                plugin.getLogger().warning("跳过无法获取页面的全息图: " + holoName);
                failed++;
                continue;
            }
            switch (type.toUpperCase()) {
                case "TEXT" -> {
                    for (String line : textLines) {
                        page.addLine(line);
                    }
                }
                case "ITEM" -> {
                    // 仅取材质，物品 meta 不迁移
                    ItemStack item = holoSection.getItemStack("item", new ItemStack(Material.APPLE));
                    if (item == null) {
                        item = new ItemStack(Material.APPLE);
                    }
                    if (item.hasItemMeta()) {
                        plugin.getLogger().warning("已忽略全息图 " + holoName + " 物品上的 meta 数据，仅导入材质");
                    }
                    page.addLine("#ICON:" + item.getType().name());
                }
                case "BLOCK" -> {
                    String block = holoSection.getString("block", "GRASS_BLOCK").toUpperCase();
                    if (Material.getMaterial(block) == null) {
                        plugin.getLogger().warning("未知方块类型: " + block + "，已回退为 GRASS_BLOCK: " + holoName);
                        block = "GRASS_BLOCK";
                    }
                    page.addLine("#BLOCK:" + block);
                }
            }

            // 通用属性映射
            hologram.setScale(
                    (float) holoSection.getDouble("scale_x", 1.0),
                    (float) holoSection.getDouble("scale_y", 1.0),
                    (float) holoSection.getDouble("scale_z", 1.0));
            hologram.setTranslation(
                    holoSection.getDouble("translation_x", 0.0),
                    holoSection.getDouble("translation_y", 0.0),
                    holoSection.getDouble("translation_z", 0.0));
            hologram.setShadowRadius((float) holoSection.getDouble("shadow_radius", 0.0));
            hologram.setShadowStrength((float) holoSection.getDouble("shadow_strength", 1.0));
            hologram.setBillboard(Billboard.fromId(holoSection.getString("billboard", "center")));

            // 亮度：任一项设置过才生效，未设置的按 FH 逻辑钳为 0
            int blockLight = Math.min(15, holoSection.getInt("block_brightness", -1));
            int skyLight = Math.min(15, holoSection.getInt("sky_brightness", -1));
            if (blockLight > -1 || skyLight > -1) {
                hologram.setBrightness(Brightness.of(Math.max(0, skyLight), Math.max(0, blockLight)));
            }

            int visibilityDistance = holoSection.getInt("visibility_distance", -1);
            if (visibilityDistance > 0) {
                hologram.setDisplayRange(visibilityDistance);
            }

            // 可见性：PERMISSION_REQUIRED 保留原权限节点以无缝迁移，旧版回退字段 visible_by_default
            String visibility = holoSection.getString("visibility", null);
            if ("PERMISSION_REQUIRED".equals(visibility)) {
                hologram.setPermission("fancyholograms.viewhologram." + holoName);
            } else if ("MANUAL".equals(visibility)) {
                plugin.getLogger().warning("已忽略全息图 " + holoName + " 的 MANUAL 可见性设置");
            } else if (visibility == null && !holoSection.getBoolean("visible_by_default", true)) {
                hologram.setPermission("fancyholograms.viewhologram." + holoName);
            }

            if (holoSection.getString("linkedNpc") != null) {
                plugin.getLogger().warning("已忽略全息图 " + holoName + " 的 linkedNpc（FancyNpcs 关联）");
            }

            // TEXT 专属属性
            if (type.equalsIgnoreCase("TEXT")) {
                if (holoSection.getBoolean("text_shadow", false)) {
                    plugin.getLogger().warning("已忽略全息图 " + holoName + " 的 text_shadow 设置");
                }
                if (holoSection.getBoolean("see_through", false)) {
                    plugin.getLogger().warning("已忽略全息图 " + holoName + " 的 see_through 设置");
                }
                hologram.setAlignment(TextAlignment.fromId(holoSection.getString("text_alignment", "center")));
                int updateInterval = holoSection.getInt("update_text_interval", -1);
                if (updateInterval > 0) {
                    hologram.setUpdateInterval(updateInterval);
                }
                String background = holoSection.getString("background", null);
                if (background != null) {
                    applyFhBackground(hologram, holoName, background);
                }
            }

            hologram.save();
            imported++;
        }

        // 输出统计结果
        plugin.getMessages().send(sender, "convert.fh-result",
                "imported", String.valueOf(imported),
                "skipped", String.valueOf(skipped),
                "failed", String.valueOf(failed));
    }

    /**
     * 解析 FancyHolograms 的背景色并应用到全息图
     * 支持 transparent / #RRGGBB / #AARRGGBB / 命名色（如 red）四种格式
     *
     * @param hologram 目标全息图
     * @param holoName 全息图名称（用于日志定位）
     * @param background 背景色字符串
     */
    private void applyFhBackground(Hologram hologram, String holoName, String background) {
        if (background.equalsIgnoreCase("transparent")) {
            hologram.setBackgroundColor(0);
            hologram.setBackgroundAlpha(0);
            return;
        }

        try {
            if (background.startsWith("#") && background.length() == 7) {
                // #RRGGBB：不透明确色
                long rgb = Long.parseLong(background.substring(1), 16);
                hologram.setBackgroundColor((int) (rgb & 0xFFFFFF));
                hologram.setBackgroundAlpha(255);
            } else if (background.startsWith("#") && background.length() == 9) {
                // #AARRGGBB：高位为 alpha
                int argb = (int) Long.parseLong(background.substring(1), 16);
                hologram.setBackgroundAlpha((argb >>> 24) & 0xFF);
                hologram.setBackgroundColor(argb & 0xFFFFFF);
            } else {
                // 命名色，与 FH 一致使用半透明背景
                NamedTextColor named = NamedTextColor.NAMES.value(background.toLowerCase().trim().replace(' ', '_'));
                if (named != null) {
                    hologram.setBackgroundColor(named.value());
                    hologram.setBackgroundAlpha(200);
                } else {
                    plugin.getLogger().warning("无法解析全息图 " + holoName + " 的背景色: " + background);
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("无法解析全息图 " + holoName + " 的背景色: " + background);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> sources = Arrays.asList("holographicdisplays", "hd", "cmi", "fancyholograms", "fh");
            List<String> result = new ArrayList<>();
            for (String source : sources) {
                if (source.startsWith(input)) {
                    result.add(source);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
}
