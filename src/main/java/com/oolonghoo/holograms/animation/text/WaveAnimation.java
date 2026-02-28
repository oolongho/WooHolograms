package com.oolonghoo.holograms.animation.text;

import com.oolonghoo.holograms.animation.TextAnimation;
import com.oolonghoo.holograms.util.TextUtil;

/**
 * 波浪动画
 * 参考 DecentHolograms 的 WaveAnimation 实现
 * 创建波浪效果的文本动画
 * 
 * @author oolongho
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

        // 移除颜色代码获取纯文本
        String stripped = stripSpecialColors(string);
        int length = stripped.length();

        if (length == 0) {
            return string;
        }

        // 波浪大小为文本长度的 1/4
        int size = Math.max(length / 4, 1);
        int currentStep = getCurrentStep(step, length + size);

        // 计算波浪位置
        int index1 = currentStep > size ? currentStep - size : 0;
        int index2 = currentStep < size ? size - (size - currentStep) : currentStep;

        // 获取颜色参数
        String colPrimary = args != null && args.length > 0 ? args[0] : "&e";
        String colSecondary = args != null && args.length > 1 ? args[1] : "&f";

        // 构建波浪效果
        String start = index1 > 0 ? stripped.substring(0, index1) : "";
        String mid = length > index2 ? stripped.substring(index1, index2) : stripped.substring(index1);
        String end = length > index2 ? stripped.substring(index2) : "";

        return colPrimary + start + colSecondary + mid + colPrimary + end;
    }
}
