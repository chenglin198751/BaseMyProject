package com.wcl.test.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.AppFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

class DownloadUtils {

    // 生成 MD5，用于 taskId
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(input.hashCode());
        }
    }

    // 简单 URL 校验
    public static boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    // UI 线程执行
    public static void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
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
        return DownloadUtils.md5(url);
    }

    // 根据url获取文件下载路径
    public static String getDownloadPath(String url) {
        File dir = BaseApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(AppFileUtils.getAppFilesPath());
        }
        return new File(dir, AppBaseUtils.md5(url).toLowerCase() + getSuffix(url)).getAbsolutePath();
    }

    private static String getSuffix(String url) {
        int i = url.lastIndexOf(".");
        return i > 0 ? url.substring(i) : "";
    }
}
