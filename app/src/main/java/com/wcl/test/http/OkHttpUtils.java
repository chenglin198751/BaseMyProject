package com.wcl.test.http;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OkHttpUtils {

    public interface HttpCallback {
        void onResult(boolean success, String result);
    }

    public interface DownloadCallback {
        void onProgress(long total, long current, float percent);

        void onFinished(boolean success, String filePath, String error);
    }

    private static final String TAG = "OkHttpUtils";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private OkHttpUtils() {
    }

    /**
     * 异步 GET 请求
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
     * 异步 GET 请求
     * <p>
     * 因为headers不常用，暂时声明为private，后续需要再改成public，或者方法内统一写死header
     */
    private static void get(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        // 为了保证UI安全，context不能传Application
        if (context instanceof Application) {
            throw new IllegalArgumentException("Application context not allowed. Use Activity context");
        }

        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
        HttpRequestHelper.enqueue(context, request, callback);
    }

    /**
     * 异步 GET 请求（Fragment 专用）
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
     * 异步 GET 请求（Fragment 专用）
     * <p>
     * 因为headers不常用，暂时声明为private，后续需要再改成public，或者方法内统一写死header
     */
    private static void get(
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
     * 异步 POST 请求（Fragment 专用）
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
     * 异步 POST 请求（Fragment 专用）
     * <p>
     * 因为headers不常用，暂时声明为private，后续需要再改成public，或者方法内统一写死header
     */
    private static void post(
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
     * 异步 POST 请求
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
     * 异步 POST 请求
     * <p>
     * 因为headers不常用，暂时声明为private，后续需要再改成public，或者方法内统一写死header
     */
    private static void post(
            Context context,
            String url,
            Map<String, Object> params,
            Map<String, String> headers,
            HttpCallback callback
    ) {
        // 为了保证UI安全，context不能传Application
        if (context instanceof Application) {
            throw new IllegalArgumentException("Application context not allowed. Use Activity context");
        }

        if (HttpHelper.isInvalidUrl(url)) {
            HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
            return;
        }
        FormBody body = HttpHelper.buildFormBody(HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(url, headers).post(body).build();
        HttpRequestHelper.enqueue(context, request, callback);
    }

    /**
     * 同步 GET 请求，直接阻塞当前线程直到请求完成。
     * <p>
     * 注意：必须在子线程中调用，可搭配工程内线程池 AppThreadPoolExecutor 使用
     */
    public static String syncGet(
            String url,
            Map<String, Object> params,
            Map<String, String> headers
    ) {
        if (HttpHelper.isInvalidUrl(url)) {
            return null;
        }
        String finalUrl = HttpHelper.buildGetUrl(url, HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
        try (okhttp3.Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return HttpHelper.removeUtf8Bom(response.body().string());
            } else {
                AppLogUtils.w(TAG, "getSync response.isSuccessful()=false");
            }
        } catch (IOException e) {
            AppLogUtils.w(TAG, "getSync error: " + e);
        }
        return null;
    }

    /**
     * 同步 POST 请求，直接阻塞当前线程直到请求完成。
     * <p>
     * 注意：必须在子线程中调用，可搭配工程内线程池 AppThreadPoolExecutor 使用
     */
    public static String syncPost(
            String url,
            Map<String, Object> params,
            Map<String, String> headers
    ) {
        if (HttpHelper.isInvalidUrl(url)) {
            return null;
        }
        FormBody body = HttpHelper.buildFormBody(HttpRequestHelper.withCommonParams(params));
        Request request = HttpRequestHelper.buildRequest(url, headers).post(body).build();
        try (okhttp3.Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return HttpHelper.removeUtf8Bom(response.body().string());
            } else {
                AppLogUtils.w(TAG, "syncPost response.isSuccessful()=false");
            }
        } catch (IOException e) {
            AppLogUtils.w(TAG, "syncPost error: " + e);
        }
        return null;
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

        HttpRequestHelper.CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
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
