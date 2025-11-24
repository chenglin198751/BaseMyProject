package com.qihoo.utils;

import android.content.Context;

import com.wcl.test.base.MainApp;

public class ContextUtils {
    public static Context getHostAppContext(){
        return MainApp.getApp();
    }
}
