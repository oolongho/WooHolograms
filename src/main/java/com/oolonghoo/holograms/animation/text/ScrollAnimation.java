package com.oolonghoo.holograms.animation.text;

import com.oolonghoo.holograms.animation.TextAnimation;
import com.oolonghoo.holograms.util.TextUtil;

/**
 * 滚动动画
 * 参考 DecentHolograms 的 ScrollAnimation 实现
 * 创建文本滚动效果
 * 
 * @author oolongho
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

        // 移除颜色代码获取纯文本
        String stripped = stripSpecialColors(string);
        int length = stripped.length();

        if (length == 0) {
            return string;
        }

        // 获取显示宽度参数
        int width = args != null && args.length > 0 ? parseWidth(args[0]) : DEFAULT_WIDTH;

        // 如果文本比宽度短，直接返回
        if (length <= width) {
            return string;
        }

        // 计算滚动位置
        int currentStep = getCurrentStep(step, length);

        // 计算显示窗口
        int endIndex = currentStep + width;

        if (endIndex <= length) {
            // 正常滚动
            return stripped.substring(currentStep, endIndex);
        } else {
            // 循环滚动：末尾 + 开头
            int overflow = endIndex - length;
            return stripped.substring(currentStep) + " " + stripped.substring(0, overflow);
        }
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
