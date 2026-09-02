package com.wcl.test.utils;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具类，提供方法/字段的查找、调用与读写，以及基于反射的实例化、Application 获取和 AIDL 接口反序列化。
 *
 * <p>所有通过本类获取的 {@link Method}、{@link Field}、{@link Constructor} 都会自动
 * {@code setAccessible(true)}，因此可以访问 private 成员；其中
 * {@link #getDeclaredMethod(Object, String, Class[])} 与 {@link #getDeclaredField(Object, String)}
 * 还支持沿继承链向上查找父类成员。</p>
 */
public class ReflectUtils {
    private ReflectUtils() {
        // 私有构造函数防止实例化
    }

    /**
     * 获取指定类的 Declared Method（含 private，并沿继承链向上查找父类）。
     *
     * <p>如果入参本身就是 {@link Class}，则以该 Class 作为起始类型；否则使用对象的运行时类型。
     * 查找顺序从当前类开始，逐级向上遍历父类，直到 {@code Object}；找到的方法会自动
     * {@code setAccessible(true)}，因此可以直接调用 private 方法。</p>
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Parent {
     *     private void hidden() { }
     * }
     *
     * class Sample extends Parent {
     *     void run(String msg) { }
     * }
     *
     * // 获取当前类声明的 run 方法
     * Method m1 = ReflectUtils.getDeclaredMethod(new Sample(), "run", String.class);
     *
     * // 沿继承链获取父类的私有 hidden 方法
     * Method m2 = ReflectUtils.getDeclaredMethod(new Sample(), "hidden");
     * }</pre>
     *
     * @param object         目标对象或目标 Class
     * @param methodName     方法名
     * @param parameterTypes 方法形参类型
     * @return 可访问的目标 Method
     * @throws NoSuchMethodException 当前类及其所有父类中都找不到该方法时抛出
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> originalClass = getTargetClass(object);
        Class<?> clazz = originalClass;

        while (clazz != Object.class) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchMethodException("Method " + methodName + " not found in " + originalClass);
    }

    /**
     * 调用指定类的静态方法（支持 private 静态方法）。
     *
     * <p>内部通过 {@link #getDeclaredMethod(Object, String, Class[])} 查找方法，以空 receiver 调用，
     * 因此可以触发那些无法直接访问的私有静态方法，例如系统隐藏 API 或第三方库的内部工具方法。</p>
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     public static void log(String msg) { }
     *     private static int secret() { return 42; }
     * }
     *
     * // 调用 public 静态方法（无返回值）
     * ReflectUtils.invokeStaticMethod(Sample.class, "log",
     *         new Class[]{String.class}, "hello");
     *
     * // 调用 private 静态方法（有返回值）
     * int result = (Integer) ReflectUtils.invokeStaticMethod(Sample.class, "secret",
     *         new Class[]{}, (Object[]) null);
     * }</pre>
     *
     * @param clazz          目标类
     * @param methodName     方法名
     * @param parameterTypes 方法形参类型数组
     * @param parameters     实际入参
     * @return 方法返回值；若目标方法返回 void，则为 null
     * @throws Exception 方法不存在、无访问权限或方法内部抛异常时抛出
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... parameters) throws Exception {
        Method method = getDeclaredMethod(clazz, methodName, parameterTypes);
        return method.invoke(null, parameters);
    }

    /**
     * 调用目标对象的方法（支持 private 实例方法）。
     *
     * <p>基于 {@link #getDeclaredMethod(Object, String, Class[])} 查找方法后，以目标对象作为 receiver 调用。</p>
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     private String secretName() { return "demo"; }
     * }
     *
     * Sample sample = new Sample();
     * String name = (String) ReflectUtils.invokeMethod(sample, "secretName",
     *         new Class[]{}, (Object[]) null);
     * }</pre>
     *
     * @param object         目标对象
     * @param methodName     方法名
     * @param parameterTypes 方法形参类型数组
     * @param parameters     实际入参
     * @return 方法返回值；若目标方法返回 void，则为 null
     * @throws Exception 方法不存在、无访问权限或方法内部抛异常时抛出
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... parameters) throws Exception {
        Method method = getDeclaredMethod(object, methodName, parameterTypes);
        return method.invoke(object, parameters);
    }

    /**
     * 获取指定类的 Declared Field（含 private，并沿继承链向上查找父类）。
     *
     * <p>行为与 {@link #getDeclaredMethod(Object, String, Class[])} 一致，只是作用于字段；
     * 找到的字段会自动 {@code setAccessible(true)}。</p>
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Parent {
     *     private int pid;
     * }
     *
     * class Sample extends Parent {
     *     private String name;
     * }
     *
     * // 获取当前类声明的 name 字段
     * Field f1 = ReflectUtils.getDeclaredField(new Sample(), "name");
     *
     * // 沿继承链获取父类的 pid 字段
     * Field f2 = ReflectUtils.getDeclaredField(new Sample(), "pid");
     * }</pre>
     *
     * @param object    目标对象或目标 Class
     * @param fieldName 字段名
     * @return 可访问的目标 Field
     * @throws NoSuchFieldException 当前类及其所有父类中都找不到该字段时抛出
     */
    public static Field getDeclaredField(Object object, String fieldName) throws NoSuchFieldException {
        Class<?> originalClass = getTargetClass(object);
        Class<?> clazz = originalClass;

        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + fieldName + " not found in " + originalClass);
    }

    /**
     * 解析目标类型：传入 Class 对象时直接使用，否则取对象的运行时类型。
     */
    private static Class<?> getTargetClass(Object object) {
        return (object instanceof Class<?>) ? (Class<?>) object : object.getClass();
    }

    /**
     * 设置对象的实例字段值（支持 private 字段）。
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     private String name;
     * }
     *
     * Sample sample = new Sample();
     * ReflectUtils.setFieldValue(sample, "name", "newName");
     * }</pre>
     *
     * @param object    目标对象
     * @param fieldName 字段名
     * @param value     要写入的字段值
     * @throws Exception 字段不存在、无访问权限或类型不匹配时抛出
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(object, fieldName);
        field.set(object, value);
    }

    /**
     * 获取对象的实例字段值（支持 private 字段）。
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     private String name = "demo";
     * }
     *
     * String name = (String) ReflectUtils.getFieldValue(new Sample(), "name");
     * }</pre>
     *
     * @param object    目标对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 字段不存在或无访问权限时抛出
     */
    public static Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = getDeclaredField(object, fieldName);
        return field.get(object);
    }

    /**
     * 根据全限定类名加载类。
     *
     * <p>等价于 {@link Class#forName(String)}，用于反射系统隐藏类或按配置动态加载类。</p>
     *
     * <pre>{@code
     * Collection class1 = ReflectUtils.classForName("java.util.ArrayList");
     *
     * Collection class2 = ReflectUtils.classForName("android.app.ActivityThread");
     * }</pre>
     *
     * @param className 类的全限定名
     * @return 加载到的 Class
     * @throws ClassNotFoundException 找不到该类时抛出
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * 通过反射获取当前进程的 Application 上下文。
     *
     * <p>内部反射调用 {@code android.app.ActivityThread.currentApplication()} 静态方法，
     * 适用于没有显式 Context 却又需要 Application 的场景。</p>
     *
     * <pre>{@code
     * Context appContext = ReflectUtils.getApplicationContext();
     * }</pre>
     *
     * @return 当前进程的 Application Context；某些极端情况下可能为 null
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
     * 通过构造器创建指定类的新实例。
     *
     * <p>只支持 public 构造器（底层调用 {@link Class#getConstructor(Class[])}）。</p>
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     public Sample() { }
     *     public Sample(String name) { }
     * }
     *
     * // 无参构造
     * Sample a = ReflectUtils.createInstance(Sample.class, new Class[]{});
     *
     * // 有参构造
     * Sample b = ReflectUtils.createInstance(Sample.class, new Class[]{String.class}, "demo");
     * }</pre>
     *
     * @param clazz     目标类
     * @param paramTypes 构造器形参类型数组
     * @param args      构造器实际入参
     * @param <T>       目标类型
     * @return 目标类的新实例
     * @throws Exception 无匹配构造器、无访问权限或构造内部抛异常时抛出
     */
    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) throws Exception {
        Constructor<T> constructor = clazz.getConstructor(paramTypes);
        return constructor.newInstance(args);
    }

    /**
     * 获取静态字段的值（支持 private 静态字段）。
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     private static int count = 10;
     * }
     *
     * int count = (Integer) ReflectUtils.getStaticFieldValue(Sample.class, "count");
     * }</pre>
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return 静态字段值
     * @throws Exception 字段不存在或无访问权限时抛出
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);
        return field.get(null);
    }

    /**
     * 设置静态字段的值（支持 private 静态字段）。
     *
     * <pre>{@code
     * // 被反射的目标类
     * class Sample {
     *     private static int count;
     * }
     *
     * ReflectUtils.setStaticFieldValue(Sample.class, "count", 99);
     * }</pre>
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @param value     要写入的字段值
     * @throws Exception 字段不存在、无访问权限或类型不匹配时抛出
     */
    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);
        field.set(null, value);
    }
}