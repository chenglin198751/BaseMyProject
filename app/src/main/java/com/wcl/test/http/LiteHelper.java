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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

class LiteHelper {

    private static final String TAG = "HttpHelper";

    // 如果是无效 url（空或无法解析为 http/https）则返回 true
    static boolean isInvalidUrl(String url) {
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
            dir = new File(AppUtils.FileUtils.getAppStoragePath());
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

    // 从 url 获取文件长度和版本校验值
    static DownloadMetadata fetchDownloadMetadata(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept-Encoding", "identity")
                .build();
        try (Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            long totalLength = response.body().contentLength();
            String validator = response.header("ETag");
            if (TextUtils.isEmpty(validator)) {
                validator = response.header("Last-Modified");
            }
            if (totalLength <= 0) return null;
            return new DownloadMetadata(totalLength, validator);
        } catch (Exception e) {
            AppLogUtils.e(TAG, "fetch download metadata error: " + e);
            return null;
        }
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

    static File getDownloadMetadataFile(File target) {
        return new File(target.getAbsolutePath() + ".download.meta");
    }

    static DownloadMetadata readDownloadMetadata(File target) {
        File metadataFile = getDownloadMetadataFile(target);
        if (!metadataFile.isFile()) return null;

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(metadataFile)) {
            properties.load(input);
            long totalLength = Long.parseLong(properties.getProperty("totalLength"));
            String validator = properties.getProperty("validator");
            if (totalLength <= 0) return null;
            return new DownloadMetadata(totalLength, validator);
        } catch (Exception e) {
            AppLogUtils.e(TAG, "read download metadata error: " + e);
            return null;
        }
    }

    static boolean writeDownloadMetadata(File target, DownloadMetadata metadata) {
        File metadataFile = getDownloadMetadataFile(target);
        File tempFile = new File(metadataFile.getAbsolutePath() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("totalLength", String.valueOf(metadata.totalLength()));
        if (!TextUtils.isEmpty(metadata.validator())) {
            properties.setProperty("validator", metadata.validator());
        }
        try (FileOutputStream output = new FileOutputStream(tempFile)) {
            properties.store(output, null);
        } catch (IOException e) {
            AppLogUtils.e(TAG, "write download metadata error: " + e);
            return false;
        }
        return replaceFile(tempFile, metadataFile);
    }

    static void deleteDownloadMetadata(File target) {
        File metadataFile = getDownloadMetadataFile(target);
        if (metadataFile.exists() && !metadataFile.delete()) {
            AppLogUtils.w(TAG, "delete download metadata failed: " + metadataFile);
        }
    }

    record DownloadMetadata(long totalLength, String validator) {
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
