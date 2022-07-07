package com.wcl.test.base;

import android.app.Application;
import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.tencent.smtt.sdk.QbSdk;
import com.wcl.test.helper.AppHelper;

public class BaseApp extends Application {
    private static BaseApp application = null;

    public static BaseApp getApp() {
        return application;
    }

    @Override
    @CallSuper
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        application = this;

        //只在应用主进程执行
        if (AppHelper.isAppMainProcess()) {
            QbSdk.initX5Environment(getApplicationContext(), null);
        }

        //监测app位于前台还是后台
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onStop(owner);
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onStart(owner);
            }
        });
    }
}
