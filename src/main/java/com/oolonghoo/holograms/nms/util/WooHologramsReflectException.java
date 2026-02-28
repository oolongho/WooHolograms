package com.oolonghoo.holograms.nms.util;

/**
 * 反射异常类
 *
 * @author oolongho
 * @since 1.0.0
 */
public class WooHologramsReflectException extends RuntimeException {

    public WooHologramsReflectException(String message) {
        super(message);
    }

    public WooHologramsReflectException(String message, Throwable cause) {
        super(message, cause);
    }
}
