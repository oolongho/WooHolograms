package com.oolongho.holograms.animation.text;

import com.oolongho.holograms.animation.TextAnimation;

import java.util.Arrays;

/**
 * 打字机动画
 * 参考 DecentHolograms 的 TypewriterAnimation 实现
 * 创建逐字显示的打字机效果
 *
 */
public class TypewriterAnimation extends TextAnimation {

    /**
     * 构造函数
     */
    public TypewriterAnimation() {
        super("typewriter", 3, 20);
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

        // 帧0到帧length-2：逐字显示 + 光标，帧length-1：完整文本无光标
        String[] frames = new String[length];
        char[] chars = stripped.toCharArray();

        for (int i = 0; i < length; i++) {
            String result = formatting + new String(Arrays.copyOfRange(chars, 0, i + 1));
            if (i < length - 1) {
                result += "|";
            }
            frames[i] = result;
        }

        return frames;
    }
}
