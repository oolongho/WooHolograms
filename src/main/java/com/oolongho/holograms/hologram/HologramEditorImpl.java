package com.oolongho.holograms.hologram;

import com.oolongho.holograms.api.hologram.Billboard;
import com.oolongho.holograms.api.hologram.Brightness;
import com.oolongho.holograms.api.hologram.EnumFlag;
import com.oolongho.holograms.api.hologram.HologramEditor;
import com.oolongho.holograms.api.event.HologramEditEvent;
import com.oolongho.holograms.api.hologram.TextAlignment;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * HologramEditor 的实现
 *
 * <p>收集批量修改，{@link #apply()} 时按序应用到内部 Hologram。
 * 各 setter 的刷新行为与直接调用一致（属性类修改为无闪烁 metadata 更新），
 * 后续版本可将 apply 优化为单次合并刷新。</p>
 */
public class HologramEditorImpl implements HologramEditor {

    private final Hologram hologram;
    private boolean applied = false;

    private Float facing;
    private Float pitch;
    private Boolean pitchSet;
    private Billboard billboard;
    private TextAlignment alignment;
    private Boolean doubleSided;
    private Integer backgroundAlpha;
    private Integer backgroundColor;
    private Integer lineWidth;
    private Double lineHeight;
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
    private Double displayRange;
    private Double updateRange;
    private Integer updateInterval;
    private String permission;
    private final List<EnumFlag> flags = new ArrayList<>();

    public HologramEditorImpl(Hologram hologram) {
        this.hologram = hologram;
    }

    @Override
    public HologramEditor facing(float facing) {
        this.facing = facing;
        return this;
    }

    @Override
    public HologramEditor pitch(@Nullable Float pitch) {
        this.pitch = pitch;
        this.pitchSet = true;
        return this;
    }

    @Override
    public HologramEditor billboard(Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    @Override
    public HologramEditor alignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public HologramEditor doubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        return this;
    }

    @Override
    public HologramEditor backgroundAlpha(int backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return this;
    }

    @Override
    public HologramEditor backgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @Override
    public HologramEditor lineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    @Override
    public HologramEditor lineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    @Override
    public HologramEditor scale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    @Override
    public HologramEditor translation(double x, double y, double z) {
        this.translationX = x;
        this.translationY = y;
        this.translationZ = z;
        return this;
    }

    @Override
    public HologramEditor shadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        return this;
    }

    @Override
    public HologramEditor shadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
        return this;
    }

    @Override
    public HologramEditor glowColor(int glowColor) {
        this.glowColor = glowColor;
        return this;
    }

    @Override
    public HologramEditor brightness(@Nullable Brightness brightness) {
        this.brightness = brightness;
        return this;
    }

    @Override
    public HologramEditor chromaBackground(boolean chromaBackground) {
        this.chromaBackground = chromaBackground;
        return this;
    }

    @Override
    public HologramEditor chromaGlow(boolean chromaGlow) {
        this.chromaGlow = chromaGlow;
        return this;
    }

    @Override
    public HologramEditor displayRange(double displayRange) {
        this.displayRange = displayRange;
        return this;
    }

    @Override
    public HologramEditor updateRange(double updateRange) {
        this.updateRange = updateRange;
        return this;
    }

    @Override
    public HologramEditor updateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    @Override
    public HologramEditor permission(@Nullable String permission) {
        // null 表示不修改（apply 时跳过）；空字符串清除权限
        this.permission = permission;
        return this;
    }

    @Override
    public HologramEditor addFlags(EnumFlag... flags) {
        for (EnumFlag flag : flags) {
            if (flag != null) {
                this.flags.add(flag);
            }
        }
        return this;
    }

    @Override
    public void apply() {
        if (applied) {
            throw new IllegalStateException("HologramEditor 已提交，不可重复使用");
        }
        applied = true;

        if (facing != null) hologram.setFacing(facing);
        if (pitchSet != null) hologram.setPitch(pitch);
        if (billboard != null) hologram.setBillboard(billboard);
        if (alignment != null) hologram.setAlignment(alignment);
        if (doubleSided != null) hologram.setDoubleSided(doubleSided);
        if (backgroundAlpha != null) hologram.setBackgroundAlpha(backgroundAlpha);
        if (backgroundColor != null) hologram.setBackgroundColor(backgroundColor);
        if (lineWidth != null) hologram.setLineWidth(lineWidth);
        if (lineHeight != null) hologram.setLineHeight(lineHeight);
        if (scaleX != null || scaleY != null || scaleZ != null) {
            hologram.setScale(
                    scaleX != null ? scaleX : hologram.getScaleX(),
                    scaleY != null ? scaleY : hologram.getScaleY(),
                    scaleZ != null ? scaleZ : hologram.getScaleZ());
        }
        if (translationX != null || translationY != null || translationZ != null) {
            hologram.setTranslation(
                    translationX != null ? translationX : hologram.getTranslationX(),
                    translationY != null ? translationY : hologram.getTranslationY(),
                    translationZ != null ? translationZ : hologram.getTranslationZ());
        }
        if (shadowRadius != null) hologram.setShadowRadius(shadowRadius);
        if (shadowStrength != null) hologram.setShadowStrength(shadowStrength);
        if (glowColor != null) hologram.setGlowColor(glowColor);
        if (brightness != null) hologram.setBrightness(brightness);
        if (chromaBackground != null) hologram.setChromaBackground(chromaBackground);
        if (chromaGlow != null) hologram.setChromaGlow(chromaGlow);
        if (displayRange != null) hologram.setDisplayRange(displayRange);
        if (updateRange != null) hologram.setUpdateRange(updateRange);
        if (updateInterval != null) hologram.setUpdateInterval(updateInterval);
        // null = 不修改；空字符串 = 清除权限（hasPermission 视 null/空为无限制）
        if (permission != null) hologram.setPermission(permission);
        if (!flags.isEmpty()) hologram.addFlags(flags.toArray(new EnumFlag[0]));

        // 编辑事件（修改已生效，不可取消）
        Bukkit.getPluginManager().callEvent(new HologramEditEvent(hologram));
    }
}
