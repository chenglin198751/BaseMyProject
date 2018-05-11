package base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.smtt.sdk.QbSdk;

import helper.AppHelper;

public class MyApp extends Application {
    private static MyApp application = null;

    public static MyApp getApp() {
        return application;
    }

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
