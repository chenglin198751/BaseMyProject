package base;

import android.app.Application;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import helper.ApplicationHelper;
import utils.Constants;

public class MyApplication extends Application {
    private static MyApplication application = null;
    private ApplicationHelper mAppHelper;

    public static MyApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mAppHelper = new ApplicationHelper();

        //只在应用主进程执行
        if (mAppHelper.isAppMainProcess()) {
            getDisplayMetrics();
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void getDisplayMetrics() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Constants.screenWidth = display.getWidth();
        Constants.screenHeight = display.getHeight();
    }

}
