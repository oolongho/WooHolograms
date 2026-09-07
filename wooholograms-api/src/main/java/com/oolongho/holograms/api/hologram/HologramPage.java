package com.oolongho.holograms.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 全息图页面 —— API 视图
 *
 * <p>一个全息图可包含多个页面，每个页面包含多行内容。
 * 玩家同一时刻只查看其中一个页面，可通过点击动作或 API 翻页。</p>
 *
 * <p><b>线程约定：</b>所有方法必须在服务器主线程（Folia 下为全息图所在区域的线程）调用。</p>
 */
public interface HologramPage {

    /**
     * 获取所属全息图
     *
     * @return 父全息图
     */
    Hologram getParent();

    /**
     * 获取页索引（从 0 开始）
     *
     * @return 页索引
     */
    int getIndex();

    /*
     * 行查询
     */

    /**
     * 获取全部行
     *
     * @return 只读行列表
     */
    List<? extends HologramLine> getLines();

    /**
     * 获取指定行
     *
     * @param index 行索引
     * @return 行；越界返回 null
     */
    @Nullable
    HologramLine getLine(int index);

    /**
     * 获取行数
     *
     * @return 行数
     */
    int size();

    /**
     * 页面是否为空（无任何行）
     *
     * @return 是否为空
     */
    boolean isEmpty();

    /*
     * 行编辑
     */

    /**
     * 在指定索引插入一行。插入后自动重建文本分组并对齐，观看者无需重新进入范围即可看到
     *
     * @param index   插入位置（0 = 最上方）
     * @param content 行内容
     * @return 是否成功（索引越界返回 false）
     */
    boolean insertLine(int index, String content);

    /**
     * 在末尾追加一行
     *
     * @param content 行内容
     * @return 新添加的行；失败返回 null
     */
    @Nullable
    HologramLine addLine(String content);

    /**
     * 设置指定行的内容
     *
     * @param index   行索引
     * @param content 新内容
     * @return 是否成功
     */
    boolean setLine(int index, String content);

    /**
     * 移除指定行
     *
     * @param index 行索引
     * @return 被移除的行；越界返回 null
     */
    @Nullable
    HologramLine removeLine(int index);

    /** 清空全部行 */
    void clearLines();

    /*
     * 几何
     */

    /**
     * 获取页面总高度（全部行高度之和）
     *
     * @return 高度
     */
    double getHeight();

    /**
     * 获取页面中心位置
     *
     * @return 中心位置副本
     */
    @Nullable
    Location getCenter();

    /*
     * 动作查询
     */

    /**
     * 页面是否可点击（自身或任意行配置了动作，且未被 DISABLE_ACTIONS 标志禁用）
     *
     * @return 是否可点击
     */
    boolean isClickable();

    /**
     * 页面自身是否配置了任意点击动作（不含行级）
     *
     * @return 是否有动作
     */
    boolean hasActions();

    /*
     * 标志
     */

    /**
     * 是否设置了指定标志
     *
     * @param flag 标志
     * @return 是否设置
     */
    boolean hasFlag(EnumFlag flag);

    /** 添加标志 */
    void addFlags(EnumFlag... flags);

    /** 移除标志 */
    void removeFlag(EnumFlag flag);

    /** 清除本页全部标志 */
    void clearFlags();

    /**
     * 获取本页设置的标志
     *
     * @return 只读标志集合
     */
    java.util.Set<EnumFlag> getFlags();
}
