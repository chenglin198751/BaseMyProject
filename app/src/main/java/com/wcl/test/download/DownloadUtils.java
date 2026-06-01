package com.wcl.test.download;

import android.os.Environment;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppUtils;

import java.io.File;

/**
 * 下载工具类（内部使用，不对外）
 */
class DownloadUtils {
    private static final String TAG = "DownloadUtils";

    // 简单 URL 校验
    public static boolean isInvalidUrl(String url) {
        boolean valid = url != null && (url.startsWith("http://") || url.startsWith("https://"));
        return !valid;
    }

    // UI 线程执行
    public static void runOnUiThread(Runnable r) {
        AppUtils.getUiHandler().post(r);
    }

    // 替换文件（下载完成后覆盖）
    public static void replaceFile(File src, File dst) {
        if (dst.exists()) {
            dst.delete();
        }
        if (!src.renameTo(dst)) {
            String err = src + " renameTo " + dst + " failed!";
            AppLogUtils.e(TAG, err);
        }
    }

    public static String getTaskId(String url) {
        return AppUtils.md5(url);
    }

    // 根据url获取文件下载路径
    public static String getDownloadPath(String url) {
        File dir = BaseApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(AppUtils.FileUtils.getAppStoragePath());
        }
        return new File(dir, AppUtils.md5(url) + getSuffix(url)).getAbsolutePath();
    }

    public static double roundProgress(long downloaded, long total) {
        return Math.round(downloaded * 10000.0 / total) / 100.0;
    }

    private static String getSuffix(String url) {
        int i = url.lastIndexOf(".");
        return i > 0 ? url.substring(i) : "";
    }
}
