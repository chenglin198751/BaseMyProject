package cheerly.mybaseproject.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
    private static boolean isDebug = false;

    static {
        File file = new File(getDebugFilePath());
        isDebug = file.exists();
    }

    public static boolean isDebug() {
        return isDebug;
    }


    public static String getDebugFilePath() {
        File file = new File(SDCardUtils.getDataPath(SDCardUtils.TYPE_FILE));
        return file.getParent() + File.separator + "debug.log";
    }

    private static void writeLog(String tag, String msg) {
        String str = getDate() + "<" + tag + ">" + msg;
        writeFile(getDebugFilePath(), str);
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug) {
            writeLog(tag, msg);
            Log.e(tag, msg, tr);
        }
    }

    public static void writeFile(String filePath, String value) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(value + "\r\n\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String readString = "";
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                currentLine += '\n';
                readString += currentLine;
            }
            return readString;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getDate() {
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
        return (sf.format(new Date()));
    }
}
