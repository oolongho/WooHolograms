package com.oolongho.holograms.animation.text;

import com.oolongho.holograms.animation.TextAnimation;

/**
 * 滚动动画
 * 参考 DecentHolograms 的 ScrollAnimation 实现
 * 创建文本滚动效果
 *
 */
public class ScrollAnimation extends TextAnimation {

    /**
     * 默认显示宽度
     */
    private static final int DEFAULT_WIDTH = 20;

    /**
     * 构造函数
     */
    public ScrollAnimation() {
        super("scroll", 3, 0);
    }

    @Override
    public String animate(String string, long step, String... args) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        String[] frames = getPrecompiledFrames(string, args);
        if (frames == null || frames.length == 0) return string;
        int index = getCurrentStep(step, frames.length);
        return frames[index];
    }

    @Override
    protected String[] precompile(String text, String... args) {
        String formatting = extractFormattingCodes(text);
        String stripped = stripSpecialColors(text);
        int length = stripped.length();

        if (length == 0) {
            return new String[]{text};
        }

        int width = args != null && args.length > 0 ? parseWidth(args[0]) : DEFAULT_WIDTH;

        // 文本比宽度短，无需滚动，保留原始格式
        if (length <= width) {
            return new String[]{text};
        }

        // 预计算每一帧的滚动偏移
        String[] frames = new String[length];
        for (int offset = 0; offset < length; offset++) {
            int endIndex = offset + width;
            if (endIndex <= length) {
                frames[offset] = formatting + stripped.substring(offset, endIndex);
            } else {
                // 循环滚动：末尾 + 开头
                int overflow = endIndex - length;
                frames[offset] = formatting + stripped.substring(offset) + " " + formatting + stripped.substring(0, overflow);
            }
        }

        return frames;
    }

    /**
     * 解析宽度参数
     *
     * @param arg 参数字符串
     * @return 宽度值
     */
    private int parseWidth(String arg) {
        try {
            int width = Integer.parseInt(arg);
            return Math.max(width, 1);
        } catch (NumberFormatException e) {
            return DEFAULT_WIDTH;
        }
    }
}
