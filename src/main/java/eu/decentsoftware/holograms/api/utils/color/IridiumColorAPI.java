package eu.decentsoftware.holograms.api.utils.color;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DH 兼容层 - 渐变色/十六进制颜色解析
 * 镜像 DecentHolograms 内置的 IridiumColorAPI（公共 API 与行为一致），
 * 供依赖 DH 的插件（如 CrazyCrates）调用。
 *
 * 面向 Paper/Folia 1.21+，RGB 恒定可用，原版的 Version/ReflectMethod 降级分支已省略。
 */
public class IridiumColorAPI {

    public static final List<String> SPECIAL_COLORS = Arrays.asList("&l", "&n", "&o", "&k", "&m");

    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "[<{]#([A-Fa-f0-9]{6})[}>](((?![<{]#[A-Fa-f0-9]{6}[}>]).)*)[<{]/#([A-Fa-f0-9]{6})[}>]");
    private static final Pattern SOLID_PATTERN = Pattern.compile("[<{]#([A-Fa-f0-9]{6})[}>]|[&§]?#([A-Fa-f0-9]{6})");
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<RAINBOW([0-9]{1,3})>(.*?)</RAINBOW>");

    /** processCached 的 LRU 缓存（容量 100，与原版默认一致） */
    private static final Map<String, String> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(128, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100;
        }
    });

    /** 全部传统颜色，用于最近色降级 */
    private static final Map<Color, ChatColor> COLORS;

    static {
        Map<Color, ChatColor> colors = new HashMap<>();
        colors.put(new Color(0), ChatColor.getByChar('0'));
        colors.put(new Color(170), ChatColor.getByChar('1'));
        colors.put(new Color(43520), ChatColor.getByChar('2'));
        colors.put(new Color(43690), ChatColor.getByChar('3'));
        colors.put(new Color(11141120), ChatColor.getByChar('4'));
        colors.put(new Color(11141290), ChatColor.getByChar('5'));
        colors.put(new Color(16755200), ChatColor.getByChar('6'));
        colors.put(new Color(11184810), ChatColor.getByChar('7'));
        colors.put(new Color(5592405), ChatColor.getByChar('8'));
        colors.put(new Color(5592575), ChatColor.getByChar('9'));
        colors.put(new Color(5635925), ChatColor.getByChar('a'));
        colors.put(new Color(5636095), ChatColor.getByChar('b'));
        colors.put(new Color(16733525), ChatColor.getByChar('c'));
        colors.put(new Color(16733695), ChatColor.getByChar('d'));
        colors.put(new Color(16777045), ChatColor.getByChar('e'));
        colors.put(new Color(16777215), ChatColor.getByChar('f'));
        COLORS = Collections.unmodifiableMap(colors);
    }

    /**
     * 处理字符串并应用缓存。
     */
    public static String processCached(String string) {
        String cached = CACHE.get(string);
        if (cached != null) {
            return cached;
        }
        String result = process(string);
        CACHE.put(string, result);
        return result;
    }

    /**
     * 处理字符串中的渐变、纯色、彩虹标签与 & 颜色代码。
     */
    public static String process(String string) {
        string = processGradients(string);
        string = processSolid(string);
        string = processRainbow(string);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * 批量处理列表中的字符串。
     */
    public static List<String> process(List<String> strings) {
        strings.replaceAll(IridiumColorAPI::process);
        return strings;
    }

    /**
     * 为字符串附加纯色前缀。
     */
    public static String color(String string, Color color) {
        return ChatColor.of(color) + string;
    }

    /**
     * 为字符串应用渐变色，保留特殊样式代码（&l/&n/&o/&k/&m）。
     */
    public static String color(String string, Color start, Color end) {
        StringBuilder specialColors = new StringBuilder();
        for (String color : SPECIAL_COLORS) {
            if (string.contains(color)) {
                specialColors.append(color);
                string = string.replace(color, "");
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        Color[] colors = createGradient(start, end, string.length());
        String[] characters = string.split("");
        for (int i = 0; i < string.length(); i++) {
            ChatColor chatColor = COLORS.get(colors[i]);
            if (chatColor != null) {
                stringBuilder.append(chatColor).append(specialColors).append(characters[i]);
            } else {
                String hex = String.format("§#%06x", (0xFFFFFF & colors[i].getRGB()));
                stringBuilder.append(hex).append(specialColors).append(characters[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 为字符串应用彩虹色。
     *
     * @param saturation 饱和度
     */
    public static String rainbow(String string, float saturation) {
        StringBuilder specialColors = new StringBuilder();
        for (String color : SPECIAL_COLORS) {
            if (string.contains(color)) {
                specialColors.append(color);
                string = string.replace(color, "");
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        Color[] colors = createRainbow(string.length(), saturation);
        String[] characters = string.split("");
        for (int i = 0; i < string.length(); i++) {
            ChatColor chatColor = COLORS.get(colors[i]);
            if (chatColor != null) {
                stringBuilder.append(chatColor).append(specialColors).append(characters[i]);
            } else {
                String hex = String.format("§#%06x", (0xFFFFFF & colors[i].getRGB()));
                stringBuilder.append(hex).append(specialColors).append(characters[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 从十六进制代码获取颜色。
     */
    public static ChatColor getColor(String string) {
        return ChatColor.of(new Color(Integer.parseInt(string, 16)));
    }

    /**
     * 移除字符串中的全部颜色代码（含渐变/纯色/彩虹标签）。
     */
    public static String stripColorFormatting(String string) {
        return string.replaceAll("[&§][a-f0-9lnokm]|<[/]?\\w{5,8}(:[0-9A-F]{6})?>", "");
    }

    /**
     * 获取最接近的传统颜色。
     */
    public static ChatColor getClosestColor(Color color) {
        return COLORS.get(getNearestColor(color));
    }

    /**
     * 获取最接近的传统颜色对应的 RGB。
     */
    public static Color getNearestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Integer.MAX_VALUE;

        for (Color constantColor : COLORS.keySet()) {
            double distance = Math.pow(color.getRed() - constantColor.getRed(), 2)
                    + Math.pow(color.getGreen() - constantColor.getGreen(), 2)
                    + Math.pow(color.getBlue() - constantColor.getBlue(), 2);
            if (nearestDistance > distance) {
                nearestColor = constantColor;
                nearestDistance = distance;
            }
        }
        return nearestColor;
    }

    private static Color[] createRainbow(int step, float saturation) {
        Color[] colors = new Color[step];
        double colorStep = (1.00 / step);
        for (int i = 0; i < step; i++) {
            colors[i] = Color.getHSBColor((float) (colorStep * i), saturation, saturation);
        }
        return colors;
    }

    private static Color[] createGradient(Color start, Color end, int step) {
        // 步长 <= 1 时返回白色，避免除零
        if (step <= 1) {
            return new Color[]{Color.WHITE, Color.WHITE, Color.WHITE};
        }

        Color[] colors = new Color[step];
        int stepR = Math.abs(start.getRed() - end.getRed()) / (step - 1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step - 1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step - 1);
        int[] direction = new int[]{
                start.getRed() < end.getRed() ? +1 : -1,
                start.getGreen() < end.getGreen() ? +1 : -1,
                start.getBlue() < end.getBlue() ? +1 : -1
        };

        for (int i = 0; i < step; i++) {
            colors[i] = new Color(
                    start.getRed() + ((stepR * i) * direction[0]),
                    start.getGreen() + ((stepG * i) * direction[1]),
                    start.getBlue() + ((stepB * i) * direction[2]));
        }
        return colors;
    }

    private static String processGradients(String string) {
        if (string.indexOf('#') == -1) {
            return string;
        }
        Matcher matcher = GRADIENT_PATTERN.matcher(string);
        while (matcher.find()) {
            String start = matcher.group(1);
            String content = matcher.group(2);
            String end = matcher.group(4);
            string = string.replace(matcher.group(),
                    color(content, new Color(Integer.parseInt(start, 16)), new Color(Integer.parseInt(end, 16))));
        }
        return string;
    }

    private static String processSolid(String string) {
        if (string.indexOf('#') == -1) {
            return string;
        }
        Matcher matcher = SOLID_PATTERN.matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            if (color == null) {
                color = matcher.group(2);
            }
            string = string.replace(matcher.group(), "§#" + color);
        }
        return string;
    }

    private static String processRainbow(String string) {
        if (string.indexOf('<') == -1) {
            return string;
        }
        Matcher matcher = RAINBOW_PATTERN.matcher(string);
        while (matcher.find()) {
            String saturation = matcher.group(1);
            String content = matcher.group(2);
            string = string.replace(matcher.group(), rainbow(content, Float.parseFloat(saturation)));
        }
        return string;
    }
}
