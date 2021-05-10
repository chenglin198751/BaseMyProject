package cheerly.mybaseproject.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cheerly.mybaseproject.BuildConfig;
import cheerly.mybaseproject.base.BaseApp;

public class LogUtils {
    private static boolean isDebug = false;

    static {
        File file = new File(getDebugFilePath());
        isDebug = file.exists();
    }

    public static boolean isDebug() {
        return isDebug || BuildConfig.DEBUG;
    }


    public static String getDebugFilePath() {
        File file = new File(SDCardUtils.getDataPath(SDCardUtils.TYPE_FILE));
        return file.getParent() + File.separator + "debug.log";
    }

    private static void writeLog(String tag, String msg) {
        if (!BaseUtils.isDebuggable(BaseApp.getApp())) {
            System.out.println(tag + ":" + msg);
        }
        String str = getDate() + "<" + tag + ">" + msg;
        writeFile(new File(getDebugFilePath()), str);
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

    /**
     * 把字符串写入文件
     */
    public static void writeFile(File file, String value) {
        if (!file.exists()) {
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(value);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件
     */
    public static String readFile(File file) {
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

    public static String getSdcardFilePath() {
        boolean sdCardExist = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //为真则SD卡已装入，
            sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        }

        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            //查找SD卡根路径
            return sdDir.getPath();
        }

        return null;
    }
}
