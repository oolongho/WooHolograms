package com.oolongho.holograms.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 全息图 —— API 视图
 *
 * <p>表示一个完整的全息图对象：多页面、多行内容、显示属性与玩家会话（可见性/页码）。</p>
 *
 * <p><b>线程约定：</b>所有方法必须在服务器主线程（Folia 下为全息图所在区域的线程）调用。</p>
 *
 * <p><b>持久化约定：</b>内容与属性修改会自动标记保存，由插件在后台批量落盘；
 * 停服时同步保存，无需调用方手动持久化。</p>
 */
public interface Hologram {

    /*
     * 基本信息
     */

    /**
     * 获取全息图名称（即 ID，服务器内唯一）
     *
     * @return 名称
     */
    String getName();

    /**
     * 获取全息图 ID（与 {@link #getName()} 相同）
     *
     * @return ID
     */
    String getId();

    /**
     * 获取全息图位置
     *
     * @return 位置副本；未设置返回 null
     */
    @Nullable
    Location getLocation();

    /**
     * 设置全息图位置。跨世界移动会自动迁移世界缓存并对观看者隐藏后重新显示
     *
     * @param location 新位置
     */
    void setLocation(Location location);

    /**
     * 全息图是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 设置启用状态。禁用会从所有玩家处隐藏；启用会向范围内玩家重新显示
     *
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);

    /*
     * 权限
     */

    /**
     * 获取查看权限
     *
     * @return 权限节点；null 或空表示无限制
     */
    @Nullable
    String getPermission();

    /** 设置查看权限（null 或空表示无限制） */
    void setPermission(@Nullable String permission);

    /**
     * 玩家是否拥有查看权限
     *
     * @param player 玩家
     * @return 是否有权限
     */
    boolean hasPermission(Player player);

    /*
     * 范围与刷新
     */

    /** 获取显示范围（方块）。超出此范围的玩家看不到该全息图 */
    double getDisplayRange();

    /** 设置显示范围（方块） */
    void setDisplayRange(double displayRange);

    /** 获取更新范围（方块）。超出此范围的玩家不接收占位符/动画刷新 */
    double getUpdateRange();

    /** 设置更新范围（方块） */
    void setUpdateRange(double updateRange);

    /**
     * 获取自动刷新间隔（tick）。0 表示禁用自动刷新（静态全息图）
     *
     * @return 间隔 tick 数
     */
    int getUpdateInterval();

    /** 设置自动刷新间隔（tick，0 = 禁用）。影响占位符与动画的刷新周期 */
    void setUpdateInterval(int updateInterval);

    /** 获取行高 */
    double getLineHeight();

    /**
     * 设置行高。修改后全部行自动重新对齐
     *
     * @param lineHeight 行高
     */
    void setLineHeight(double lineHeight);

    /*
     * 显示属性
     */

    /** 获取整体朝向（yaw，度） */
    float getFacing();

    /** 设置整体朝向，并刷新所有观看者 */
    void setFacing(float facing);

    /**
     * 获取垂直倾斜角度覆盖（仅 FIXED_ANGLE billboard 下生效）
     *
     * @return 倾斜角度；null 表示使用默认值 0
     */
    @Nullable
    Float getPitch();

    /** 设置垂直倾斜角度（-90~90；null 表示清除覆盖） */
    void setPitch(@Nullable Float pitch);

    /** 获取 Billboard 模式 */
    Billboard getBillboard();

    /** 设置 Billboard 模式，并刷新所有观看者 */
    void setBillboard(Billboard billboard);

    /** 是否双面渲染 */
    boolean isDoubleSided();

    /** 设置双面渲染，并刷新所有观看者 */
    void setDoubleSided(boolean doubleSided);

    /** 获取文本对齐方式 */
    TextAlignment getAlignment();

    /** 设置文本对齐方式（无闪烁的 metadata 更新） */
    void setAlignment(TextAlignment alignment);

    /** 获取背景透明度（0~255） */
    int getBackgroundAlpha();

    /** 设置背景透明度（0=完全透明，255=不透明，自动截断到区间） */
    void setBackgroundAlpha(int backgroundAlpha);

    /** 获取背景颜色（RGB） */
    int getBackgroundColor();

    /** 设置背景颜色（只保留 RGB 分量） */
    void setBackgroundColor(int backgroundColor);

    /** 获取文本自动换行宽度（像素） */
    int getLineWidth();

    /** 设置文本自动换行宽度（像素，最小 1） */
    void setLineWidth(int lineWidth);

    /** 获取 X 轴缩放 */
    float getScaleX();

    /** 获取 Y 轴缩放 */
    float getScaleY();

    /** 获取 Z 轴缩放 */
    float getScaleZ();

    /**
     * 一次性设置三轴缩放（无闪烁的 metadata 更新）
     *
     * @param x X 轴缩放
     * @param y Y 轴缩放
     * @param z Z 轴缩放
     */
    void setScale(float x, float y, float z);

    /** 获取 X 轴平移 */
    double getTranslationX();

    /** 获取 Y 轴平移 */
    double getTranslationY();

    /** 获取 Z 轴平移 */
    double getTranslationZ();

    /** 一次性设置三轴平移 */
    void setTranslation(double x, double y, double z);

    /** 获取阴影半径 */
    float getShadowRadius();

    /** 设置阴影半径 */
    void setShadowRadius(float shadowRadius);

    /** 获取阴影强度 */
    float getShadowStrength();

    /** 设置阴影强度 */
    void setShadowStrength(float shadowStrength);

    /**
     * 获取发光颜色覆盖
     *
     * @return 颜色值；-1 表示不覆盖
     */
    int getGlowColor();

    /** 设置发光颜色覆盖（-1 表示不覆盖） */
    void setGlowColor(int glowColor);

    /**
     * 获取亮度覆盖
     *
     * @return 亮度；null 表示继承环境光照
     */
    @Nullable
    Brightness getBrightness();

    /** 设置亮度覆盖（null 表示继承环境光照） */
    void setBrightness(@Nullable Brightness brightness);

    /** 是否启用彩虹背景色 */
    boolean isChromaBackground();

    /** 设置彩虹背景色 */
    void setChromaBackground(boolean chromaBackground);

    /** 是否启用彩虹发光色 */
    boolean isChromaGlow();

    /** 设置彩虹发光色 */
    void setChromaGlow(boolean chromaGlow);

    /** 同时设置彩虹背景色与彩虹发光色 */
    void setChroma(boolean enabled);

    /*
     * 类型与可见性
     */

    /**
     * 获取全息图类型（预留字段，当前渲染按行类型决定）
     *
     * @return 类型
     */
    HologramType getType();

    /**
     * 设置全息图类型（类型变化触发完整重建）
     *
     * @param type 类型
     */
    void setType(HologramType type);

    /**
     * 全局可见开关（与玩家会话无关；false 时对所有玩家隐藏）
     *
     * @return 是否可见
     */
    boolean isVisible();

    /** 设置全局可见开关（false 隐藏所有玩家，true 恢复显示） */
    void setVisible(boolean visible);

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

    /** 清除全部标志 */
    void clearFlags();

    /**
     * 获取全部标志
     *
     * @return 只读标志集合
     */
    Set<EnumFlag> getFlags();

    /*
     * 页面管理
     */

    /**
     * 获取页数
     *
     * @return 页数
     */
    int size();

    /** 获取页数（等价于 {@link #size()}） */
    int getPageCount();

    /**
     * 获取全部页面
     *
     * @return 只读页面列表
     */
    List<? extends HologramPage> getPages();

    /**
     * 获取指定页
     *
     * @param index 页索引
     * @return 页面；越界返回 null
     */
    @Nullable
    HologramPage getPage(int index);

    /**
     * 获取玩家当前查看的页面
     *
     * @param player 玩家
     * @return 页面；玩家未观看时返回 null
     */
    @Nullable
    HologramPage getPage(Player player);

    /**
     * 获取第一页（等价于 {@code getPage(0)}）
     *
     * @return 第一页
     */
    @Nullable
    HologramPage getCurrentPage();

    /** 在末尾追加一个空页面 */
    HologramPage addPage();

    /**
     * 在末尾追加一个页面并写入内容
     *
     * @param lines 行内容列表
     * @return 新页面
     */
    HologramPage addPage(List<String> lines);

    /**
     * 在指定位置插入空页面（后续页面索引与玩家页码自动顺延）
     *
     * @param index 插入位置
     * @return 新页面；索引越界返回 null
     */
    @Nullable
    HologramPage insertPage(int index);

    /**
     * 在指定位置插入页面并写入内容
     *
     * @param index 插入位置
     * @param lines 行内容列表
     * @return 新页面；索引越界返回 null
     */
    @Nullable
    HologramPage insertPage(int index, List<String> lines);

    /**
     * 移除指定页（观看该页的玩家自动切回第一页）
     *
     * @param index 页索引
     * @return 被移除的页面；越界返回 null
     */
    @Nullable
    HologramPage removePage(int index);

    /**
     * 交换两个页面
     *
     * @param index1 页索引 1
     * @param index2 页索引 2
     * @return 是否成功
     */
    boolean swapPages(int index1, int index2);

    /** 清空并销毁全部页面（所有玩家停止观看） */
    void clearPages();

    /**
     * 获取总页数（等价于 {@link #size()}）
     *
     * @return 总页数
     */
    int getTotalPages();

    /*
     * 玩家页码切换
     */

    /**
     * 玩家切换到下一页（最后一页时循环回第一页）
     *
     * @param player 玩家
     * @return 是否成功
     */
    boolean nextPage(Player player);

    /**
     * 玩家切换到上一页（第一页时循环回最后一页）
     *
     * @param player 玩家
     * @return 是否成功
     */
    boolean previousPage(Player player);

    /**
     * 玩家切换到指定页
     *
     * @param player    玩家
     * @param pageIndex 页索引
     * @return 是否成功
     */
    boolean switchPage(Player player, int pageIndex);

    /** 玩家切换到第一页 */
    boolean firstPage(Player player);

    /** 玩家切换到最后一页 */
    boolean lastPage(Player player);

    /** 玩家是否还有下一页 */
    boolean hasNextPage(Player player);

    /** 玩家是否还有上一页 */
    boolean hasPreviousPage(Player player);

    /*
     * 显示 / 隐藏
     */

    /**
     * 显示给玩家（使用玩家记忆的页码）
     *
     * @param player 玩家
     * @return 是否成功（禁用/权限/距离/空页等导致失败返回 false）
     */
    boolean show(Player player);

    /**
     * 显示指定页给玩家
     *
     * @param player    玩家
     * @param pageIndex 页索引
     * @return 是否成功
     */
    boolean show(Player player, int pageIndex);

    /**
     * 从玩家处隐藏
     *
     * @param player 玩家
     */
    void hide(Player player);

    /**
     * 从玩家处隐藏
     *
     * @param player        玩家
     * @param keepPageState 是否保留玩家的页码记忆
     */
    void hide(Player player, boolean keepPageState);

    /** 显示给全部在线玩家（受权限/距离约束） */
    void showAll();

    /** 从所有观看者处隐藏 */
    void hideAll();

    /** 向显示范围内的全部在线玩家显示（通常无需手动调用，插件自动管理） */
    void showToNearby();

    /*
     * 玩家会话
     */

    /**
     * 玩家当前是否正在观看
     *
     * @param player 玩家
     * @return 是否观看
     */
    boolean isVisible(Player player);

    /**
     * 获取玩家记忆的页码（未观看时为 0）
     *
     * @param player 玩家
     * @return 页码
     */
    int getPlayerPage(Player player);

    /**
     * 设置玩家记忆的页码（下次 show 时生效）
     *
     * @param player    玩家
     * @param pageIndex 页码
     */
    void setPlayerPage(Player player, int pageIndex);

    /** 清除玩家的页码记忆（重置为第一页） */
    void clearPlayerPage(Player player);

    /**
     * 获取全部观看者的页码映射（只读）
     *
     * @return UUID -> 页码
     */
    Map<UUID, Integer> getViewerPages();

    /**
     * 获取全部观看者 UUID
     *
     * @return 只读 UUID 集合
     */
    Set<UUID> getViewers();

    /**
     * 获取全部观看者玩家（仅含在线者）
     *
     * @return 玩家列表
     */
    List<Player> getViewerPlayers();

    /**
     * 获取正在查看指定页的玩家
     *
     * @param pageIndex 页索引
     * @return 玩家集合
     */
    Set<Player> getViewerPlayers(int pageIndex);

    /**
     * 玩家是否处于"强制隐藏"状态（通过 {@link #setHidePlayer} 设置）
     *
     * @param player 玩家
     * @return 是否隐藏
     */
    boolean isHideState(Player player);

    /** 强制对玩家隐藏（无视距离自动显示，直到移除该状态或玩家退出） */
    void setHidePlayer(Player player);

    /** 移除玩家的强制隐藏状态 */
    void removeHidePlayer(Player player);

    /**
     * 玩家是否处于"强制显示"状态
     *
     * @param player 玩家
     * @return 是否显示
     */
    boolean isShowState(Player player);

    /** 强制对玩家显示（默认全局不可见时仍可见，直到移除该状态或玩家退出） */
    void setShowPlayer(Player player);

    /** 移除玩家的强制显示状态 */
    void removeShowPlayer(Player player);

    /*
     * 更新与刷新
     */

    /**
     * 立即更新玩家看到的内容（占位符/非文本行，受更新范围与 DISABLE_UPDATING 标志约束）
     *
     * @param player 玩家
     */
    void update(Player player);

    /** 更新全部观看者的内容 */
    void updateAll();

    /**
     * 更新全部观看者的内容
     *
     * @param force true 时跳过占位符变化检测，强制重发
     */
    void updateAll(boolean force);

    /** 强制重解析全部页面的文本内容（含动画与颜色） */
    void updateText();

    /** 手动推进一次全部观看者的动画帧 */
    void updateAnimationsAll();

    /** 完整重建并刷新所有观看者的显示（hide+show，有闪烁，仅在必要时使用） */
    void refreshAllViewers();

    /** 无闪烁刷新全部观看者的 Display 属性（billboard/背景色/缩放等） */
    void updateDisplayPropertiesAllViewers();

    /** 重新对齐全部页面的行位置 */
    void realignLines();

    /*
     * 移动
     */

    /**
     * 传送到新位置
     *
     * @param location 新位置
     */
    void teleport(Location location);

    /**
     * 传送到新位置
     *
     * @param location      新位置
     * @param updateViewers 是否同步更新观看者（false 仅修改数据，稍后手动刷新）
     */
    void teleport(Location location, boolean updateViewers);

    /**
     * 相对偏移移动
     *
     * @param dx X 轴偏移
     * @param dy Y 轴偏移
     * @param dz Z 轴偏移
     */
    void move(double dx, double dy, double dz);

    /**
     * 获取批量编辑器（多处属性修改一次性提交）
     *
     * @return 编辑器
     */
    HologramEditor edit();

    /*
     * 生命周期
     */

    /**
     * 删除全息图：销毁实体并从存储中删除文件。删除后不要再使用此对象
     */
    void delete();

    /**
     * 克隆为新的全息图
     *
     * @param name     新全息图名称
     * @param location 新位置
     * @param temp     true 时不保存到文件（临时全息图）
     * @return 克隆出的全息图
     */
    Hologram clone(String name, Location location, boolean temp);
}
