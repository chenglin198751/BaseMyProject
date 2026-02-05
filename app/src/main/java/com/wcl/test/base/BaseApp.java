package com.wcl.test.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

public class BaseApp extends Application {
    private static volatile BaseApp sApp;

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
        AppHelper.initAppForegroundObserver();
        AppHelper.initActivityLifecycleObserver(this);
        AppHelper.initGsonFactory();
    }


    /**
     * 获取当前最顶层 Activity（可能为 null）
     */
    @Nullable
    public static Activity getTopActivity() {
        return AppHelper.getTopActivity();
    }

    /**
     * App 是否处于前台
     */
    public static boolean isAppInForeground() {
        return AppHelper.isAppInForeground();
    }

}
