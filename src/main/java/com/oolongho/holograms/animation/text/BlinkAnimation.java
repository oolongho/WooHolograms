package com.oolongho.holograms.animation.text;

import com.oolongho.holograms.animation.TextAnimation;

/**
 * 闪烁动画
 * 创建闪烁效果的文本动画
 *
 */
public class BlinkAnimation extends TextAnimation {

    /**
     * 构造函数
     */
    public BlinkAnimation() {
        super("blink", 10, 0);
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
        // 速度参数控制闪烁周期：数值越大，闪烁越慢（显示/隐藏各持续 speed 帧）
        int speed = 10;
        if (args != null && args.length > 0) {
            try {
                speed = Math.max(Integer.parseInt(args[0]), 1);
            } catch (NumberFormatException ignored) {
            }
        }
        // 一个完整周期：speed 帧显示 + speed 帧隐藏
        String[] frames = new String[speed * 2];
        for (int i = 0; i < speed; i++) {
            frames[i] = text;
        }
        for (int i = speed; i < speed * 2; i++) {
            frames[i] = "";
        }
        return frames;
    }
}
