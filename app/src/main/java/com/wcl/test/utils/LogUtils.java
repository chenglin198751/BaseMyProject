package com.wcl.test.utils;

import android.os.Environment;
import android.util.Log;

import com.wcl.test.EnvToggle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    public static void d(String tag, String msg) {
        if (EnvToggle.isLog()) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (EnvToggle.isLog()) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (EnvToggle.isLog()) {
            Log.i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (EnvToggle.isLog()) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (EnvToggle.isLog()) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (EnvToggle.isLog()) {
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (EnvToggle.isLog()) {
            Log.e(tag, msg, tr);
        }
    }
}
