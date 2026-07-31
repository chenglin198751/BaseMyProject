package com.wcl.test.http;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

class LiteHelper {

    private static final String TAG = "LiteHelper";

    // 如果是无效 url（空或无法解析为 http/https）则返回 true
    static boolean invalidUrl(String url) {
        HttpUrl parsed = url == null ? null : HttpUrl.parse(url);
        return parsed == null || !("http".equals(parsed.scheme()) || "https".equals(parsed.scheme()));
    }

    static void postToUi(Runnable r) {
        AppUtils.getUiHandler().post(r);
    }

    static void notifyDownloadFailure(OkHttpExecutor.DownloadCallback callback, String error) {
        if (callback != null) {
            postToUi(() -> callback.onFinished(false, null, error));
        }
    }

    static void notifyUploadFailure(OkHttpExecutor.UploadCallback callback, String error) {
        if (callback != null) {
            postToUi(() -> callback.onFinished(false, error));
        }
    }

    static void addCommonParams(Map<String, Object> params) {
        params.put("deviceId", AppUtils.getAndroidId());
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
        HttpUrl parsed = HttpUrl.parse(url);
        if (parsed == null) {
            return url;
        }

        HttpUrl.Builder builder = parsed.newBuilder();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            builder.addQueryParameter(e.getKey(), String.valueOf(e.getValue()));
        }
        return builder.build().toString();
    }

    // ======================= Download =======================

    // 获取文件下载路径
    static String getDownloadPath(String url) {
        File dir = BaseApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(BaseApp.getApp().getFilesDir(), Environment.DIRECTORY_DOWNLOADS);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            AppLogUtils.e(TAG, "create download directory failed: " + dir.getAbsolutePath());
        }
        return new File(dir, AppUtils.md5(url).toLowerCase() + getSuffix(url)).getAbsolutePath();
    }

    private static String getSuffix(String url) {
        HttpUrl parsed = HttpUrl.parse(url);
        if (parsed == null) return "";

        String path = parsed.encodedPath();
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = fileName.lastIndexOf('.');
        return dot > 0 && dot < fileName.length() - 1 ? fileName.substring(dot) : "";
    }

    // 从 url 获取文件长度
    static long fetchContentLength(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept-Encoding", "identity")
                .build();
        try (Response response = HttpRequest.CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) return 0;
            long totalLength = response.body().contentLength();
            return Math.max(totalLength, 0);
        } catch (Exception e) {
            AppLogUtils.e(TAG, "fetch content length error: " + e);
            return 0;
        }
    }

    static void postSuccess(OkHttpExecutor.DownloadCallback callback, String path) {
        postToUi(() -> {
            if (callback != null) callback.onFinished(true, path, null);
        });
    }

    static ContentRange parseContentRange(String value) {
        if (value == null || !value.startsWith("bytes ")) return null;
        int dash = value.indexOf('-', 6);
        int slash = value.indexOf('/', dash + 1);
        if (dash < 0 || slash < 0) return null;
        try {
            long start = Long.parseLong(value.substring(6, dash));
            long end = Long.parseLong(value.substring(dash + 1, slash));
            long total = Long.parseLong(value.substring(slash + 1));
            return new ContentRange(start, end, total);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    record ContentRange(long start, long end, long total) {
    }

    static boolean replaceFile(File src, File dest) {
        try {
            Files.move(src.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException atomicMoveError) {
            try {
                Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException moveError) {
                AppLogUtils.e(TAG, "replace file error: " + moveError
                        + ", atomic move error: " + atomicMoveError);
                return false;
            }
        }
    }

    /**
     * 去除字符串开头的 UTF-8 BOM 字符
     */
    static String removeUtf8Bom(String input) {
        if (TextUtils.isEmpty(input)) return input;
        return input.charAt(0) == '\ufeff' ? input.substring(1) : input;
    }

    /**
     * 判断 Fragment 是否仍处于存活状态
     */
    static boolean isFragmentAlive(Fragment fragment) {
        return !AppUtils.isFragmentDestroyed(fragment);
    }

    /**
     * 判断 Activity 是否仍处于存活状态
     */
    static boolean isActivityAlive(Context context) {
        return !AppUtils.isActivityDestroyed(context);
    }

    /**
     * 根据文件后缀获取 MediaType，不认识的后缀兜底 application/octet-stream
     */
    static MediaType guessMediaType(File file) {
        String name = file.getName().toLowerCase(Locale.US);
        if (name.endsWith(".png")) return MediaType.parse("image/png");
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return MediaType.parse("image/jpeg");
        if (name.endsWith(".webp")) return MediaType.parse("image/webp");
        if (name.endsWith(".gif")) return MediaType.parse("image/gif");
        if (name.endsWith(".bmp")) return MediaType.parse("image/bmp");
        return MediaType.parse("application/octet-stream");
    }
}
