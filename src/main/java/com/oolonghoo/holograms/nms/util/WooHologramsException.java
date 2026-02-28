package com.oolonghoo.holograms.nms.util;

/**
 * WooHolograms 异常类
 *
 * @author oolongho
 * @since 1.0.0
 */
public class WooHologramsException extends RuntimeException {

    public WooHologramsException(String message) {
        super(message);
    }

    public WooHologramsException(String message, Throwable cause) {
        super(message, cause);
    }
}
