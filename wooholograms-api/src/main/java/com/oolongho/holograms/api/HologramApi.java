package com.oolongho.holograms.api;

import com.oolongho.holograms.api.hologram.HologramRegistry;
import com.oolongho.holograms.api.registry.ActionTypeRegistry;
import com.oolongho.holograms.api.registry.AnimationRegistry;

/**
 * WooHolograms 公共 API 顶层入口
 *
 * <p>通过 {@link WooHologramsApiProvider#get()} 获取实例：</p>
 * <pre>{@code
 * HologramApi api = WooHologramsApiProvider.getOrThrow();
 * Hologram holo = api.holograms().createHologram("shop", location);
 * }</pre>
 *
 * <p><b>兼容性承诺：</b>{@link #getApiVersion()} 标识 API 形状版本。
 * 同一主版本内（主版本号不变）保证二进制向后兼容：
 * 只新增方法与接口，不修改/删除既有签名。</p>
 */
public interface HologramApi {

    /** 当前 API 形状版本 */
    String API_VERSION = "2.0";

    /**
     * 获取 API 形状版本
     *
     * @return 版本字符串（如 "2.0"）
     */
    default String getApiVersion() {
        return API_VERSION;
    }

    /**
     * 获取全息图注册表（查询/创建/克隆/删除入口）
     *
     * @return 注册表实例
     */
    HologramRegistry holograms();

    /**
     * 获取动作类型注册表（注册自定义点击动作）
     *
     * @return 注册表实例
     */
    ActionTypeRegistry actions();

    /**
     * 获取动画注册表（注册自定义文本动画）
     *
     * @return 注册表实例
     */
    AnimationRegistry animations();
}
