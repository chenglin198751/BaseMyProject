package cheerly.mybaseproject.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class LogUtils {
    private static boolean isDebug = false;

    static {
        String sdcardPath = getSDCardPath();
        if (!TextUtils.isEmpty(sdcardPath)) {
            String debugFilePath = sdcardPath + File.separator + "debug_file_log";
            File file = new File(debugFilePath);
            if (file.exists()) {
                isDebug = true;
            }
        }
    }

    public static boolean isDebug() {
        return isDebug;
    }

    private static String getSDCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        if (sdDir != null) {
            return sdDir.getAbsolutePath();
        }
        return null;
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
