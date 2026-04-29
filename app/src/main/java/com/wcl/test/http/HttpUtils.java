package com.wcl.test.http;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
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
                .addInterceptor(new HttpRetryInterceptor(1));

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
        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
        HttpRequestHelper.enqueue(context, request, callback);
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
        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
        HttpRequestHelper.enqueue(fragment, request, callback);
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
        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        FormBody body = HttpHelper.buildFormBody(HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(url, headers).post(body).build();
        HttpRequestHelper.enqueue(fragment, request, callback);
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
        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        FormBody body = HttpHelper.buildFormBody(HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(url, headers).post(body).build();
        HttpRequestHelper.enqueue(context, request, callback);
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
        if (HttpHelper.isInvalidUrl(url) || file == null || !file.exists()) {
            return;
        }
        Map<String, Object> finalParams = HttpRequestHelper.withCommonParams(params);
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
        if (HttpHelper.isInvalidUrl(url)) {
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


}
