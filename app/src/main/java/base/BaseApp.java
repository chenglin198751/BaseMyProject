package base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.multidex.MultiDex;

import com.tencent.smtt.sdk.QbSdk;

import helper.AppHelper;

public class BaseApp extends Application {
    private static BaseApp application = null;
    private Activity mTopAct;

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


        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                mTopAct = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
        });
    }


    /**
     * 得到栈顶Activity(有Activity在另一个进程打开就检测不到了)
     */
    public Activity getmTopActivity() {
        return mTopAct;
    }
}
