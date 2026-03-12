package com.oolonghoo.holograms.animation;

import java.util.Arrays;
import java.util.List;

/**
 * 动画基类
 * 参考 DecentHolograms 的 Animation 实现
 * 定义全息图动画的基本属性和行为
 * 
 */
public abstract class Animation {

    /**
     * 动画名称
     */
    private final String name;

    /**
     * 动画别名列表
     */
    private final List<String> aliases;

    /**
     * 动画速度（tick）
     */
    private final int speed;

    /**
     * 动画暂停时间（tick）
     */
    private final int pause;

    /**
     * 构造函数
     * 
     * @param name 动画名称
     * @param speed 动画速度
     * @param pause 暂停时间
     */
    protected Animation(String name, int speed, int pause) {
        this(name, speed, pause, new String[0]);
    }

    /**
     * 构造函数（带别名）
     * 
     * @param name 动画名称
     * @param speed 动画速度
     * @param pause 暂停时间
     * @param aliases 别名数组
     */
    protected Animation(String name, int speed, int pause, String... aliases) {
        this.name = name;
        this.speed = speed;
        this.pause = pause;
        this.aliases = Arrays.asList(aliases == null ? new String[0] : aliases);
    }

    /**
     * 获取当前动画步骤
     * 
     * @param step 当前步骤
     * @param maxSteps 最大步骤数
     * @return 当前步骤索引
     */
    protected int getCurrentStep(long step, int maxSteps) {
        if (maxSteps <= 0) {
            return 0;
        }
        long actualStep = step / speed;
        // 适配暂停时间到速度
        int actualPause = pause <= 0 ? 0 : pause / speed;
        int currentStep = (int) (actualStep % (maxSteps + actualPause));
        return Math.min(currentStep, maxSteps);
    }

    /**
     * 检查字符串是否匹配此动画的标识符
     * 
     * @param string 要检查的字符串
     * @return 是否匹配
     */
    public boolean isIdentifier(String string) {
        if (string == null) {
            return false;
        }
        return name.equalsIgnoreCase(string) || aliases.contains(string.toLowerCase());
    }

    /**
     * 获取动画名称
     * 
     * @return 动画名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取动画别名列表
     * 
     * @return 别名列表
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * 获取动画速度
     * 
     * @return 速度（tick）
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * 获取暂停时间
     * 
     * @return 暂停时间（tick）
     */
    public int getPause() {
        return pause;
    }

    @Override
    public String toString() {
        return "Animation{" +
                "name='" + name + '\'' +
                ", speed=" + speed +
                ", pause=" + pause +
                '}';
    }
}
