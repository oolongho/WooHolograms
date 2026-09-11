package com.oolongho.holograms.api.hologram;

import com.oolongho.holograms.api.action.ClickType;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * 全息图行 —— API 视图
 *
 * <p>表示全息图中的单行内容。行类型由内容字符串自动解析
 * （{@code #ICON:}、{@code #HEAD:}、{@code #SMALLHEAD:}、{@code #BLOCK:}、{@code #ENTITY:} 前缀，
 * 其余视为普通文本，支持颜色代码与占位符）。</p>
 *
 * <p><b>线程约定：</b>所有方法必须在服务器主线程（Folia 下为全息图所在区域的线程）调用。</p>
 *
 * <p><b>注意：</b>行的显示属性 setter 为"静默"修改（不触发客户端刷新），
 * 批量修改后请调用所属全息图的 {@code refreshAllViewers()}，或使用后续版本提供的批量编辑器。</p>
 */
public interface HologramLine {

    /**
     * 获取所属页面
     *
     * @return 父页面
     */
    HologramPage getParent();

    /**
     * 获取原始内容字符串（未解析占位符/动画）
     *
     * @return 内容
     */
    String getContent();

    /**
     * 设置内容。内容变化会自动推送到所有观看者；
     * 若内容前缀导致行类型变化，将触发该行的实体重建
     *
     * @param content 新内容
     */
    void setContent(String content);

    /**
     * 获取行类型
     *
     * @return 行类型
     */
    HologramType getType();

    /**
     * 获取行高。TEXT 行返回所属全息图的行高，其余类型返回各自的默认/自定义高度
     *
     * @return 行高
     */
    double getHeight();

    /*
     * 偏移
     */

    /**
     * 获取 X 轴偏移。非零 X/Z 偏移会使该行脱离主文本组独立渲染（不占据纵向空间）
     */
    double getOffsetX();

    /** 获取 Y 轴偏移 */
    double getOffsetY();

    /** 获取 Z 轴偏移 */
    double getOffsetZ();

    /**
     * 一次性设置 X/Y/Z 偏移。
     * X/Z 偏移变化会触发文本组重分裂与全部行位置重对齐（自带刷新，无需额外调用）
     *
     * @param x X 轴偏移
     * @param y Y 轴偏移
     * @param z Z 轴偏移
     */
    void setOffset(double x, double y, double z);

    /*
     * 朝向
     */

    /** 获取行朝向（yaw，度） */
    float getFacing();

    /** 设置行朝向（yaw，度） */
    void setFacing(float facing);

    /**
     * 获取自定义 yaw 覆盖
     *
     * @return yaw 覆盖值；null 表示跟随全息图整体设置
     */
    @Nullable
    Float getCustomYaw();

    /** 设置自定义 yaw 覆盖（null 表示清除覆盖） */
    void setCustomYaw(@Nullable Float yaw);

    /**
     * 获取自定义 pitch 覆盖
     *
     * @return pitch 覆盖值；null 表示跟随全息图整体设置
     */
    @Nullable
    Float getCustomPitch();

    /** 设置自定义 pitch 覆盖（null 表示清除覆盖） */
    void setCustomPitch(@Nullable Float pitch);

    /** 是否设置了自定义朝向（yaw 或 pitch 任一） */
    boolean hasCustomFacing();

    /** 清除自定义朝向覆盖 */
    void clearCustomFacing();

    /*
     * 权限
     */

    /**
     * 获取本行的查看权限
     *
     * @return 权限节点；null 或空表示无限制
     */
    @Nullable
    String getPermission();

    /** 设置本行的查看权限（null 或空表示无限制） */
    void setPermission(@Nullable String permission);

    /*
     * 显示属性（null 表示继承全息图级别的对应设置）
     */

    /**
     * 获取行级 Billboard 模式
     *
     * @return Billboard 模式；null 表示继承全息图设置
     */
    @Nullable
    Billboard getBillboard();

    /** 设置行级 Billboard 模式（null 表示继承全息图设置） */
    void setBillboard(@Nullable Billboard billboard);

    /**
     * 获取行级亮度覆盖
     *
     * @return 亮度；null 表示继承全息图设置
     */
    @Nullable
    Brightness getBrightness();

    /** 设置行级亮度覆盖（null 表示继承全息图设置） */
    void setBrightness(@Nullable Brightness brightness);

    /** 获取 X 轴缩放覆盖（null 表示继承） */
    @Nullable
    Float getScaleX();

    /** 获取 Y 轴缩放覆盖（null 表示继承） */
    @Nullable
    Float getScaleY();

    /** 获取 Z 轴缩放覆盖（null 表示继承） */
    @Nullable
    Float getScaleZ();

    /**
     * 一次性设置三轴缩放覆盖（null 表示继承全息图设置）
     */
    void setScale(@Nullable Float x, @Nullable Float y, @Nullable Float z);

    /** 获取 X 轴平移覆盖（null 表示继承） */
    @Nullable
    Double getTranslationX();

    /** 获取 Y 轴平移覆盖（null 表示继承） */
    @Nullable
    Double getTranslationY();

    /** 获取 Z 轴平移覆盖（null 表示继承） */
    @Nullable
    Double getTranslationZ();

    /**
     * 一次性设置三轴平移覆盖（null 表示继承全息图设置）
     */
    void setTranslation(@Nullable Double x, @Nullable Double y, @Nullable Double z);

    /** 获取阴影半径覆盖（null 表示继承） */
    @Nullable
    Float getShadowRadius();

    /** 设置阴影半径覆盖（null 表示继承） */
    void setShadowRadius(@Nullable Float shadowRadius);

    /** 获取阴影强度覆盖（null 表示继承） */
    @Nullable
    Float getShadowStrength();

    /** 设置阴影强度覆盖（null 表示继承） */
    void setShadowStrength(@Nullable Float shadowStrength);

    /** 获取发光颜色覆盖（ARGB/RGB 整数，null 表示不覆盖） */
    @Nullable
    Integer getGlowColor();

    /** 设置发光颜色覆盖（null 表示不覆盖） */
    void setGlowColor(@Nullable Integer glowColor);

    /**
     * 获取彩虹背景色的显式设置
     *
     * @return null 表示继承全息图设置
     */
    @Nullable
    Boolean getChromaBackground();

    /** 设置彩虹背景色（null 表示继承全息图设置） */
    void setChromaBackground(@Nullable Boolean chromaBackground);

    /**
     * 获取彩虹发光色的显式设置
     *
     * @return null 表示继承全息图设置
     */
    @Nullable
    Boolean getChromaGlow();

    /** 设置彩虹发光色（null 表示继承全息图设置） */
    void setChromaGlow(@Nullable Boolean chromaGlow);

    /*
     * 标志
     */

    /**
     * 是否设置了指定标志（含页面级/全息图级的继承）
     *
     * @param flag 标志
     * @return 是否生效
     */
    boolean hasFlag(EnumFlag flag);

    /** 添加标志 */
    void addFlags(EnumFlag... flags);

    /** 移除标志 */
    void removeFlag(EnumFlag flag);

    /** 清除本行的全部标志 */
    void clearFlags();

    /**
     * 获取本行设置的标志（不含继承）
     *
     * @return 只读标志集合
     */
    java.util.Set<EnumFlag> getFlags();

    /*
     * 动作
     */

    /**
     * 本行是否配置了点击动作
     *
     * @return 是否有动作
     */
    boolean hasActions();

    /**
     * 为本行添加点击动作
     *
     * <p>添加首个动作时插件会自动重建点击判定实体，无需额外刷新。
     * 动作格式与配置文件一致：{@code TYPE:参数}（如 {@code MESSAGE:你好 {player}}、
     * {@code CONSOLE:say hello}、自定义注册的动作类型）。</p>
     *
     * @param clickType  触发的点击类型
     * @param actionData 动作数据字符串
     * @return 是否成功（格式非法返回 false）
     */
    boolean addAction(ClickType clickType, String actionData);

    /**
     * 获取本行在指定点击类型下的全部动作数据（与 {@link #addAction} 格式一致，可直接回写）
     *
     * @param clickType 点击类型
     * @return 动作数据列表（只读副本，可能为空）
     */
    java.util.List<String> getActionData(ClickType clickType);

    /**
     * 清除本行在指定点击类型下的全部动作
     *
     * @param clickType 点击类型
     */
    void clearActions(ClickType clickType);

    /**
     * 获取本行的位置（仅供只读参考；行位置由页面统一对齐管理）
     *
     * @return 行位置副本
     */
    @Nullable
    Location getLocation();
}
