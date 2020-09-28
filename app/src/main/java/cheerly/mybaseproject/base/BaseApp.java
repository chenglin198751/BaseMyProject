package cheerly.mybaseproject.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.CallSuper;
import androidx.multidex.MultiDex;

import com.tencent.smtt.sdk.QbSdk;

import cheerly.mybaseproject.helper.AppHelper;

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
    @CallSuper
    public void onCreate() {
        super.onCreate();
        application = this;

//        // 置入一个不设防的VmPolicy，用于解决某种情况下file://调用失败的问题
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//            StrictMode.setVmPolicy(builder.build());
//        }

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
    public Activity getTopActivity() {
        return mTopAct;
    }
}
