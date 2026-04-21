package com.wcl.test.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.wcl.test.base.BaseApp;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 专供AppUtils使用，不对外
 */
class InnerAppUtils {
    static volatile String sCurrentProcessName;

    /**
     * 判断是不是 app 主进程（有些组件只应在主进程初始化）
     */
    static boolean isAppMainProcess(Context context) {
        try {
            if (context == null) {
                return true;
            }
            String packageName = context.getApplicationContext().getPackageName();
            String current = getCurrentProcessName(context);
            if (current == null) {
                return true;
            }
            return packageName.equalsIgnoreCase(current);
        } catch (Throwable t) {
            try {
                AppLogUtils.e("AppProcess", "isAppMainProcess error:" + t);
            } catch (Throwable ignored) {
            }
            return true;
        }
    }

    /**
     * 获取当前进程名
     */
    static String getCurrentProcessName(Context context) {
        if (!TextUtils.isEmpty(sCurrentProcessName)) {
            return sCurrentProcessName;
        }

        synchronized (InnerAppUtils.class) {
            if (!TextUtils.isEmpty(sCurrentProcessName)) {
                return sCurrentProcessName;
            }

            String currentProcessName;

            // 1.Application API (Android P+)
            currentProcessName = getCurrentProcessNameByApplication();
            AppLogUtils.v("AppProcess", "currentProcess:" + currentProcessName);
            if (!TextUtils.isEmpty(currentProcessName)) {
                sCurrentProcessName = currentProcessName;
                return sCurrentProcessName;
            }

            // 2.反射 ActivityThread
            currentProcessName = getCurrentProcessNameByActivityThread();
            AppLogUtils.v("AppProcess", "getCurrentProcessNameByActivityThread = " + currentProcessName);
            if (!TextUtils.isEmpty(currentProcessName)) {
                sCurrentProcessName = currentProcessName;
                return sCurrentProcessName;
            }

            // 3.ActivityManager（IPC）
            currentProcessName = getCurrentProcessNameByActivityManager(context);
            AppLogUtils.v("AppProcess", "getCurrentProcessNameByActivityManager = " + currentProcessName);
            if (!TextUtils.isEmpty(currentProcessName)) {
                sCurrentProcessName = currentProcessName;
                return sCurrentProcessName;
            }

            return null;
        }
    }

    /**
     * 通过 Application 的 API 获取进程名（Android P 及以上）
     */
    private static String getCurrentProcessNameByApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return Application.getProcessName();
            } catch (Throwable t) {
                try {
                    AppLogUtils.e("AppProcess", "getCurrentProcessNameByApplication error:" + t);
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /**
     * 通过反射 android.app.ActivityThread.currentProcessName() 获取进程名，尽量避免 IPC
     */
    private static String getCurrentProcessNameByActivityThread() {
        String processName = null;
        try {
            Method declaredMethod = Class.forName("android.app.ActivityThread", false,
                    Application.class.getClassLoader()).getDeclaredMethod("currentProcessName");
            declaredMethod.setAccessible(true);
            Object result = declaredMethod.invoke(null);
            if (result instanceof String) {
                processName = (String) result;
            }
        } catch (Throwable t) {
            try {
                AppLogUtils.v("AppProcess", "ActivityThread reflection failed:" + t);
            } catch (Throwable ignored) {
            }
        }
        return processName;
    }

    /**
     * 通过 ActivityManager 获取当前进程名（需要 IPC）
     */
    private static String getCurrentProcessNameByActivityManager(Context context) {
        if (context == null) {
            return null;
        }
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppList = am.getRunningAppProcesses();
        if (runningAppList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppList) {
            if (processInfo != null && processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }

    public static int[] getScreenSize() {
        int screenWidth;
        int screenHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = BaseApp.getApp().getSystemService(WindowManager.class);
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            screenWidth = metrics.getBounds().width();
            screenHeight = metrics.getBounds().height();
        } else {
            DisplayMetrics displayMetrics = BaseApp.getApp().getResources().getDisplayMetrics();
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }
        return new int[]{screenWidth, screenHeight};
    }
}
