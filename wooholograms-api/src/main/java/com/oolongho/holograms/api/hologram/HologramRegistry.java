package com.oolongho.holograms.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 全息图注册表
 *
 * <p>全息图的查询、创建、克隆与删除入口。通过
 * {@code WooHologramsApiProvider.get().holograms()} 获取实例。</p>
 *
 * <p><b>线程约定：</b>所有方法必须在服务器主线程（Folia 下为全息图所在区域的线程）调用。</p>
 */
public interface HologramRegistry {

    /*
     * 查询
     */

    /**
     * 按名称获取全息图
     *
     * @param name 名称
     * @return 全息图；不存在返回 null
     */
    @Nullable
    Hologram getHologram(String name);

    /**
     * 获取全部全息图
     *
     * @return 只读集合
     */
    Collection<? extends Hologram> getAllHolograms();

    /**
     * 获取全部全息图名称
     *
     * @return 只读名称集合
     */
    Set<String> getHologramNames();

    /**
     * 获取指定世界中的全部全息图
     *
     * @param worldName 世界名称
     * @return 只读列表
     */
    List<? extends Hologram> getHologramsInWorld(String worldName);

    /**
     * 全息图是否存在
     *
     * @param name 名称
     * @return 是否存在
     */
    boolean exists(String name);

    /**
     * 全息图是否存在（等价于 {@link #exists(String)}）
     *
     * @param name 名称
     * @return 是否存在
     */
    boolean containsHologram(String name);

    /**
     * 获取全息图总数
     *
     * @return 数量
     */
    int getHologramCount();

    /**
     * 获取玩家当前可见的全息图
     *
     * @param player 玩家
     * @return 只读列表
     */
    List<? extends Hologram> getVisibleHolograms(Player player);

    /**
     * 获取玩家当前可见的全息图数量
     *
     * @param player 玩家
     * @return 数量
     */
    int getVisibleHologramCount(Player player);

    /**
     * 校验全息图名称是否合法（长度 ≤ 50，仅字母/数字/下划线/连字符）
     *
     * @param name 待校验名称
     * @return 是否合法
     */
    boolean isValidName(String name);

    /**
     * 创建流式构建器（链式设置内容与属性后调用 create() 完成创建）
     *
     * @param name     全息图名称
     * @param location 初始位置
     * @return 构建器
     */
    HologramBuilder builder(String name, Location location);

    /*
     * 创建 / 克隆 / 删除
     */

    /**
     * 创建全息图（保存到文件）
     *
     * @param name     名称（需合法且唯一）
     * @param location 初始位置
     * @return 新全息图；名称非法/已存在或位置无效返回 null
     */
    @Nullable
    Hologram createHologram(String name, Location location);

    /**
     * 创建全息图
     *
     * @param name       名称（需合法且唯一）
     * @param location   初始位置
     * @param saveToFile false 时为临时全息图（不写入存储文件）
     * @return 新全息图；失败返回 null
     */
    @Nullable
    Hologram createHologram(String name, Location location, boolean saveToFile);

    /**
     * 克隆全息图
     *
     * @param sourceName 源全息图名称
     * @param targetName 新全息图名称
     * @param location   新位置（null 表示沿用源位置）
     * @param temp       true 时不保存到文件（临时全息图）
     * @return 克隆出的全息图；失败返回 null
     */
    @Nullable
    Hologram cloneHologram(String sourceName, String targetName, @Nullable Location location, boolean temp);

    /**
     * 删除全息图（销毁实体并删除存储文件）
     *
     * @param name 名称
     * @return 是否成功删除
     */
    boolean deleteHologram(String name);

    /**
     * 计划延迟删除全息图（临时全息图定时销毁；全息图被提前删除时任务自动跳过）
     *
     * @param hologram   全息图
     * @param delayTicks 延迟 tick 数
     */
    void scheduleDeletion(Hologram hologram, long delayTicks);
}
