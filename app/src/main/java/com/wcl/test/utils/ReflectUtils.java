package com.wcl.test.utils;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    private static final String TAG = "ReflectUtils";

    private ReflectUtils() {
        // 私有构造函数防止实例化
    }

    /**
     * 获取指定类的DeclaredMethod（支持继承链查找）
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> clazz = (object instanceof Class<?>) ? (Class<?>) object : object.getClass();

        while (clazz != Object.class) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchMethodException("Method " + methodName + " not found in " + object.getClass());
    }

    /**
     * 调用指定类的静态方法
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... parameters) throws Exception {
        Method method = getDeclaredMethod(clazz, methodName, parameterTypes);
        return method.invoke(null, parameters);
    }

    /**
     * 调用对象的方法
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... parameters) throws Exception {
        Method method = getDeclaredMethod(object, methodName, parameterTypes);
        return method.invoke(object, parameters);
    }

    /**
     * 获取指定类的DeclaredField（支持继承链查找）
     */
    public static Field getDeclaredField(Object object, String fieldName) throws NoSuchFieldException {
        Class<?> clazz = (object instanceof Class<?>) ? (Class<?>) object : object.getClass();

        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + fieldName + " not found in " + object.getClass());
    }

    /**
     * 设置对象的属性值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(object, fieldName);
        field.set(object, value);
    }

    /**
     * 获取对象的属性值
     */
    public static Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = getDeclaredField(object, fieldName);
        return field.get(object);
    }

    /**
     * 根据类名加载类
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * 获取当前Application上下文（必须在主线程调用）
     */
    public static Context getApplicationContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            return (Context) currentApplicationMethod.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get application context", e);
        }
    }

    /**
     * 创建指定类的新实例
     */
    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) throws Exception {
        Constructor<T> constructor = clazz.getConstructor(paramTypes);
        return constructor.newInstance(args);
    }

    /**
     * 获取静态字段的值
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);
        return field.get(null);
    }

    /**
     * 设置静态字段的值
     */
    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);
        field.set(null, value);
    }

    /**
     * 通过asInterface方法获取接口实例
     */
    public static Object stubAsInterface(Class<?> clazz, IBinder binder) {
        try {
            Method asInterfaceMethod = clazz.getDeclaredMethod("asInterface", IBinder.class);
            asInterfaceMethod.setAccessible(true);
            return asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call asInterface method", e);
        }
    }
}
