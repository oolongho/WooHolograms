package com.oolonghoo.holograms.nms.versions;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 字节缓冲区包装类
 * 用于数据包序列化
 *
 * 
 * 
 */
class FriendlyByteBufWrapper {

    private static final ThreadLocal<FriendlyByteBufWrapper> LOCAL_INSTANCE = ThreadLocal.withInitial(
            FriendlyByteBufWrapper::new);

    private final FriendlyByteBuf serializer;

    private FriendlyByteBufWrapper() {
        this.serializer = new FriendlyByteBuf(Unpooled.buffer());
    }

    /**
     * 获取底层的 FriendlyByteBuf
     *
     * @return FriendlyByteBuf
     */
    FriendlyByteBuf getSerializer() {
        return serializer;
    }

    /**
     * 清空缓冲区
     */
    void clear() {
        serializer.clear();
    }

    /**
     * 写入整数数组
     *
     * @param array 整数数组
     */
    void writeIntArray(int[] array) {
        serializer.writeVarIntArray(array);
    }

    /**
     * 写入 VarInt
     *
     * @param value 值
     */
    void writeVarInt(int value) {
        serializer.writeVarInt(value);
    }

    /**
     * 读取 VarInt
     *
     * @return 值
     */
    int readVarInt() {
        return serializer.readVarInt();
    }

    /**
     * 获取线程本地实例
     *
     * @return FriendlyByteBufWrapper 实例
     */
    static FriendlyByteBufWrapper getInstance() {
        FriendlyByteBufWrapper instance = LOCAL_INSTANCE.get();
        instance.clear();
        return instance;
    }
}
