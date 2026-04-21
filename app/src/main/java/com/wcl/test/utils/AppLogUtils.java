package com.wcl.test.utils;

import android.util.Log;

import com.wcl.test.EnvToggle;

public class AppLogUtils {

    public static void d(String tag, String msg) {
        if (EnvToggle.isLog()) {
            print(tag, msg, Log.DEBUG);
        }
    }

    public static void e(String tag, String msg) {
        if (EnvToggle.isLog()) {
            print(tag, msg, Log.ERROR);
        }
    }

    public static void i(String tag, String msg) {
        if (EnvToggle.isLog()) {
            print(tag, msg, Log.INFO);
        }
    }

    public static void v(String tag, String msg) {
        if (EnvToggle.isLog()) {
            print(tag, msg, Log.VERBOSE);
        }
    }

    public static void w(String tag, String msg) {
        if (EnvToggle.isLog()) {
            print(tag, msg, Log.WARN);
        }
    }


    /**
     * Android内核源码在logger.h中定义的最大字符长度LOGGER_ENTRY_MAX_LEN为4*1024
     * 如果日志超过4K就截断打印
     */
    private static final int SEGMENT_SIZE = 4000;
    private static final String SEG_START = "-----------log长度超过了" + SEGMENT_SIZE + "，分段打印start-----------";
    private static final String SEG_END   = "-----------log长度超过了" + SEGMENT_SIZE + "，分段打印end-----------";

    private static void print(String tag, String msg, int level) {
        final int length = msg.length();

        if (length <= SEGMENT_SIZE) {
            print2(tag, msg, level);
            return;
        }

        print2(tag, SEG_START, level);
        for (int i = 0; i < length; i += SEGMENT_SIZE) {
            print2(tag, msg.substring(i, Math.min(i + SEGMENT_SIZE, length)), level);
        }
        print2(tag, SEG_END, level);
    }

    private static void print2(String tag, String log2, int level) {
        switch (level) {
            case Log.DEBUG:
                Log.d(tag, log2);
                break;
            case Log.ERROR:
                Log.e(tag, log2);
                break;
            case Log.INFO:
                Log.i(tag, log2);
                break;
            case Log.VERBOSE:
                Log.v(tag, log2);
                break;
            case Log.WARN:
                Log.w(tag, log2);
                break;
        }
    }
}
