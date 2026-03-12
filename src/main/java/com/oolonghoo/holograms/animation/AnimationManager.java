package com.oolonghoo.holograms.animation;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.animation.custom.CustomTextAnimation;
import com.oolonghoo.holograms.animation.text.BlinkAnimation;
import com.oolonghoo.holograms.animation.text.GradientAnimation;
import com.oolonghoo.holograms.animation.text.ScrollAnimation;
import com.oolonghoo.holograms.animation.text.TypewriterAnimation;
import com.oolonghoo.holograms.animation.text.WaveAnimation;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动画管理器
 * 参考 DecentHolograms 的 AnimationManager 实现
 * 负责动画的注册、管理和文本解析
 * 
 */
public class AnimationManager {

    /**
     * 动画匹配模式
     * 格式: {#ANIM:name:args}text{/#ANIM} 或 <#ANIM:name:args>text</#ANIM>
     */
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("[<{]#?ANIM:(\\w+)(:\\S+)?[}>](.*?)[<{]/#?ANIM[}>]");

    /**
     * 插件实例
     */
    private final WooHolograms plugin;

    /**
     * 动画映射表
     */
    private final Map<String, TextAnimation> animationMap;

    /**
     * 当前步骤计数器
     */
    private final AtomicLong step;

    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     */
    public AnimationManager(WooHolograms plugin) {
        this.plugin = plugin;
        this.animationMap = new HashMap<>();
        this.step = new AtomicLong(0);
        this.reload();
    }

    /**
     * 重载动画管理器
     */
    public synchronized void reload() {
        this.animationMap.clear();

        // 注册内置动画
        registerAnimation(new TypewriterAnimation());
        registerAnimation(new WaveAnimation());
        registerAnimation(new BlinkAnimation());
        registerAnimation(new ScrollAnimation());
        registerAnimation(new GradientAnimation());

        this.step.set(0);

        // 异步加载自定义动画
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::loadCustomAnimations);
    }
    
    /**
     * 加载动画
     */
    public void loadAnimations() {
        reload();
    }
    
    /**
     * 清理动画管理器
     */
    public synchronized void clear() {
        this.animationMap.clear();
    }

    /**
     * 销毁动画管理器
     */
    public synchronized void destroy() {
        this.animationMap.clear();
    }

    /**
     * 执行一次 tick
     */
    public void tick() {
        step.incrementAndGet();
    }

    /**
     * 获取当前步骤
     * 
     * @return 当前步骤
     */
    public long getStep() {
        return step.get();
    }

    /**
     * 解析文本中的动画
     * 
     * @param string 原始文本
     * @return 解析后的文本
     */
    public String parseTextAnimations(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        Matcher matcher = ANIMATION_PATTERN.matcher(string);
        while (matcher.find()) {
            String animationName = matcher.group(1);
            String args = matcher.group(2);
            String text = matcher.group(3);

            TextAnimation animation = getAnimation(animationName);
            if (animation != null) {
                String[] argsArray = args == null ? new String[0] : args.substring(1).split(",");
                string = string.replace(matcher.group(), animation.animate(text, getStep(), argsArray));
            }
        }

        return string;
    }

    /**
     * 检查文本是否包含动画
     * 
     * @param string 文本
     * @return 是否包含动画
     */
    public boolean containsAnimations(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        Matcher matcher = ANIMATION_PATTERN.matcher(string);
        return matcher.find();
    }

    /**
     * 注册动画
     * 
     * @param name 动画名称
     * @param animation 动画实例
     * @return 被替换的动画（如果存在）
     */
    public TextAnimation registerAnimation(String name, TextAnimation animation) {
        return animationMap.put(name.toLowerCase(), animation);
    }

    /**
     * 注册动画
     * 
     * @param animation 动画实例
     * @return 被替换的动画（如果存在）
     */
    public TextAnimation registerAnimation(TextAnimation animation) {
        return animationMap.put(animation.getName().toLowerCase(), animation);
    }

    /**
     * 注销动画
     * 
     * @param name 动画名称
     * @return 被移除的动画
     */
    public TextAnimation unregisterAnimation(String name) {
        return animationMap.remove(name.toLowerCase());
    }

    /**
     * 获取动画
     * 
     * @param name 动画名称
     * @return 动画实例
     */
    public TextAnimation getAnimation(String name) {
        if (name == null) {
            return null;
        }
        return animationMap.get(name.toLowerCase());
    }

    /**
     * 检查动画是否存在
     * 
     * @param name 动画名称
     * @return 是否存在
     */
    public boolean hasAnimation(String name) {
        return animationMap.containsKey(name.toLowerCase());
    }

    /**
     * 获取所有动画
     * 
     * @return 动画集合
     */
    public Collection<TextAnimation> getAnimations() {
        return animationMap.values();
    }

    /**
     * 获取动画数量
     * 
     * @return 动画数量
     */
    public int getAnimationCount() {
        return animationMap.size();
    }

    /**
     * 加载自定义动画
     */
    private void loadCustomAnimations() {
        File folder = new File(plugin.getDataFolder(), "animations");
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        int counter = 0;
        plugin.getLogger().info("正在加载自定义动画...");

        for (File file : files) {
            String fileName = file.getName();
            try {
                TextAnimation animation = CustomTextAnimation.fromFile(file);
                if (animation != null) {
                    registerAnimation(animation);
                    counter++;
                    plugin.getLogger().info("已加载动画: " + animation.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("无法从文件加载动画 '" + fileName + "': " + e.getMessage());
            }
        }

        if (counter > 0) {
            plugin.getLogger().info("已加载 " + counter + " 个自定义动画");
        }
    }
}
