package com.wcl.test.utils;

import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.google.gson.Gson;
import com.hjq.gson.factory.GsonFactory;

import com.wcl.test.base.BaseApp;

/**
 * 公共常量类
 */
public class AppConstants {
    public static int screenWidth = getScreenSize()[0]; //屏幕宽度
    public static int screenHeight = getScreenSize()[1]; //屏幕高度
    public static int statusBarHeight = 0; //状态栏高度
    public static int navBarHeight = 0; //底部虚拟导航栏高度
    public final static Gson gson = GsonFactory.getSingletonGson();

    public static class Toggle {
        //app是否展示黑白模式
        public static boolean isGrayscale = false;
    }

    private static int[] getScreenSize() {
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
