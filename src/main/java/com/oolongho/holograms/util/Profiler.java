package com.oolongho.holograms.util;

import com.oolongho.holograms.config.Messages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量级性能分析器
 * 使用 System.nanoTime() 计时，线程安全，默认禁用
 *
 */
public class Profiler {

    private static final Profiler INSTANCE = new Profiler();

    /** 是否启用（volatile 保证可见性） */
    private volatile boolean enabled = false;

    /** 每个模块累计耗时（纳秒） */
    private final Map<String, Long> totalTime = new ConcurrentHashMap<>();

    /** 每个模块调用次数 */
    private final Map<String, Long> count = new ConcurrentHashMap<>();

    /** 每个线程当前计时起点 */
    private final ThreadLocal<Map<String, Long>> startTimes = ThreadLocal.withInitial(ConcurrentHashMap::new);

    private Profiler() {}

    public static Profiler getInstance() {
        return INSTANCE;
    }

    /**
     * 开始计时
     *
     * @param section 模块名称
     */
    public void start(String section) {
        if (!enabled) return;
        startTimes.get().put(section, System.nanoTime());
    }

    /**
     * 结束计时并累计
     *
     * @param section 模块名称
     */
    public void stop(String section) {
        if (!enabled) return;
        Long startTime = startTimes.get().remove(section);
        if (startTime == null) return;

        long elapsed = System.nanoTime() - startTime;
        totalTime.merge(section, elapsed, Long::sum);
        count.merge(section, 1L, Long::sum);
    }

    /**
     * 生成性能报告（使用 MiniMessage 标签，由调用方通过 Messages.parse 解析为 Component）
     *
     * @param messages Messages 实例，用于解析语言键
     * @return 格式化的报告文本（MiniMessage 格式）
     */
    public String getReport(Messages messages) {
        if (totalTime.isEmpty()) {
            return messages.getRaw("gui.profiler.report-empty");
        }

        // 按总耗时降序排列
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(totalTime.entrySet());
        sorted.sort(Map.Entry.<String, Long>comparingByValue().reversed());

        StringBuilder sb = new StringBuilder();
        sb.append(messages.getRaw("gui.profiler.report-header")).append("\n");

        for (Map.Entry<String, Long> entry : sorted) {
            String section = entry.getKey();
            long total = entry.getValue();
            long cnt = count.getOrDefault(section, 1L);
            double avgMs = (total / (double) cnt) / 1_000_000.0;

            sb.append("<color:#9CA3AF>").append(section).append(": ")
              .append("<white>").append(String.format("%.2f", avgMs)).append("ms")
              .append(messages.getString("gui.profiler.report-avg", "count", String.valueOf(cnt)))
              .append("\n");
        }

        // 移除末尾换行
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * 重置所有统计数据
     */
    public void reset() {
        totalTime.clear();
        count.clear();
        startTimes.remove();
    }

    /**
     * 设置启用状态
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            reset();
        }
    }

    /**
     * 获取启用状态
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
}
