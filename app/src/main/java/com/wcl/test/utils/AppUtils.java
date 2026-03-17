package com.wcl.test.utils;

import android.app.Activity;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AppUtils {

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
