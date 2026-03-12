package com.oolonghoo.holograms.animation.text;

import com.oolonghoo.holograms.animation.TextAnimation;

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

        // 移除颜色代码获取纯文本
        String stripped = stripSpecialColors(string);
        int length = stripped.length();

        if (length == 0) {
            return string;
        }

        // 获取当前步骤
        int currentStep = getCurrentStep(step, length);

        // 截取文本到当前步骤
        char[] chars = stripped.toCharArray();
        String result = new String(Arrays.copyOfRange(chars, 0, Math.min(currentStep, length)));

        // 添加光标效果（可选）
        if (currentStep < length) {
            result += "|";
        }

        return result;
    }
}
