package com.oolongho.holograms.animation.custom;

import com.oolongho.holograms.animation.TextAnimation;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义文本动画
 * 从配置文件加载的自定义动画
 *
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

        String[] precompiled = getPrecompiledFrames(string, args);
        if (precompiled == null || precompiled.length == 0) return string;
        int index = getCurrentStep(step, precompiled.length);
        return precompiled[index];
    }

    @Override
    protected String[] precompile(String text, String... args) {
        // 将 {text} 占位符替换后的结果缓存
        String[] result = new String[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            String frame = frames.get(i);
            if (frame.contains("{text}")) {
                result[i] = frame.replace("{text}", text);
            } else {
                result[i] = frame;
            }
        }
        return result;
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
        if (speed <= 0) speed = 1;
        int pause = config.getInt("pause", 0);
        List<String> frames = config.getStringList("frames");

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
        if (speed <= 0) speed = 1;
        int pause = config.getInt("pause", 0);
        List<String> frames = config.getStringList("frames");

        return new CustomTextAnimation(name, speed, pause, frames);
    }
}
