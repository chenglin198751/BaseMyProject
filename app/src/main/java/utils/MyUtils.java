package utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import base.MyApplication;
import bean.ApkItem;

public class MyUtils {
    private static Handler mHandler;

    /**
     * 判断手机是否联网
     */
    public static boolean isNet(Context context) {
        boolean flag = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int leng = 0;
        NetworkInfo[] arrayOfNetworkInfo = manager.getAllNetworkInfo();
        if (arrayOfNetworkInfo != null) {
            leng = arrayOfNetworkInfo.length;
        }
        for (int i = 0; i < leng; i++) {
            if (arrayOfNetworkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 将dip转化为px *
     */
    public static float dip2px(Context context, float dipValue) {
        float value = 0;
        value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
        return value;
    }

    /**
     * 判断某个表是否存在
     */
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean exist = false;
        Cursor cursor = db.rawQuery("select name from sqlite_master where name = ?", new String[]{tableName});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            cursor.close();
        }
        return exist;
    }

    /**
     * 将格林时间转换为标准DATE时间（比如新浪微博返回的那个格林时间）
     */
    public static Date convertTime(String GMT_time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date myDate = null;
        try {
            myDate = sdf.parse(GMT_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return myDate;
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
     * 从view 得到图片
     */
    public static Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
    }

    /**
     * MD5加密一个字符串
     */
    public final static String MD5(String s) {
        final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * MD5加密一个字符串
     */
    public final static String MD5Lower(String s) {
        final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 显示输入法键盘
     */
    public static void showInput(Context context, EditText edit) {
        edit.setFocusable(true);
        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 关闭输入法键盘
     */
    public static void hideInput(Context context, EditText edit) {
        edit.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    /**
     * 四舍五入
     */
    public final static String siSheWuRu(String num) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#");
        return df.format(Float.parseFloat(num) / 100f);
    }

    /**
     * 格式化数据，指定保留几位小数
     */
    public static String formatSize(long size, int flag) {
        NumberFormat df = NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(flag);

        StringBuffer buffer = new StringBuffer();
        if (size < 0) {
            buffer.append(0);
            buffer.append("B");
        } else if (size < 1000) {
            buffer.append(size);
            buffer.append("B");
        } else if (size < 1024000) {
            buffer.append(df.format(((double) size) / 1024));
            buffer.append("K");
        } else if (size < 1048576000) {
            buffer.append(df.format(((double) size) / 1048576));
            buffer.append("M");
        } else {
            buffer.append(df.format(((double) size) / 1073741824));
            buffer.append("G");
        }
        return buffer.toString();
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
    public static ApkItem getApkInfor(Context context, String path) {
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
            synchronized (MyUtils.class) {
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
     * 四舍五入保留指定位数的小数
     */
    public static double formatDouble(double d, int scale) {
        BigDecimal b = new BigDecimal(d);
        double myNum3 = b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        return myNum3;
    }


    /**
     * 根据关键字来把字符串标记为不同颜色
     *
     * @param myStr   传入的字符串
     * @param keyword 要被标记的关键字
     * @param color   要被标记的颜色
     * @return SpannableString
     */
    public static SpannableString getSpanByKeyword(final String myStr, final String keyword, final int color) {
        int redColor = MyApplication.getApplication().getResources().getColor(color);
        SpannableString textSpan = new SpannableString(myStr);
        if (TextUtils.isEmpty(keyword)) {
            return textSpan;
        }

        int index = myStr.indexOf(keyword);
        while (index >= 0) {
            textSpan.setSpan(new ForegroundColorSpan(redColor), index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            int nextIndex = index + keyword.length();

            if (nextIndex <= myStr.length()) {
                index = myStr.indexOf(keyword, nextIndex);
            }

        }
        return textSpan;
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
}
