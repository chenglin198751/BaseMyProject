package com.wcl.test.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.google.gson.Gson;
import com.hjq.gson.factory.GsonFactory;

import com.wcl.test.base.BaseApp;

/**
 * 公共常量类
 */
public class AppConstants {
    public static int screenWidth = BaseApp.getApp().getResources().getDisplayMetrics().widthPixels;
    public static int screenHeight = BaseApp.getApp().getResources().getDisplayMetrics().heightPixels;
    public final static Gson gson = GsonFactory.getSingletonGson();

    public static class Toggle {
        //app是否展示黑白模式
        public static boolean isGrayscale = false;
    }

}
