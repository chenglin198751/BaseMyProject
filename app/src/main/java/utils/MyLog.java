package utils;

import android.util.Log;

public class MyLog {

    private static boolean isDebug = true;

    public static boolean isDebug() {
        return isDebug;
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug)
            Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug)
            Log.e(tag, msg, tr);
    }
}
