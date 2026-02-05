package com.wcl.test.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.EmptyActivityLifecycleCallbacks;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonToken;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.gson.factory.ParseExceptionCallback;
import com.tencent.mmkv.MMKV;
import com.wcl.test.utils.AppLogUtils;

import java.lang.ref.WeakReference;

public class BaseApp extends Application {
    private static volatile BaseApp sApp;
    private static volatile WeakReference<Activity> sTopActivityRef;
    private static volatile boolean sIsForeground = false;

    public static BaseApp getApp() {
        return sApp;
    }

    @Override
    @CallSuper
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApp = this;
    }

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        sApp = this;

        MMKV.initialize(this);
        initAppForegroundObserver();
        initActivityLifecycleObserver();
        initGsonFactory();
    }

    // app前后台监听
    private void initAppForegroundObserver() {
        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(new DefaultLifecycleObserver() {

                    @Override
                    public void onStart(@NonNull LifecycleOwner owner) {
                        sIsForeground = true;
                    }

                    @Override
                    public void onStop(@NonNull LifecycleOwner owner) {
                        sIsForeground = false;
                    }
                });
    }

    // Activity 监听
    private void initActivityLifecycleObserver() {
        registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                sTopActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                if (sTopActivityRef != null && sTopActivityRef.get() == activity) {
                    sTopActivityRef.clear();
                    sTopActivityRef = null;
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (sTopActivityRef != null && sTopActivityRef.get() == activity) {
                    sTopActivityRef.clear();
                    sTopActivityRef = null;
                }
            }
        });
    }

    /**
     * 获取当前最顶层 Activity（可能为 null）
     */
    @Nullable
    public static Activity getTopActivity() {
        if (sTopActivityRef == null) {
            return null;
        }
        Activity activity = sTopActivityRef.get();
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        return activity;
    }

    /**
     * App 是否处于前台
     */
    public static boolean isAppInForeground() {
        return sIsForeground;
    }

    // 设置 Json 解析容错监听
    private void initGsonFactory() {
        GsonFactory.setParseExceptionCallback(new ParseExceptionCallback() {
            @Override
            public void onParseObjectException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseObjectException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }

            @Override
            public void onParseListItemException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseListItemException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }

            @Override
            public void onParseMapItemException(TypeToken<?> typeToken, String fieldName, String mapItemKey, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseMapItemException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }
        });
    }
}
