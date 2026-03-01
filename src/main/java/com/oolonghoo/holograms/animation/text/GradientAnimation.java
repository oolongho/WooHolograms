package com.oolonghoo.holograms.animation.text;

import com.oolonghoo.holograms.animation.TextAnimation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradientAnimation extends TextAnimation {

    private static final Map<String, String> COLOR_NAMES = new HashMap<>();

    static {
        COLOR_NAMES.put("red", "#FF0000");
        COLOR_NAMES.put("green", "#00FF00");
        COLOR_NAMES.put("blue", "#0000FF");
        COLOR_NAMES.put("yellow", "#FFFF00");
        COLOR_NAMES.put("cyan", "#00FFFF");
        COLOR_NAMES.put("magenta", "#FF00FF");
        COLOR_NAMES.put("white", "#FFFFFF");
        COLOR_NAMES.put("black", "#000000");
        COLOR_NAMES.put("orange", "#FFA500");
        COLOR_NAMES.put("purple", "#800080");
        COLOR_NAMES.put("pink", "#FFC0CB");
        COLOR_NAMES.put("gold", "#FFD700");
        COLOR_NAMES.put("gray", "#808080");
        COLOR_NAMES.put("grey", "#808080");
        COLOR_NAMES.put("lightblue", "#ADD8E6");
        COLOR_NAMES.put("lightgreen", "#90EE90");
        COLOR_NAMES.put("darkred", "#8B0000");
        COLOR_NAMES.put("darkgreen", "#006400");
        COLOR_NAMES.put("darkblue", "#00008B");
        COLOR_NAMES.put("aqua", "#00FFFF");
        COLOR_NAMES.put("brown", "#A52A2A");
        COLOR_NAMES.put("lime", "#00FF00");
    }

    public GradientAnimation() {
        super("gradient", 10, 0, "grad");
    }

    @Override
    public String animate(String string, long step, String... args) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        if (args == null || args.length < 2) {
            return string;
        }

        List<Color> colors = parseColors(args);
        if (colors.size() < 2) {
            return string;
        }

        String stripped = stripSpecialColors(string);
        int length = stripped.length();

        if (length == 0) {
            return string;
        }

        int totalFrames = length * 2;
        int currentFrame = getCurrentStep(step, totalFrames);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            double position = (double) (i + currentFrame % length) / length;
            Color color = getGradientColor(colors, position);
            result.append(colorToHex(color)).append(stripped.charAt(i));
        }

        return result.toString();
    }

    private List<Color> parseColors(String... args) {
        List<Color> colors = new ArrayList<>();

        for (String arg : args) {
            String colorStr = arg.trim();
            if (colorStr.isEmpty()) {
                continue;
            }

            if (colorStr.startsWith("#")) {
                try {
                    Color color = Color.decode(colorStr);
                    colors.add(color);
                } catch (NumberFormatException ignored) {
                }
            } else if (COLOR_NAMES.containsKey(colorStr.toLowerCase())) {
                try {
                    Color color = Color.decode(COLOR_NAMES.get(colorStr.toLowerCase()));
                    colors.add(color);
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    Color color = Color.decode("#" + colorStr);
                    colors.add(color);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return colors;
    }

    private Color getGradientColor(List<Color> colors, double position) {
        if (colors.size() == 1) {
            return colors.get(0);
        }

        position = position % 1.0;
        if (position < 0) {
            position += 1.0;
        }

        double segmentCount = colors.size() - 1;
        double segmentPosition = position * segmentCount;
        int segmentIndex = (int) Math.floor(segmentPosition);
        double segmentFraction = segmentPosition - segmentIndex;

        if (segmentIndex >= colors.size() - 1) {
            return colors.get(colors.size() - 1);
        }

        Color startColor = colors.get(segmentIndex);
        Color endColor = colors.get(segmentIndex + 1);

        return interpolateColor(startColor, endColor, segmentFraction);
    }

    private Color interpolateColor(Color start, Color end, double fraction) {
        int red = (int) Math.round(start.getRed() + (end.getRed() - start.getRed()) * fraction);
        int green = (int) Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * fraction);
        int blue = (int) Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * fraction);

        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new Color(red, green, blue);
    }

    private String colorToHex(Color color) {
        return String.format("&#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
