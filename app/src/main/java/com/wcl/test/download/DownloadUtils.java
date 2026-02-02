package com.wcl.test.download;

import android.os.Handler;
import android.os.Looper;

import com.wcl.test.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

public class DownloadUtils {

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

    // 获取下载文件路径（可自定义路径规则）
    public static String getDownloadPath(String url, String fileName) {
        File dir = new File(FileUtils.getAppFilesPath());
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, fileName).getAbsolutePath();
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
}
