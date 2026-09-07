package com.oolongho.holograms.api.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 全息图构建器（流式创建）
 *
 * <p>通过 {@link HologramRegistry#builder(String, Location)} 获取：</p>
 * <pre>{@code
 * Hologram holo = api.holograms().builder("shop", location)
 *         .line(HologramLines.text("&6&l商店"))
 *         .line(HologramLines.icon(Material.EMERALD))
 *         .lineHeight(0.3)
 *         .billboard(Billboard.CENTER)
 *         .create();
 * }</pre>
 *
 * <p>未显式设置的属性沿用插件配置的默认值。
 * 默认在无内容时补一行空文本，保证页面非空可显示。</p>
 */
public final class HologramBuilder {

    private final HologramRegistry registry;
    private final String name;
    private final Location location;

    /** 单行内容与偏移 */
    private static final class LineSpec {
        final String content;
        final double offsetX;
        final double offsetY;
        final double offsetZ;

        LineSpec(String content, double offsetX, double offsetY, double offsetZ) {
            this.content = content;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
    }

    /** 第一页的行（与 page() 追加的后续页区分） */
    private final List<LineSpec> lines = new ArrayList<>();
    /** page() 追加的额外页（每页多行） */
    private final List<List<LineSpec>> pages = new ArrayList<>();

    private boolean temporary = false;
    private long expireAfterTicks = -1;
    private double lineHeight = -1;
    private double displayRange = -1;
    private double updateRange = -1;
    private int updateInterval = -1;
    private Billboard billboard;
    private TextAlignment alignment;
    private Boolean doubleSided;
    private Float facing;
    private Float pitch;
    private Integer backgroundAlpha;
    private Integer backgroundColor;
    private Integer lineWidth;
    private Float scaleX;
    private Float scaleY;
    private Float scaleZ;
    private Double translationX;
    private Double translationY;
    private Double translationZ;
    private Float shadowRadius;
    private Float shadowStrength;
    private Integer glowColor;
    private Brightness brightness;
    private Boolean chromaBackground;
    private Boolean chromaGlow;
    private String permission;
    private final List<EnumFlag> flags = new ArrayList<>();

    /**
     * 构造器。调用方应通过 {@link HologramRegistry#builder(String, Location)} 获取构建器
     */
    public HologramBuilder(HologramRegistry registry, String name, Location location) {
        this.registry = registry;
        this.name = name;
        this.location = location;
    }

    /**
     * 追加一行内容
     *
     * @param content 行内容（可用 {@link com.oolongho.holograms.api.content.HologramLines} 工厂构造）
     * @return this
     */
    public HologramBuilder line(String content) {
        lines.add(new LineSpec(content, 0, 0, 0));
        return this;
    }

    /**
     * 追加一行带 X/Y/Z 偏移的内容（偏移行独立渲染，不占据纵向空间，可用于错位/双列布局）
     *
     * @param content 行内容
     * @param x       X 轴偏移
     * @param y       Y 轴偏移
     * @param z       Z 轴偏移
     * @return this
     */
    public HologramBuilder offsetLine(String content, double x, double y, double z) {
        lines.add(new LineSpec(content, x, y, z));
        return this;
    }

    /**
     * 追加多行内容
     *
     * @param contents 行内容数组
     * @return this
     */
    public HologramBuilder lines(String... contents) {
        for (String content : contents) {
            line(content);
        }
        return this;
    }

    /**
     * 开启一个新页面（后续 line()/lines() 的内容写入新页）
     *
     * <pre>{@code
     * builder.line("第一页标题")
     *        .page("第二页标题", "第二页内容")
     *        .create();
     * }</pre>
     *
     * @param lines 新页的行内容（可为空数组，稍后再补）
     * @return this
     */
    public HologramBuilder page(String... lines) {
        List<LineSpec> page = new ArrayList<>();
        for (String content : lines) {
            page.add(new LineSpec(content, 0, 0, 0));
        }
        pages.add(page);
        return this;
    }

    /**
     * 设为临时全息图（不写入存储文件，重启后消失）
     *
     * @return this
     */
    public HologramBuilder temporary() {
        this.temporary = true;
        return this;
    }

    /**
     * 设置存活时长：创建后延迟指定 tick 自动删除（隐含临时全息图，不写入文件）。
     * 适用于副本特效、商店预览、活动倒计时等场景
     *
     * @param ticks 存活 tick 数（20 tick = 1 秒）
     * @return this
     */
    public HologramBuilder expireAfter(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("expireAfter 不能为负: " + ticks);
        }
        this.expireAfterTicks = ticks;
        this.temporary = true;
        return this;
    }

    /** 设置行高 */
    public HologramBuilder lineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    /** 设置显示范围（方块） */
    public HologramBuilder displayRange(double displayRange) {
        this.displayRange = displayRange;
        return this;
    }

    /** 设置更新范围（方块） */
    public HologramBuilder updateRange(double updateRange) {
        this.updateRange = updateRange;
        return this;
    }

    /** 设置自动刷新间隔（tick，0 = 禁用） */
    public HologramBuilder updateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    /** 设置 Billboard 模式 */
    public HologramBuilder billboard(Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    /** 设置文本对齐方式 */
    public HologramBuilder alignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /** 设置双面渲染 */
    public HologramBuilder doubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        return this;
    }

    /** 设置整体朝向（yaw，度） */
    public HologramBuilder facing(float facing) {
        this.facing = facing;
        return this;
    }

    /** 设置垂直倾斜角度（-90~90，仅 FIXED_ANGLE 模式） */
    public HologramBuilder pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    /** 设置背景透明度（0~255） */
    public HologramBuilder backgroundAlpha(int backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return this;
    }

    /** 设置背景颜色（RGB） */
    public HologramBuilder backgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /** 设置文本自动换行宽度（像素） */
    public HologramBuilder lineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    /** 设置三轴缩放 */
    public HologramBuilder scale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    /** 设置三轴平移 */
    public HologramBuilder translation(double x, double y, double z) {
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
        return this;
    }

    /** 设置阴影半径 */
    public HologramBuilder shadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        return this;
    }

    /** 设置阴影强度 */
    public HologramBuilder shadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
        return this;
    }

    /** 设置发光颜色覆盖（-1 表示不覆盖） */
    public HologramBuilder glowColor(int glowColor) {
        this.glowColor = glowColor;
        return this;
    }

    /** 设置亮度覆盖（null 表示继承环境光照） */
    public HologramBuilder brightness(@Nullable Brightness brightness) {
        this.brightness = brightness;
        return this;
    }

    /** 设置彩虹背景色 */
    public HologramBuilder chromaBackground(boolean chromaBackground) {
        this.chromaBackground = chromaBackground;
        return this;
    }

    /** 设置彩虹发光色 */
    public HologramBuilder chromaGlow(boolean chromaGlow) {
        this.chromaGlow = chromaGlow;
        return this;
    }

    /** 同时设置彩虹背景色与彩虹发光色 */
    public HologramBuilder chroma(boolean enabled) {
        this.chromaBackground = enabled;
        this.chromaGlow = enabled;
        return this;
    }

    /** 设置查看权限（null 或空字符串表示无限制） */
    public HologramBuilder permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    /** 添加标志 */
    public HologramBuilder flags(EnumFlag... flags) {
        for (EnumFlag flag : flags) {
            if (flag != null) {
                this.flags.add(flag);
            }
        }
        return this;
    }

    /**
     * 构建并注册全息图
     *
     * @return 创建的全息图
     * @throws IllegalStateException 名称非法/已存在、位置无效或超出每世界数量限制
     */
    public Hologram create() {
        Hologram hologram = registry.createHologram(name, location.clone(), !temporary);
        if (hologram == null) {
            throw new IllegalStateException("创建全息图失败: " + name + "（名称非法/已存在、位置无效或超出每世界数量限制）");
        }

        // 应用可选属性（-1/null 表示未设置，沿用默认值）
        if (lineHeight > 0) hologram.setLineHeight(lineHeight);
        if (displayRange > 0) hologram.setDisplayRange(displayRange);
        if (updateRange > 0) hologram.setUpdateRange(updateRange);
        if (updateInterval >= 0) hologram.setUpdateInterval(updateInterval);
        if (billboard != null) hologram.setBillboard(billboard);
        if (alignment != null) hologram.setAlignment(alignment);
        if (doubleSided != null) hologram.setDoubleSided(doubleSided);
        if (facing != null) hologram.setFacing(facing);
        if (pitch != null) hologram.setPitch(pitch);
        if (backgroundAlpha != null) hologram.setBackgroundAlpha(backgroundAlpha);
        if (backgroundColor != null) hologram.setBackgroundColor(backgroundColor);
        if (lineWidth != null) hologram.setLineWidth(lineWidth);
        if (scaleX != null || scaleY != null || scaleZ != null) {
            hologram.setScale(
                    scaleX != null ? scaleX : 1.0f,
                    scaleY != null ? scaleY : 1.0f,
                    scaleZ != null ? scaleZ : 1.0f);
        }
        if (translationX != null || translationY != null || translationZ != null) {
            hologram.setTranslation(
                    translationX != null ? translationX : 0.0,
                    translationY != null ? translationY : 0.0,
                    translationZ != null ? translationZ : 0.0);
        }
        if (shadowRadius != null) hologram.setShadowRadius(shadowRadius);
        if (shadowStrength != null) hologram.setShadowStrength(shadowStrength);
        if (glowColor != null) hologram.setGlowColor(glowColor);
        if (brightness != null) hologram.setBrightness(brightness);
        if (chromaBackground != null) hologram.setChromaBackground(chromaBackground);
        if (chromaGlow != null) hologram.setChromaGlow(chromaGlow);
        if (permission != null) hologram.setPermission(permission);
        if (!flags.isEmpty()) hologram.addFlags(flags.toArray(new EnumFlag[0]));

        // 写入行内容（保证首页非空可显示）
        writePage(hologram.getPage(0), lines);
        for (List<LineSpec> pageLines : pages) {
            writePage(hologram.addPage(), pageLines);
        }

        // 创建时页面为空未能立即显示，内容就绪后向范围内玩家显示
        hologram.showToNearby();

        // 定时销毁
        if (expireAfterTicks >= 0) {
            registry.scheduleDeletion(hologram, expireAfterTicks);
        }
        return hologram;
    }

    /**
     * 向指定页面写入行内容；空页自动补一行空文本保证可显示；偏移行应用偏移
     */
    private static void writePage(HologramPage page, List<LineSpec> specs) {
        if (page == null) {
            return;
        }
        List<LineSpec> finalSpecs = specs.isEmpty() ? List.of(new LineSpec("", 0, 0, 0)) : specs;
        for (LineSpec spec : finalSpecs) {
            if (spec.content == null) {
                continue;
            }
            HologramLine line = page.addLine(spec.content);
            if (line != null && (spec.offsetX != 0 || spec.offsetY != 0 || spec.offsetZ != 0)) {
                line.setOffset(spec.offsetX, spec.offsetY, spec.offsetZ);
            }
        }
    }
}
