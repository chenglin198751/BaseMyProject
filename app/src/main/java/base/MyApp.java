package base;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

import helper.ApplicationHelper;

public class MyApp extends Application {
    private static MyApp application = null;

    public static MyApp getApp() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        ApplicationHelper.initPicasso();
        ApplicationHelper.initGetDisplayMetrics(this);

        //只在应用主进程执行
        if (ApplicationHelper.isAppMainProcess()) {
            QbSdk.initX5Environment(getApplicationContext(), null);
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
