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
    public final static Gson gson = GsonFactory.getSingletonGson();

    public static class Toggle {
        //app是否展示黑白模式
        public static boolean isGrayscale = false;
    }
}
