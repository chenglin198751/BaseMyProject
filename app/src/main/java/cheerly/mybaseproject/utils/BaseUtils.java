package cheerly.mybaseproject.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.base.BaseApp;
import cheerly.mybaseproject.bean.ApkItem;
import cheerly.mybaseproject.widget.ToastUtils;

public class BaseUtils {
    private static Handler mHandler;
    private static String mStrImei = null;
    private static String mVerCode = null;
    private static String mVerName = null;

    /**
     * 判断手机是否联网
     */
    public static boolean hasNet() {
        ConnectivityManager manager = (ConnectivityManager) BaseApp.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static String getString(int id) {
        return BaseApp.getApp().getResources().getString(id);
    }

    /**
     * 将dip转化为px *
     */
    public static int dip2px(float dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, BaseApp.getApp().getResources().getDisplayMetrics());
    }

    /**
     * 判断某个表是否存在
     */
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean exist = false;
        String sql = "select name from sqlite_master where name = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{tableName});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 将java的13位时间长度转换成10位的PHP长度
     */
    public static String javaTimeToPhpTime(String time) {
        if (time.length() == 13) {
            return time.substring(0, time.length() - 3);
        } else {
            return time;
        }

    }

    /**
     * 将String日期转换为Long型日期
     */
    public static long dateForStringToLong(String strTime, String formatType) {
        Date date = dateForStringToDate(strTime, formatType);
        if (date == null) {
            return 0;
        } else {
            long currentTime = date.getTime();
            return currentTime;
        }
    }

    /**
     * 将Long日期转换为String型日期
     */
    public static String longToString(long time, String formatType) {
        SimpleDateFormat formatter = null;
        formatter = new SimpleDateFormat(formatType);
        String str = formatter.format(time);
        return str;
    }

    /**
     * 将String日期转换为Date日期
     */
    public static Date dateForStringToDate(String strTime, String formatType) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * MD5加密一个字符串
     */
    public static String MD5(String plainText) {
        String str;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        return str;
    }

    /**
     * 显示输入法键盘
     */
    public static void showKeyboard(Context context, EditText edit) {
        edit.setFocusable(true);
        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 关闭输入法键盘
     */
    public static void hideKeyboard(Context context, EditText edit) {
        edit.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    /**
     * 安装一个APK包
     */
    public static void installApk(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 得到APK包的信息
     */
    public static ApkItem getApkInfo(Context context, String path) {
        File file = new File(path);
        if (TextUtils.isEmpty(path) || !file.exists()) {
            return null;
        }

        PackageManager mPackageManager = context.getPackageManager();
        ApkItem apkItem = new ApkItem();
        PackageInfo ApkInfor = mPackageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);

        if (ApkInfor == null) {
            return null;
        }

        apkItem.appSize = file.length();
        apkItem.appVersion = ApkInfor.versionName;
        apkItem.versionCode = ApkInfor.versionCode;

        ApplicationInfo appInfo = ApkInfor.applicationInfo;
        appInfo.sourceDir = path;
        appInfo.publicSourceDir = path;

        apkItem.appName = appInfo.loadLabel(mPackageManager).toString().trim();
        apkItem.image = appInfo.loadIcon(mPackageManager);
        apkItem.packageName = ApkInfor.applicationInfo.packageName;
        return apkItem;
    }

    /**
     * 创建一个全局Handler，可以用来执行一些post任务等
     */
    public static Handler getHandler() {
        if (mHandler == null) {
            synchronized (BaseUtils.class) {
                if (mHandler == null) {
                    mHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mHandler;
    }

    //利用BigDecimal做除法
    public static double divide(double value1, double value2, int scale) {
        if (value2 == 0) {
            return 0;
        }
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }


    /**
     * 格式化1024数据，指定保留几位小数
     */
    public static String format1024(long size, int flag) {
        NumberFormat df = NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(flag);

        StringBuilder builder = new StringBuilder();
        if (size < 0) {
            builder.append(0);
            builder.append("B");
        } else if (size < 1000) {
            builder.append(size);
            builder.append("B");
        } else if (size < 1024000) {
            builder.append(df.format(((double) size) / 1024));
            builder.append("K");
        } else if (size < 1048576000) {
            builder.append(df.format(((double) size) / 1048576));
            builder.append("M");
        } else {
            builder.append(df.format(((double) size) / 1073741824));
            builder.append("G");
        }
        return builder.toString();
    }

    /**
     * 四舍五入保留指定位数的小数
     */
    public static double formatDouble(double d, int scale) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 四舍五入保留指定位数的小数
     */
    public static float formatFloat(float f, int scale) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }


    /**
     * 得到数组中的最小值
     */
    public static long getMinNum(long[] numbers) {
        if (numbers == null || numbers.length <= 0) {
            return 0;
        }

        int i;
        long min, max;
        min = max = numbers[0];
        for (i = 0; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
            }
            if (numbers[i] < min) {
                min = numbers[i];
            }
        }
        return min;
    }

    /**
     * 得到数组中的最大值
     */
    public static long getMaxNum(long[] numbers) {
        if (numbers == null || numbers.length <= 0) {
            return 0;
        }

        int i;
        long min, max;
        min = max = numbers[0];
        for (i = 0; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
            }
            if (numbers[i] < min) {
                min = numbers[i];
            }
        }
        return max;
    }

    /**
     * 只能输入数字。字母和汉字
     */
    public static String LetterAndChinese(String text) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (!((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z') || (letter >= '0' && letter <= '9') || letter > 128)) {
                str.append(letter + "");
            }
        }
        return str.toString();
    }

    /**
     * 得到自身的versionCode
     */
    public static String getVerCode() {
        if (TextUtils.isEmpty(mVerCode)) {
            try {
                mVerCode = BaseApp.getApp().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + "";
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mVerCode;
    }

    /**
     * 得到自身的versionName
     */
    public static String getVerName() {
        if (TextUtils.isEmpty(mVerName)) {
            try {
                mVerName = BaseApp.getApp().getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "";
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mVerName;
    }

    /**
     * 得到设备的串号
     */
    public static String getDeviceId() {
        if (TextUtils.isEmpty(mStrImei)) {
            try {
                if (ActivityCompat.checkSelfPermission(BaseApp.getApp(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) BaseApp.getApp().getSystemService(Context.TELEPHONY_SERVICE);
                    mStrImei = telephonyManager.getDeviceId();
                }
                if (TextUtils.isEmpty(mStrImei)) {
                    mStrImei = Settings.Secure.getString(BaseApp.getApp().getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStrImei;
    }

    public static String getChannel() {
        return "";
    }

    public static String getPackageName() {
        return BaseApp.getApp().getPackageName();
    }

    /**
     * 判断当前线程是不是UI线程
     */
    public static boolean isUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 将String 编码为base64
     */
    public static String toBase64(String text) {
        if (!TextUtils.isEmpty(text)) {
            return Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
        } else {
            return "";
        }
    }

    /**
     * 将String 解码为base64
     */
    public static String fromBase64(String strBase64) {
        if (!TextUtils.isEmpty(strBase64)) {
            return new String(Base64.decode(strBase64.getBytes(), Base64.DEFAULT));
        } else {
            return "";
        }
    }

    /**
     * 判断用户是否开启了应用通知栏，如果开启返回true ，禁用返回false
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isNotificationEnabled(Context context) {
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        try {
            Class appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据包名打开别的应用
     */
    public static void startApp(BaseActivity context, String packageName) {
        try {
            if (isInstalledApp(context, packageName)) {
                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                context.startActivity(LaunchIntent);
            } else {
                ToastUtils.show("你手机没安装此应用");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            ToastUtils.show("你手机没安装此应用");
        }
    }

    /**
     * 是否安装了此应用
     */
    public static boolean isInstalledApp(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * java crc32 运算
     */
    public static long crc32(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        return crc32.getValue();
    }

    /**
     * 获取指定域名的ip地址
     */
    public static String getHostIP(final String serverHost) {
        final String[] res = new String[1];
        res[0] = "";
        final ThreadSync sync = new ThreadSync();
        try {
            sync.pause();
            final Thread tr = new Thread(new Runnable() {
                @Override
                public void run() {
                    String hostIP = "";
                    // 系统函数方式
                    try {
                        java.net.InetAddress addr = java.net.InetAddress.getByName(serverHost);
                        if (addr != null) {
                            hostIP = addr.getHostAddress();
                        }
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }

                    if (!TextUtils.isEmpty(hostIP)) {
                        res[0] = hostIP;
                        sync.resume();
                        return;
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    // ping 命令方式
                    try {
                        Process p = Runtime.getRuntime().exec("/system/bin/ping -c " + 1 + " " + serverHost);
                        p.waitFor();
                        BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String str = "";
                        while ((str = buf.readLine()) != null) {
                            int startIndex = str.indexOf("(");
                            int endIndex = str.indexOf(")");
                            if ((startIndex >= 0) && (endIndex >= 0)) {
                                hostIP = str.substring(startIndex + 1, endIndex);
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    res[0] = hostIP;
                    sync.resume();
                }
            });
            tr.start();

            if (sync.isPaused()) {
                // 等15秒钟
                sync.callWait(15000);
            }
            tr.interrupt();
        } catch (Throwable tr) {
            tr.printStackTrace();
        } finally {
            if (sync != null) {
                sync.exit();
            }
        }

        return res[0];
    }

    /**
     * 执行adb shell 命令来滑动屏幕
     */
    public static void exec() {
        try {
            Runtime.getRuntime().exec("input swipe 400 1000 400 100 4000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
