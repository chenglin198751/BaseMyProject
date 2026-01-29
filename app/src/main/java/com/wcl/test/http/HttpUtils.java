package com.wcl.test.http;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtils {

    public interface HttpCallback {
        void onResult(boolean success, String result);
    }

    public interface DownloadCallback {
        void onProgress(long total, long current, float percent);

        void onFinished(boolean success, String filePath, String error);
    }

    private static final String TAG = "HttpUtils";
    private static final int TIME_OUT = 15;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    static final OkHttpClient CLIENT;
    static final Set<String> DOWNLOADING_URLS = ConcurrentHashMap.newKeySet();

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(1));

        if (!EnvToggle.isDebug()) {
            builder.proxy(Proxy.NO_PROXY);
        }
        CLIENT = builder.build();
    }

    private HttpUtils() {
    }

    /**
     * 异步 GET 请求（无 Header）
     *
     * @param context  Context，用于生命周期安全判断
     * @param url      请求地址
     * @param params   GET 参数（会自动追加公共参数）
     * @param callback 回调（主线程）
     */
    public static void get(
            Context context,
            String url,
            Map<String, Object> params,
            HttpCallback callback
    ) {
        get(context, url, params, null, callback);
    }

    /**
     * 异步 GET 请求（支持 Header）
     *
     * @param context  Context，用于生命周期安全判断
     * @param url      请求地址
     * @param params   GET 参数（会自动追加公共参数）
     * @param headers  HTTP Header（可为 null）
     * @param callback 回调（主线程）
     */
    public static void get(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            callback.onResult(false, "Invalid URL");
            return;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        HttpHelper.addCommonParams(params);
        String finalUrl = HttpHelper.buildGetUrl(url, params);
        Request request = buildRequest(finalUrl, headers).get().build();
        enqueue(context, request, callback);
    }

    /* ======================= POST ======================= */

    /**
     * 异步 POST 请求（无 Header）
     *
     * @param context  Context，用于生命周期安全判断
     * @param url      请求地址
     * @param params   POST 参数（Form 表单，会自动追加公共参数）
     * @param callback 回调（主线程）
     */
    public static void post(
            Context context,
            String url,
            Map<String, Object> params,
            HttpCallback callback
    ) {
        post(context, url, params, null, callback);
    }

    /**
     * 异步 POST 请求（支持 Header）
     *
     * @param context  Context，用于生命周期安全判断
     * @param url      请求地址
     * @param params   POST 参数（Form 表单，会自动追加公共参数）
     * @param headers  HTTP Header（可为 null）
     * @param callback 回调（主线程）
     */
    public static void post(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            callback.onResult(false, "Invalid URL");
            return;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        HttpHelper.addCommonParams(params);
        FormBody body = HttpHelper.buildFormBody(params);
        Request request = buildRequest(url, headers).post(body).build();
        enqueue(context, request, callback);
    }

    /**
     * 上传单张图片（异步）
     *
     * @param url     上传地址
     * @param params  表单参数（会自动追加公共参数）
     * @param fileKey 文件字段名（如 "image"）
     * @param file    本地文件
     */
    public static void uploadImage(
            String url,
            Map<String, Object> params,
            String fileKey,
            File file
    ) {
        if (!HttpHelper.isValidUrl(url) || file == null || !file.exists()) {
            return;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        HttpHelper.addCommonParams(params);
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
        }

        builder.addFormDataPart(fileKey, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
    }

    /**
     * 异步下载文件（支持断点续传）
     *
     * @param url      文件下载地址
     * @param callback 下载回调（主线程）
     */
    public static void download(String url, DownloadCallback callback) {
        if (!HttpHelper.isValidUrl(url)) {
            callback.onFinished(false, null, "非法 URL");
            return;
        }
        AppThreadPoolExecutor.getExecutor().execute(() -> HttpDownload.downloadInternal(url, callback));
    }

    /**
     * 异步下载文件（多线程切块下载 + 支持断点续传 + 进度按1%回调）
     *
     * @param url      文件下载地址
     * @param callback 下载回调（主线程）
     */
    public static void fastDownload(String url, DownloadCallback callback) {
        HttpDownload.fastDownload(url, callback);
    }

    /**
     * 统一的异步请求入口，自动切换到主线程回调
     */
    private static void enqueue(Context context, Request request, HttpCallback callback) {
        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (AppBaseUtils.isActivityDestroyed(context)) return;
                HttpHelper.postToUi(() -> callback.onResult(false, e.toString()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (AppBaseUtils.isActivityDestroyed(context)) return;
                boolean ok = response.isSuccessful();
                final String result = HttpHelper.removeUtf8Bom(ok ? response.body().string() : response.toString());
                AppLogUtils.v(TAG, "result:" + result);
                response.close();
                HttpHelper.postToUi(() -> callback.onResult(ok, result));
            }
        });
    }

    /**
     * 构建支持 Header 的 Request.Builder
     */
    private static Request.Builder buildRequest(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }
        return builder;
    }

    /**
     * 简单重试拦截器（仅在请求失败时重试）
     */
    private record RetryInterceptor(int maxRetry) implements Interceptor {

        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            long start = System.currentTimeMillis();
            Request request = chain.request();
            Response response = chain.proceed(request);
            int retry = 0;
            while (!response.isSuccessful() && retry < maxRetry) {
                retry++;
                response.close();
                response = chain.proceed(request);
            }
            long end = System.currentTimeMillis();
            AppLogUtils.v(TAG, "cost:" + (end - start) + "ms" + ",url:" + response.request().url());
            return response;
        }
    }
}
