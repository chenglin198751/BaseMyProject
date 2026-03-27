package com.wcl.test.download;

import android.os.Environment;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

class DownloadUtils {

    // 简单 URL 校验
    public static boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    // UI 线程执行
    public static void runOnUiThread(Runnable r) {
        AppUtils.getUiHandler().post(r);
    }

    // 替换文件（下载完成后覆盖）
    public static void replaceFile(File src, File dst) throws Exception {
        if (dst.exists()) dst.delete();
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        src.delete();
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
        return new File(dir, AppUtils.md5(url).toLowerCase() + getSuffix(url)).getAbsolutePath();
    }

    private static String getSuffix(String url) {
        int i = url.lastIndexOf(".");
        return i > 0 ? url.substring(i) : "";
    }
}
