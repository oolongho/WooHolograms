package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.action.Action;
import com.oolongho.holograms.action.ActionType;
import com.oolongho.holograms.action.ClickType;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.hologram.Hologram;
import com.oolongho.holograms.hologram.HologramLine;
import com.oolongho.holograms.hologram.HologramPage;
import com.oolongho.holograms.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据转换命令
 * 支持从 HolographicDisplays、CMI 插件导入全息图数据
 * /wh convert holographicdisplays 或 /wh convert hd 或 /wh convert cmi
 *
 */
public class ConvertCommand extends Subcommand {

    private final WooHolograms plugin;

    public ConvertCommand(WooHolograms plugin) {
        super("convert", "从其他插件导入全息图数据", "/wh convert <holographicdisplays|hd|cmi>", "wooholograms.convert");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.usage")));
            return true;
        }

        String source = args[0].toLowerCase();
        switch (source) {
            case "holographicdisplays", "hd" -> convertHolographicDisplays(sender);
            case "cmi" -> convertCMI(sender);
            default -> sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.unknown-source", "source", args[0])));
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
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.hd-not-found")));
            return;
        }

        // 检查是否为 HD 3.x（数据库存储）
        File databaseFile = new File(hdDir, "database.db");
        if (databaseFile.exists()) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.hd-v3-unsupported")));
            return;
        }

        // 扫描所有 .yml 文件
        File[] ymlFiles = hdDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (ymlFiles == null || ymlFiles.length == 0) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.hd-no-data")));
            return;
        }

        int totalFiles = ymlFiles.length;
        int imported = 0;
        int skipped = 0;
        int failed = 0;

        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.hd-start", "count", String.valueOf(totalFiles))));

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
        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.hd-result",
                "imported", String.valueOf(imported),
                "skipped", String.valueOf(skipped),
                "failed", String.valueOf(failed))));
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
            sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.cmi-not-found")));
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
                        HologramLine prevLine = page.addLine("&c< 上一页");
                        if (prevLine != null) {
                            prevLine.addAction(ClickType.ANY, new Action(ActionType.PREV_PAGE, hologram.getName()));
                        }
                    }
                    if (i < hologram.getPageCount() - 1) {
                        HologramLine nextLine = page.addLine("&a下一页 >");
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
        sender.sendMessage(ColorUtil.colorize(plugin.getMessages().getWithPrefix("convert.cmi-result",
                "imported", String.valueOf(imported),
                "skipped", String.valueOf(skipped),
                "failed", String.valueOf(failed))));
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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> sources = Arrays.asList("holographicdisplays", "hd", "cmi");
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
