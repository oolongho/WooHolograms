package com.oolonghoo.holograms.nms.util;

import java.lang.reflect.Field;

/**
 * 反射字段工具类
 * 用于访问和修改类的字段
 *
 * @param <T> 字段类型
 * @author oolongho
 * @since 1.0.0
 */
public class ReflectField<T> {

    private final Class<?> parentClass;
    private final String fieldName;
    private Field field;

    public ReflectField(Class<?> clazz, String fieldName) {
        this.parentClass = clazz;
        this.fieldName = fieldName;
    }

    /**
     * 获取字段的值
     *
     * @param instance 父类的实例（如果是静态字段则为 null）
     * @return 字段的值
     * @throws WooHologramsReflectException 如果无法获取值
     */
    @SuppressWarnings("unchecked")
    public T get(Object instance) {
        initializeField();
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new WooHologramsReflectException("Could not get value of field '" + fieldName + "' in class "
                    + parentClass.getName(), e);
        }
    }

    /**
     * 设置字段的值
     *
     * @param instance 父类的实例（如果是静态字段则为 null）
     * @param value    新的值
     * @throws WooHologramsReflectException 如果无法设置值
     */
    public void set(Object instance, T value) {
        initializeField();
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new WooHologramsReflectException("Could not set value of field '" + fieldName + "' in class "
                    + parentClass.getName(), e);
        }
    }

    private void initializeField() {
        if (field != null) {
            return;
        }
        try {
            field = findField(parentClass, fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new WooHologramsReflectException("Could not find field '" + fieldName + "' in class "
                    + parentClass.getName(), e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field foundField;
        try {
            foundField = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            foundField = clazz.getField(fieldName);
        }
        return foundField;
    }
}
