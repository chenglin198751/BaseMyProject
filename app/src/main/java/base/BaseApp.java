package base;

import android.app.Application;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.multidex.MultiDex;

import com.tencent.smtt.sdk.QbSdk;

import helper.AppHelper;

public class BaseApp extends Application {
    private static BaseApp application = null;

    public static BaseApp getApp() {
        return application;
    }

    @Override
    @CallSuper
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        AppHelper.initPicasso();

        //只在应用主进程执行
        if (AppHelper.isAppMainProcess()) {
            QbSdk.initX5Environment(getApplicationContext(), null);
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
