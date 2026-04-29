package com.wcl.test.http;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;
import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

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
     */
    public static void get(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            notifyResult(callback, false, "Invalid URL");
            return;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, withCommonParams(params));
        Request request = buildRequest(finalUrl, headers).get().build();
        enqueue(context, request, callback);
    }

    /**
     * 异步 GET 请求（Fragment 专用，无 Header）
     */
    public static void get(
            Fragment fragment,
            String url,
            Map<String, Object> params,
            HttpCallback callback
    ) {
        get(fragment, url, params, null, callback);
    }

    /**
     * 异步 GET 请求（Fragment 专用，支持 Header）
     */
    public static void get(
            Fragment fragment,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            notifyResult(callback, false, "Invalid URL");
            return;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, withCommonParams(params));
        Request request = buildRequest(finalUrl, headers).get().build();
        enqueue(fragment, request, callback);
    }

    /**
     * 异步 POST 请求（Fragment 专用，无 Header）
     */
    public static void post(
            Fragment fragment,
            String url,
            Map<String, Object> params,
            HttpCallback callback
    ) {
        post(fragment, url, params, null, callback);
    }

    /**
     * 异步 POST 请求（Fragment 专用，支持 Header）
     */
    public static void post(
            Fragment fragment,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            notifyResult(callback, false, "Invalid URL");
            return;
        }
        FormBody body = HttpHelper.buildFormBody(withCommonParams(params));
        Request request = buildRequest(url, headers).post(body).build();
        enqueue(fragment, request, callback);
    }

    /**
     * 异步 POST 请求（无 Header）
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
     */
    public static void post(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        if (!HttpHelper.isValidUrl(url)) {
            notifyResult(callback, false, "Invalid URL");
            return;
        }
        FormBody body = HttpHelper.buildFormBody(withCommonParams(params));
        Request request = buildRequest(url, headers).post(body).build();
        enqueue(context, request, callback);
    }

    /**
     * 上传单张图片（异步）
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
        Map<String, Object> finalParams = withCommonParams(params);
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, Object> entry : finalParams.entrySet()) {
            builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
        }

        builder.addFormDataPart(fileKey, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                AppLogUtils.w(TAG, "uploadImage error: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
    }

    /**
     * 异步下载文件（支持断点续传）
     */
    public static void download(String url, DownloadCallback callback) {
        if (!HttpHelper.isValidUrl(url)) {
            callback.onFinished(false, null, "非法 URL");
            return;
        }
        AppThreadPoolExecutor.getExecutor().execute(() -> Downloader.downloadInternal(url, callback));
    }

    /**
     * 异步下载文件（多线程切块下载 + 支持断点续传 + 进度按1%回调）
     */
    public static void fastDownload(String url, DownloadCallback callback) {
        Downloader.fastDownload(url, callback);
    }

    /**
     * 统一的异步请求入口（Fragment 专用），自动切换到主线程回调
     */
    private static void enqueue(Fragment fragment, Request request, HttpCallback callback) {
        enqueue(() -> HttpHelper.isFragmentAlive(fragment), request, callback);
    }

    /**
     * 统一的异步请求入口，自动切换到主线程回调
     */
    private static void enqueue(Context context, Request request, HttpCallback callback) {
        enqueue(() -> !AppUtils.isActivityDestroyed(context), request, callback);
    }

    /**
     * 核心异步请求入口，自动切换到主线程回调
     *
     * @param isAlive 存活检测器，返回 false 时静默丢弃回调（用于 Activity/Fragment 生命周期安全判断）
     */
    private static void enqueue(BooleanSupplier isAlive, Request request, HttpCallback callback) {
        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAlive.getAsBoolean()) return;
                HttpHelper.postToUi(() -> {
                    if (isAlive.getAsBoolean()) {
                        notifyResult(callback, false, e.toString());
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!isAlive.getAsBoolean()) return;
                try (response) {
                    boolean ok = response.isSuccessful();
                    String responseContent = response.body().string();
                    final String result = HttpHelper.removeUtf8Bom(ok ? responseContent : response.toString());
                    AppLogUtils.i(TAG, "result:" + result);
                    HttpHelper.postToUi(() -> {
                        if (isAlive.getAsBoolean()) {
                            notifyResult(callback, ok, result);
                        }
                    });
                }
            }
        });
    }

    private static Map<String, Object> withCommonParams(Map<String, Object> params) {
        Map<String, Object> finalParams = params == null ? new HashMap<>() : new HashMap<>(params);
        HttpHelper.addCommonParams(finalParams);
        return finalParams;
    }

    private static void notifyResult(HttpCallback callback, boolean success, String result) {
        if (callback != null) {
            callback.onResult(success, result);
        }
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
            AppLogUtils.v(TAG, "cost:" + (end - start) + "ms" + ",retry:" + retry + ",url:" + request.url());
            return response;
        }
    }
}
