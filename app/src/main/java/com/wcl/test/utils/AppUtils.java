package com.wcl.test.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.wcl.test.BuildConfig;
import com.wcl.test.base.BaseApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {

    public static class FileUtils {

        private static final String TAG = "FileUtils";

        /**
         * 获取应用私有的可写文件目录路径（不需要存储权限）
         * 1、优先使用外部存储的 App 私有目录：
         * /storage/emulated/0/Android/data/{packageName}/files
         * 2、当外部存储不可用时，回退到内部存储：
         * /data/data/{packageName}/files
         */
        public static String getAppFilesPath() {
            Context context = BaseApp.getApp();
            File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                dir = context.getFilesDir();
            }
            return dir.getAbsolutePath();
        }

        /**
         * 递归计算文件或文件夹的总大小
         *
         * @param folder 文件或目录
         * @return 字节数，异常时返回 0
         */
        public static long getFolderSize(File folder) {
            if (folder == null || !folder.exists()) {
                return 0;
            }

            if (folder.isFile()) {
                return folder.length();
            }

            long size = 0;
            File[] files = folder.listFiles();
            if (files == null) {
                return 0;
            }

            for (File file : files) {
                size += getFolderSize(file);
            }
            return size;
        }

        /**
         * 删除文件或目录（递归）
         * - 如果是文件，直接删除
         * - 如果是目录，先删除子文件再删除目录本身
         *
         * @param path 文件或目录路径
         */
        public static void delete(String path) {
            if (TextUtils.isEmpty(path)) {
                return;
            }
            deleteInternal(new File(path));
        }

        private static void deleteInternal(File file) {
            if (!file.exists()) {
                return;
            }

            if (file.isFile()) {
                if (!file.delete()) {
                    Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
                }
                return;
            }

            File[] files = file.listFiles();
            if (files != null) {
                for (File sub : files) {
                    deleteInternal(sub);
                }
            }

            if (!file.delete()) {
                Log.e(TAG, "Failed to delete directory: " + file.getAbsolutePath());
            }
        }

        /**
         * 追加写入文本到文件（UTF-8）
         * - 文件不存在会自动创建
         * - 父目录不存在会自动创建
         * - 以追加方式写入
         *
         * @param filePath 文件路径
         * @param text     要写入的内容
         */
        public static void writeFile(String filePath, String text) {
            if (TextUtils.isEmpty(filePath) || text == null) {
                return;
            }

            File file = new File(filePath);
            ensureParentDir(file);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(text);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write file: " + filePath, e);
            }
        }

        /**
         * 以 UTF-8 编码读取整个文件内容为字符串
         *
         * @param filePath 文件路径
         * @return 文件内容，失败返回 null
         */
        private static String readFileString(String filePath) {
            if (TextUtils.isEmpty(filePath)) {
                return null;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Log.e(TAG, "Failed to read file: " + filePath, e);
            }
            return null;
        }

        /**
         * 按行读取文件（UTF-8）
         *
         * @param filePath 文件路径
         * @return 行列表，失败返回空列表
         */
        public static List<String> readFileLines(String filePath) {
            List<String> lines = new ArrayList<>();
            if (TextUtils.isEmpty(filePath)) {
                return lines;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return lines;
            }

            try {
                lines.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                Log.e(TAG, "Failed to read file lines: " + filePath, e);
            }
            return lines;
        }

        /**
         * 覆盖写入多行文本到文件（UTF-8）
         *
         * <p>
         * - 原文件内容会被清空
         * - 每行自动追加系统换行符
         *
         * @param filePath 文件路径
         * @param lines    文本行集合
         */
        public static void writeFileLines(String filePath, Iterable<String> lines) {
            if (TextUtils.isEmpty(filePath) || lines == null) {
                return;
            }

            File file = new File(filePath);
            ensureParentDir(file);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to write file lines: " + filePath, e);
            }
        }

        /**
         * 递归复制目录
         *
         * @param fromDir 源目录
         * @param toDir   目标目录
         */
        public static void copyDirectory(File fromDir, File toDir) {
            if (fromDir == null || toDir == null || !fromDir.isDirectory()) {
                return;
            }

            if (!toDir.exists() && !toDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + toDir.getAbsolutePath());
                return;
            }

            File[] files = fromDir.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                File target = new File(toDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, target);
                } else {
                    copyFile(file, target);
                }
            }
        }

        /**
         * 复制单个文件
         *
         * @param source 源文件
         * @param dest   目标文件
         */
        public static void copyFile(File source, File dest) {
            if (source == null || dest == null || !source.exists()) {
                return;
            }

            ensureParentDir(dest);

            try {
                Files.copy(source.toPath(), dest.toPath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy file from " + source + " to " + dest, e);
            }
        }

        // ---------------- Internal ----------------

        /**
         * 确保父目录存在
         */
        private static void ensureParentDir(File file) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        }
    }

    private static class AppProcess {

        private static volatile String sCurrentProcessName;

        private AppProcess() {
        }

        /**
         * 判断是不是 app 主进程（有些组件只应在主进程初始化）
         */
        static boolean isAppMainProcess(Context context) {
            try {
                if (context == null) {
                    return true;
                }
                String packageName = context.getApplicationContext().getPackageName();
                String current = getCurrentProcessName(context);
                if (current == null) {
                    return true;
                }
                return packageName.equalsIgnoreCase(current);
            } catch (Throwable t) {
                try {
                    AppLogUtils.e("AppProcess", "isAppMainProcess error:" + t);
                } catch (Throwable ignored) {
                }
                return true;
            }
        }

        /**
         * 获取当前进程名
         */
        static String getCurrentProcessName(Context context) {
            if (!TextUtils.isEmpty(sCurrentProcessName)) {
                return sCurrentProcessName;
            }

            synchronized (AppProcess.class) {
                if (!TextUtils.isEmpty(sCurrentProcessName)) {
                    return sCurrentProcessName;
                }

                String currentProcessName;

                // 1.Application API (Android P+)
                currentProcessName = getCurrentProcessNameByApplication();
                AppLogUtils.v("AppProcess", "currentProcess:" + currentProcessName);
                if (!TextUtils.isEmpty(currentProcessName)) {
                    sCurrentProcessName = currentProcessName;
                    return sCurrentProcessName;
                }

                // 2.反射 ActivityThread
                currentProcessName = getCurrentProcessNameByActivityThread();
                AppLogUtils.v("AppProcess", "getCurrentProcessNameByActivityThread = " + currentProcessName);
                if (!TextUtils.isEmpty(currentProcessName)) {
                    sCurrentProcessName = currentProcessName;
                    return sCurrentProcessName;
                }

                // 3.ActivityManager（IPC）
                currentProcessName = getCurrentProcessNameByActivityManager(context);
                AppLogUtils.v("AppProcess", "getCurrentProcessNameByActivityManager = " + currentProcessName);
                if (!TextUtils.isEmpty(currentProcessName)) {
                    sCurrentProcessName = currentProcessName;
                    return sCurrentProcessName;
                }

                return null;
            }
        }

        /**
         * 通过 Application 的 API 获取进程名（Android P 及以上）
         */
        private static String getCurrentProcessNameByApplication() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    return Application.getProcessName();
                } catch (Throwable t) {
                    try {
                        AppLogUtils.e("AppProcess", "getCurrentProcessNameByApplication error:" + t);
                    } catch (Throwable ignored) {
                    }
                }
            }
            return null;
        }

        /**
         * 通过反射 android.app.ActivityThread.currentProcessName() 获取进程名，尽量避免 IPC
         */
        private static String getCurrentProcessNameByActivityThread() {
            String processName = null;
            try {
                Method declaredMethod = Class.forName("android.app.ActivityThread", false,
                        Application.class.getClassLoader()).getDeclaredMethod("currentProcessName");
                declaredMethod.setAccessible(true);
                Object result = declaredMethod.invoke(null);
                if (result instanceof String) {
                    processName = (String) result;
                }
            } catch (Throwable t) {
                try {
                    AppLogUtils.v("AppProcess", "ActivityThread reflection failed:" + t);
                } catch (Throwable ignored) {
                }
            }
            return processName;
        }

        /**
         * 通过 ActivityManager 获取当前进程名（需要 IPC）
         */
        private static String getCurrentProcessNameByActivityManager(Context context) {
            if (context == null) {
                return null;
            }
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                return null;
            }
            List<ActivityManager.RunningAppProcessInfo> runningAppList = am.getRunningAppProcesses();
            if (runningAppList == null) {
                return null;
            }
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppList) {
                if (processInfo != null && processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
            return null;
        }
    }

    /**
     * 判断手机是否联网
     */
    public static boolean isNetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApp.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    public static String getString(int id) {
        return BaseApp.getApp().getResources().getString(id);
    }

    /**
     * 将dip转化为px *
     */
    public static int dp2px(float dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, BaseApp.getApp().getResources().getDisplayMetrics());
    }

    /**
     * MD5加密一个字符串
     */
    public static String md5(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(32);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
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

    private static final class MHandlerHolder {
        private static final Handler mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 创建一个全局Handler，可以用来执行一些post任务等
     */
    public static Handler getUiHandler() {
        return MHandlerHolder.mHandler;
    }


    /**
     * 判断是不是 app 主进程
     */
    static boolean isAppMainProcess(Context context) {
        return AppProcess.isAppMainProcess(context);
    }

    /**
     * 获取当前进程名
     */
    static String getCurrentProcessName(Context context) {
        return AppProcess.getCurrentProcessName(context);
    }

    /**
     * 四舍五入保留指定位数的小数
     */
    public static double formatDouble(double d, int scale) {
        BigDecimal b = new BigDecimal(Double.toString(d));
        return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 四舍五入保留指定位数的小数
     */
    public static float formatFloat(float f, int scale) {
        BigDecimal b = new BigDecimal(Float.toString(f));
        return b.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }

    /**
     * versionCode
     */
    public static long getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * versionName
     */
    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }


    public static String getChannel() {
        return "";
    }

    public static String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }

    /**
     * 判断当前线程是不是UI线程
     */
    public static boolean isUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 获取当前最顶层 Activity（可能为 null）
     */
    @Nullable
    public static Activity getTopActivity() {
        return BaseApp.getTopActivity();
    }

    /**
     * App 是否处于前台
     */
    public static boolean isAppInForeground() {
        return BaseApp.isAppInForeground();
    }

    /**
     * 从任意 Context 中获取 Activity
     * 解决：Android 从 View 中获取 Activity 时遇到 TintContextWrapper cannot be cast to 的问题
     */
    public static Activity getActivityFromContext(Context context) {
        if (context == null) return null;

        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * 扩展 View 的点击区域
     *
     * @param view   需要扩展的元素，必须有父布局
     * @param expend 需要扩展的尺寸（dp）
     */
    public static void expandTouchArea(final View view, final int expend) {
        if (view == null) return;

        final View parentView = (View) view.getParent();
        if (parentView == null) return;

        final int px = dp2px(expend);

        parentView.post(() -> {
            Rect rect = new Rect();
            view.getHitRect(rect);
            rect.left -= px;
            rect.top -= px;
            rect.right += px;
            rect.bottom += px;
            parentView.setTouchDelegate(new TouchDelegate(rect, view));
        });
    }


    /**
     * 从 assets 目录读取文本文件内容（UTF-8）
     */
    public static String readTextFromAssets(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(BaseApp.getApp().getAssets().open(fileName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (sb.length() == 0) {
            return "";
        }

        // 去掉最后一个换行符
        sb.setLength(sb.length() - 1);

        // 去除 UTF-8 BOM 头（\uFEFF）
        if (sb.length() > 0 && sb.charAt(0) == '\uFEFF') {
            sb.deleteCharAt(0);
        }

        return sb.toString();
    }

    /**
     * 判断Activity是否finish
     */
    public static boolean isActivityDestroyed(Context context) {
        Activity activity = getActivityFromContext(context);
        return activity == null || activity.isDestroyed() || activity.isFinishing();
    }

    /**
     * 重启应用
     */
    public static void restartApp(Context mContext) {
        try {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前应用是否采用“沉浸式全屏（Edge-to-Edge）”模式。
     * <p>
     * 逻辑说明：
     * 1. Android 13 MR1（API 35）及以上系统支持 Edge-to-Edge 界面。
     * 2. 应用的 targetSdkVersion 也需 >= 35 才会启用该特性。
     *
     * @return true 表示应用和系统都支持 Edge-to-Edge UI，false 表示不支持。
     */
    public static boolean isEdgeToEdge() {
        return Build.VERSION.SDK_INT >= 35 && BaseApp.getApp().getApplicationInfo().targetSdkVersion >= 35;
    }

    /**
     * 判断是不是小米设备
     */
    public static boolean isXiaomiDevice() {
        return (Build.BRAND != null && Build.BRAND.toLowerCase().contains("xiaomi") || "xiaomi".equalsIgnoreCase(Build.MANUFACTURER));
    }

    /**
     * 设置View纯圆形
     */
    public static void setViewCircle(View view) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = Math.min(view.getWidth(), view.getHeight());
                outline.setOval(0, 0, size, size);
            }
        });
        view.setClipToOutline(true);
    }

    /**
     * 设置View圆角，单位dp
     */
    public static void setViewRounded(View view, int radiusDp) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = dp2px(radiusDp);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        view.setClipToOutline(true);
    }

    /**
     * 设置Dialog边到边效果
     */
    public static void setDialogEdgeToEdge(Dialog dialog) {
        try {
            Window window = dialog.getWindow();
            if (window == null) {
                return;
            }

            // 1. 配置布局延伸至系统栏（状态栏+导航栏）
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );

            // 2. 允许窗口绘制系统栏背景，设置状态栏和导航栏透明
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            // 3. 允许内容进入刘海（缺口）区域
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
