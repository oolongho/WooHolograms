package com.oolonghoo.holograms.nms.util;

import java.lang.reflect.Field;

/**
 * 反射工具类
 *
 * 
 * 
 */
public class ReflectUtil {

    public static boolean isPaper = false;

    static {
        try {
            ReflectUtil.getClass("io.papermc.paper.PaperBootstrap");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private ReflectUtil() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 获取类中静态字段的值
     *
     * @param clazz     包含该字段的类
     * @param fieldName 字段名称
     * @param <T>       字段类型
     * @return 字段的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new WooHologramsException("Could not find field " + fieldName + " in class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new WooHologramsException("Could not access field " + fieldName + " in class "
                    + clazz.getName(), e);
        } catch (Exception e) {
            throw new WooHologramsException("Unexpected error occurred while getting value of field " + fieldName
                    + " in class " + clazz.getName(), e);
        }
    }

    /**
     * 通过名称获取类
     *
     * @param className 类名
     * @return 类对象
     * @throws ClassNotFoundException 如果类不存在
     */
    public static Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
