package com.oolongho.holograms.util;

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
     * 生成性能报告
     *
     * @return 格式化的报告文本
     */
    public String getReport() {
        if (totalTime.isEmpty()) {
            return "§e========== 性能分析 ==========\n§7暂无数据";
        }

        // 按总耗时降序排列
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(totalTime.entrySet());
        sorted.sort(Map.Entry.<String, Long>comparingByValue().reversed());

        StringBuilder sb = new StringBuilder();
        sb.append("§e========== 性能分析 ==========\n");

        for (Map.Entry<String, Long> entry : sorted) {
            String section = entry.getKey();
            long total = entry.getValue();
            long cnt = count.getOrDefault(section, 1L);
            double avgMs = (total / (double) cnt) / 1_000_000.0;

            sb.append("§7").append(section).append(": ")
              .append("§f").append(String.format("%.2f", avgMs)).append("ms")
              .append(" §8(平均, ").append(cnt).append("次)")
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
