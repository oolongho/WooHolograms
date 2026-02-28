package com.oolonghoo.holograms.animation.custom;

import com.oolonghoo.holograms.animation.TextAnimation;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义文本动画
 * 从配置文件加载的自定义动画
 * 
 * @author oolongho
 */
public class CustomTextAnimation extends TextAnimation {

    /**
     * 动画帧列表
     */
    private final List<String> frames;

    /**
     * 构造函数
     * 
     * @param name 动画名称
     * @param speed 动画速度
     * @param pause 暂停时间
     * @param frames 帧列表
     */
    public CustomTextAnimation(String name, int speed, int pause, List<String> frames) {
        super(name, speed, pause);
        this.frames = frames != null ? frames : new ArrayList<>();
    }

    @Override
    public String animate(String string, long step, String... args) {
        if (frames.isEmpty()) {
            return string;
        }

        // 获取当前帧
        int currentStep = getCurrentStep(step, frames.size());
        String frame = frames.get(currentStep);

        // 如果帧包含 {text} 占位符，替换为原始文本
        if (frame.contains("{text}")) {
            return frame.replace("{text}", string);
        }

        // 否则返回帧内容
        return frame;
    }

    /**
     * 获取帧列表
     * 
     * @return 帧列表
     */
    public List<String> getFrames() {
        return frames;
    }

    /**
     * 从文件加载自定义动画
     * 
     * @param file 配置文件
     * @return 动画实例
     * @throws IllegalArgumentException 如果配置无效
     */
    public static CustomTextAnimation fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 获取动画名称（从文件名）
        String name = file.getName().replace(".yml", "");

        // 获取配置
        int speed = config.getInt("speed", 5);
        int pause = config.getInt("pause", 0);
        List<String> frames = config.getStringList("frames");

        // 验证配置
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("动画 '" + name + "' 没有定义帧");
        }

        return new CustomTextAnimation(name, speed, pause, frames);
    }

    /**
     * 从配置创建自定义动画
     * 
     * @param name 动画名称
     * @param config 配置
     * @return 动画实例
     */
    public static CustomTextAnimation fromConfig(String name, YamlConfiguration config) {
        int speed = config.getInt("speed", 5);
        int pause = config.getInt("pause", 0);
        List<String> frames = config.getStringList("frames");

        return new CustomTextAnimation(name, speed, pause, frames);
    }
}
