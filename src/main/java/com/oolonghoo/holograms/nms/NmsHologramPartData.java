package com.oolonghoo.holograms.nms;

import com.oolonghoo.holograms.nms.util.DecentPosition;

import java.util.function.Supplier;

/**
 * 全息图部件数据类
 * 用于存储单个全息图部件的渲染数据
 *
 * @param <T> 全息图部件内容的类型
 * @author oolongho
 * @since 1.0.0
 */
public class NmsHologramPartData<T> {

    private final Supplier<DecentPosition> positionSupplier;
    private final Supplier<T> contentSupplier;

    /**
     * 构造一个新的 NmsHologramPartData 对象
     *
     * @param positionSupplier 提供全息图部件位置的供应器
     * @param contentSupplier  提供全息图部件内容的供应器
     */
    public NmsHologramPartData(Supplier<DecentPosition> positionSupplier, Supplier<T> contentSupplier) {
        this.positionSupplier = positionSupplier;
        this.contentSupplier = contentSupplier;
    }

    /**
     * 获取全息图部件的位置
     *
     * @return 位置
     */
    public DecentPosition getPosition() {
        return positionSupplier.get();
    }

    /**
     * 获取全息图部件的内容
     *
     * @return 内容
     */
    public T getContent() {
        return contentSupplier.get();
    }
}
