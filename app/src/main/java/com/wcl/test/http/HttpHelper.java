package com.wcl.test.http;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.utils.AppFileUtils;
import com.wcl.test.utils.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

class HttpHelper {

    // 如果是有效url则返回true
    static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"));
    }

    static void postToUi(Runnable r) {
        AppUtils.getUiHandler().post(r);
    }

    static void addCommonParams(Map<String, Object> params) {
        params.put("deviceId", DeviceUtils.getDeviceId());
        params.put("product", Build.MODEL);
        params.put("brand", Build.BRAND);
        params.put("os_int", Build.VERSION.SDK_INT);
        params.put("os_release", Build.VERSION.RELEASE);
        params.put("appCode", AppUtils.getVersionCode());
        params.put("appName", AppUtils.getVersionName());
        params.put("channel", AppUtils.getChannel());
        params.put("pkg", AppUtils.getPackageName());
        params.put("os", "android");
    }

    static FormBody buildFormBody(Map<String, Object> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            builder.add(e.getKey(), String.valueOf(e.getValue()));
        }
        return builder.build();
    }

    static String buildGetUrl(String url, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(url.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (!first) sb.append("&");
            sb.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    // ======================= Download =======================

    // 获取文件下载路径
    static String getDownloadPath(String url) {
        File dir = BaseApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(AppFileUtils.getAppFilesPath());
        }
        return new File(dir, AppUtils.md5(url).toLowerCase() + getSuffix(url)).getAbsolutePath();
    }

    // 从url获取文件长度
    static long fetchContentLength(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = HttpUtils.CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().contentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static void replaceFile(File src, File dest) throws Exception {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
        src.delete();
    }

    private static String getSuffix(String url) {
        int i = url.lastIndexOf(".");
        return i > 0 ? url.substring(i) : "";
    }

    /**
     * 去除字符串开头的 UTF-8 BOM 字符
     */
    static String removeUtf8Bom(String input) {
        if (TextUtils.isEmpty(input)) return input;
        return input.charAt(0) == '\ufeff' ? input.substring(1) : input;
    }
}
