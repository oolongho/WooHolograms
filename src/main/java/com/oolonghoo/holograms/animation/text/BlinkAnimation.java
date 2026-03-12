package com.oolonghoo.holograms.animation.text;

import com.oolonghoo.holograms.animation.TextAnimation;

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
        super("blink", 10, 10);
    }

    @Override
    public String animate(String string, long step, String... args) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        // 获取闪烁速度参数
        int blinkSpeed = args != null && args.length > 0 ? parseSpeed(args[0]) : 10;

        // 计算当前是否显示
        int actualStep = (int) (step / blinkSpeed);
        boolean visible = actualStep % 2 == 0;

        if (visible) {
            return string;
        } else {
            // 返回空字符串或透明文本
            return "";
        }
    }

    /**
     * 解析速度参数
     * 
     * @param arg 参数字符串
     * @return 速度值
     */
    private int parseSpeed(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}
