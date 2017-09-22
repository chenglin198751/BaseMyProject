package base;

import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

import helper.ApplicationHelper;

public class MyApplication extends Application {
    private static MyApplication application = null;

    public static MyApplication getApp() {
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
