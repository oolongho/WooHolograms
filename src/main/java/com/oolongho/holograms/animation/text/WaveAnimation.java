package com.oolongho.holograms.animation.text;

import com.oolongho.holograms.animation.TextAnimation;

/**
 * 波浪动画
 * 参考 DecentHolograms 的 WaveAnimation 实现
 * 创建波浪效果的文本动画
 *
 */
public class WaveAnimation extends TextAnimation {

    /**
     * 构造函数
     */
    public WaveAnimation() {
        super("wave", 2, 40);
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

        // 波浪大小为文本长度的 1/4
        int size = Math.max(length / 4, 1);
        int totalFrames = length + size;

        // 获取颜色参数
        String colPrimary = args != null && args.length > 0 ? args[0] : "&e";
        String colSecondary = args != null && args.length > 1 ? args[1] : "&f";

        String[] frames = new String[totalFrames];
        for (int currentStep = 0; currentStep < totalFrames; currentStep++) {
            int index1 = currentStep > size ? currentStep - size : 0;
            int index2 = currentStep < size ? size - (size - currentStep) : currentStep;

            String start = index1 > 0 ? stripped.substring(0, index1) : "";
            String mid = length > index2 ? stripped.substring(index1, index2) : stripped.substring(index1);
            String end = length > index2 ? stripped.substring(index2) : "";

            frames[currentStep] = colPrimary + formatting + start + colSecondary + formatting + mid + colPrimary + formatting + end;
        }

        return frames;
    }
}
