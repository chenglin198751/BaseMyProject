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
        initAppForegroundObserver();
        initActivityLifecycleObserver();
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
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (sTopActivityRef != null && sTopActivityRef.get() == activity) {
                    sTopActivityRef.clear();
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
}
