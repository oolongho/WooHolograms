package com.oolonghoo.holograms.animation;

import com.oolonghoo.holograms.util.ColorUtil;
import com.oolonghoo.holograms.util.TextUtil;

/**
 * 文本动画抽象类
 * 参考 DecentHolograms 的 TextAnimation 实现
 * 用于处理文本相关的动画效果
 * 
 */
public abstract class TextAnimation extends Animation {

    /**
     * 构造函数
     * 
     * @param name 动画名称
     * @param speed 动画速度
     * @param pause 暂停时间
     */
    protected TextAnimation(String name, int speed, int pause) {
        super(name, speed, pause);
    }

    /**
     * 构造函数（带别名）
     * 
     * @param name 动画名称
     * @param speed 动画速度
     * @param pause 暂停时间
     * @param aliases 别名数组
     */
    protected TextAnimation(String name, int speed, int pause, String... aliases) {
        super(name, speed, pause, aliases);
    }

    /**
     * 执行动画效果
     * 
     * @param string 原始文本
     * @param step 当前步骤
     * @param args 动画参数
     * @return 动画处理后的文本
     */
    public abstract String animate(String string, long step, String... args);

    /**
     * 移除文本中的特殊颜色代码
     * 
     * @param string 原始文本
     * @return 清理后的文本
     */
    protected String stripSpecialColors(String string) {
        if (string == null) {
            return "";
        }
        return TextUtil.stripColor(string);
    }

    /**
     * 获取文本的实际长度（不包含颜色代码）
     * 
     * @param string 文本
     * @return 实际长度
     */
    protected int getTextLength(String string) {
        return TextUtil.getLength(string);
    }

    /**
     * 处理颜色代码
     * 
     * @param string 文本
     * @return 处理后的文本
     */
    protected String colorize(String string) {
        return ColorUtil.colorize(string);
    }
}
