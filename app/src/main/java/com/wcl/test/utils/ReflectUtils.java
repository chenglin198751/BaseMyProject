package com.wcl.test.utils;

import android.content.Context;
import android.os.IBinder;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
    public ReflectUtils() {
    }

    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Class clazz = object instanceof Class ? (Class) object : object.getClass();

        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception var5) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new RuntimeException("getDeclaredMethod exception, object = " + object + ", methodName = " + methodName);
    }

    public static Object invokeMethod(Object receiver, String methodName, Class<?>[] parameterTypes, Object... parameters) {
        try {
            Method method = getDeclaredMethod(receiver, methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(receiver, parameters);
        } catch (Exception var5) {
            throw new RuntimeException("invokeMethod exception, receiver = " + receiver + ", methodName = " + methodName, var5);
        }
    }

    public static Field getDeclaredField(Object object, String fieldName) {
        Class clazz = object.getClass();

        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (Exception var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new RuntimeException("getDeclaredField exception, object = " + object + ", fieldName = " + fieldName);
    }

    /**
     * 设置对象的属性值
     * 参数：对象、属性名，属性值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = getDeclaredField(object, fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception var4) {
            throw new RuntimeException("setFieldValue exception, object = " + object + ", fieldName = " + fieldName, var4);
        }
    }

    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = getDeclaredField(object, fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception var3) {
            throw new RuntimeException("getFieldValue exception, object = " + object + ", fieldName = " + fieldName, var3);
        }
    }

    public static Class classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var2) {
            throw new RuntimeException("getClass exception, className = " + className, var2);
        }
    }

    public static Object stubAsInterface(String clazz, IBinder binder) {
        return stubAsInterface(classForName(clazz), binder);
    }

    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] parameterTypes, Object... parameters) {
        try {
            Method method = getDeclaredMethod(classForName(className), methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke((Object) null, parameters);
        } catch (Exception var5) {
            throw new RuntimeException("invokeStaticMethod exception, className = " + className + ", methodName = " + methodName, var5);
        }
    }

    public static Object stubAsInterface(Class clazz, IBinder binder) {
        try {
            return clazz.getDeclaredMethod("asInterface", IBinder.class).invoke((Object) null, binder);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (InvocationTargetException var4) {
            throw new RuntimeException(var4);
        } catch (NoSuchMethodException var5) {
            throw new RuntimeException(var5);
        }
    }

    public static Method getMethod(String calssName, String methodName, Class<?>... paremerters) {
        Method method = null;

        try {
            Class<?> mClass = Class.forName(calssName);
            method = mClass.getDeclaredMethod(methodName, paremerters);
        } catch (SecurityException | NoSuchMethodException | ClassNotFoundException var5) {
            if (LogUtils.isDebug()) {
                var5.printStackTrace();
            }
        }

        return method;
    }

    public static Method getMethod(Class<?> cls, String methodName, Class<?>[] paramTypes) {
        Method method = null;
        if (methodName != null && methodName.length() > 0) {
            try {
                method = cls.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException var5) {
                var5.printStackTrace();
            }
        }

        return method;
    }

    public static Object invoke(Object receiver, Method method, Object... paremeters) {
        Object result = null;

        try {
            result = method.invoke(receiver, paremeters);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException var5) {
            if (LogUtils.isDebug()) {
                var5.printStackTrace();
            }
        }

        return result;
    }

    public static String getSystemProperties(String propertyName, String defaultValue) {
        String result = defaultValue;

        try {
            Class<?> className = Class.forName("android.os.SystemProperties");
            Method method = className.getDeclaredMethod("get", String.class, String.class);
            result = (String) method.invoke((Object) null, propertyName, defaultValue);
        } catch (SecurityException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException var5) {
            if (LogUtils.isDebug()) {
                var5.printStackTrace();
            }
        }

        return result;
    }

    public static Context getApplicationContext() {
        LogUtils.d("ReflectUtil", "must be MainThread!" + (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()));
        Context context = null;

        try {
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Method method = clazz.getDeclaredMethod("currentApplication");
            context = (Context) method.invoke((Object) null);
        } catch (SecurityException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException var3) {
            if (LogUtils.isDebug()) {
                var3.printStackTrace();
            }
        }

        return context;
    }

    public static Constructor getObjectConstructor(String className, Class... paramsTypes) {
        try {
            return Class.forName(className).getConstructor(paramsTypes);
        } catch (NoSuchMethodException var3) {
            throw new RuntimeException(var3);
        } catch (ClassNotFoundException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static Object getObjectNewInstance(String className, Class[] paramsTypes, Object... args) {
        try {
            return Class.forName(className).getConstructor(paramsTypes).newInstance(args);
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    /**
     * 获取对象的Object类型属性值
     * 参数：对象、属性名
     */
    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return prepareField(obj.getClass(), fieldName).get(obj);
    }

    /**
     * 设置对象的属性值
     * 参数：对象、属性名，属性值
     */
    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        prepareField(obj.getClass(), fieldName).set(obj, value);
    }

    /**
     * 获取对象的int类型属性值
     * 参数：对象、属性名
     */
    public static int getIntField(Object object, String fieldName) {
        try {
            return object.getClass().getDeclaredField(fieldName).getInt(object);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (NoSuchFieldException var4) {
            throw new RuntimeException(var4);
        }
    }

    /**
     * 获取对象的object类型属性值
     * 参数：对象、属性名
     */
    public static Object getObjectField(Object object, String fieldName) {
        try {
            return object.getClass().getDeclaredField(fieldName).get(object);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (NoSuchFieldException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static Object getObjectFieldNoDeclared(Object object, String fieldName) {
        try {
            return object.getClass().getField(fieldName).get(object);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (NoSuchFieldException var4) {
            throw new RuntimeException(var4);
        }
    }

    /**
     * 获取一个静态类的int属性值
     * 参数：类名(如BaseUtils.class.getName())、属性名
     */
    public static int getStaticIntField(String className, String fieldName) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt((Object) null);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (NoSuchFieldException var4) {
            throw new RuntimeException(var4);
        } catch (ClassNotFoundException var5) {
            throw new RuntimeException(var5);
        }
    }

    /**
     * 设置一个静态类的Object属性值
     * 参数：类名(如BaseUtils.class.getName())、属性名、属性值
     */
    public static void setStaticObjectField(String className, String fieldName, Object value) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set((Object) null, value);
        } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4);
        } catch (NoSuchFieldException var5) {
            throw new RuntimeException(var5);
        } catch (ClassNotFoundException var6) {
            throw new RuntimeException(var6);
        }
    }

    /**
     * 获取一个静态类的Object属性值
     * 参数：类名(如BaseUtils.class.getName())、属性名
     */
    public static Object getStaticObjectField(String className, String fieldName) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get((Object) null);
        } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3);
        } catch (NoSuchFieldException var4) {
            throw new RuntimeException(var4);
        } catch (ClassNotFoundException var5) {
            throw new RuntimeException(var5);
        }
    }

    public static Field prepareField(Class<?> c, String fieldName) throws NoSuchFieldException {
        while (true) {
            if (c != null) {
                Field var3;
                try {
                    Field f = c.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    var3 = f;
                } catch (Exception var7) {
                    continue;
                } finally {
                    c = c.getSuperclass();
                }

                return var3;
            }

            throw new NoSuchFieldException();
        }
    }
}
