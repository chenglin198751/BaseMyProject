package com.wcl.test.http;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;
import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import okhttp3.FormBody;
import okhttp3.Request;


/**
 * 基于 OkHttp 的网络请求工具类，采用 Builder 链式调用风格。
 *
 * <h3>异步 GET（Activity 中使用）</h3>
 * <pre>{@code
 * OkHttpExecutor.get("https://api.example.com/user")
 *     .params(params)
 *     .execute(activity, (success, result) -> {
 *         // 主线程回调，Activity 销毁后自动丢弃
 *     });
 * }</pre>
 *
 * <h3>异步 POST（Fragment 中使用）</h3>
 * <pre>{@code
 * OkHttpExecutor.post("https://api.example.com/login")
 *     .params(params)
 *     .headers(headers)
 *     .execute(fragment, (success, result) -> {
 *         // 主线程回调，Fragment 销毁后自动丢弃
 *     });
 * }</pre>
 *
 * <h3>同步请求（必须在子线程调用）</h3>
 * <pre>{@code
 * AppThreadPoolExecutor.getExecutor().execute(() -> {
 *     // 同步 GET
 *     String result = OkHttpExecutor.get("https://api.example.com/config")
 *         .params(params)
 *         .headers(headers)
 *         .executeSync();
 *
 *     // 同步 POST
 *     String result2 = OkHttpExecutor.post("https://api.example.com/submit")
 *         .params(params)
 *         .executeSync();
 * });
 * }</pre>
 */
public class OkHttpExecutor {

    public interface HttpCallback {
        void onResult(boolean success, String result);
    }

    public interface DownloadCallback {
        void onProgress(long total, long current, float percent);

        void onFinished(boolean success, String filePath, String error);
    }

    public interface UploadCallback {
        /**
         * 第 index 张（从0开始）上传完成，totalCount 为总张数
         */
        void onProgress(int index, int totalCount);

        void onFinished(boolean success, String error);
    }

    private static final String TAG = "OkHttpExecutor";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    private OkHttpExecutor() {
    }

    /**
     * 构建 GET 请求
     */
    public static RequestBuilder get(String url) {
        return new RequestBuilder(url, METHOD_GET);
    }

    /**
     * 构建 POST 请求
     */
    public static RequestBuilder post(String url) {
        return new RequestBuilder(url, METHOD_POST);
    }

    public static class RequestBuilder {
        private final String url;
        private final String method;
        private Map<String, Object> params;
        private Map<String, String> headers;

        RequestBuilder(String url, String method) {
            this.url = url;
            this.method = method;
        }

        public RequestBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public RequestBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * 异步执行请求（Activity 场景）
         */
        public void execute(Activity activity, HttpCallback callback) {
            if (AppUtils.isActivityDestroyed(activity)) {
                AppLogUtils.e(TAG, "The Activity was destroyed");
                return;
            }
            if (LiteHelper.invalidUrl(url)) {
                notifyFailure(callback, "Invalid URL");
                return;
            }
            try {
                HttpRequestHelper.enqueue(activity, buildRequest(), callback);
            } catch (IllegalArgumentException e) {
                notifyFailure(callback, e.toString());
            }
        }

        /**
         * 异步执行请求（Fragment 场景）
         */
        public void execute(Fragment fragment, HttpCallback callback) {
            if (AppUtils.isFragmentDestroyed(fragment)) {
                AppLogUtils.e(TAG, "The Fragment was destroyed");
                return;
            }
            if (LiteHelper.invalidUrl(url)) {
                notifyFailure(callback, "Invalid URL");
                return;
            }
            try {
                HttpRequestHelper.enqueue(fragment, buildRequest(), callback);
            } catch (IllegalArgumentException e) {
                notifyFailure(callback, e.toString());
            }
        }

        /**
         * 同步执行请求，直接阻塞当前线程直到请求完成。
         * <p>
         * 注意：必须在子线程中调用，可搭配工程内线程池 AppThreadPoolExecutor 使用
         */
        public String executeSync() {
            if (LiteHelper.invalidUrl(url)) {
                return null;
            }
            Request request = buildRequest();
            try (okhttp3.Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return LiteHelper.removeUtf8Bom(response.body().string());
                } else {
                    AppLogUtils.w(TAG, "executeSync response.isSuccessful()=false");
                }
            } catch (IOException e) {
                AppLogUtils.w(TAG, "executeSync error: " + e);
            }
            return null;
        }

        private Request buildRequest() {
            Map<String, Object> finalParams = HttpRequestHelper.withCommonParams(params);
            if (METHOD_GET.equals(method)) {
                String finalUrl = LiteHelper.buildGetUrl(url, finalParams);
                return HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
            } else {
                FormBody body = LiteHelper.buildFormBody(finalParams);
                return HttpRequestHelper.buildRequest(url, headers).post(body).build();
            }
        }

        private void notifyFailure(HttpCallback callback, String error) {
            LiteHelper.postToUi(() -> HttpRequestHelper.notifyResult(callback, false, error));
        }
    }

    /**
     * 上传图片（异步，串行逐张上传，主线程回调）
     *
     * @param url      上传地址
     * @param params   附加表单参数
     * @param files    要上传的图片文件列表
     * @param callback 上传回调（可为 null）
     */
    public static void uploadImages(
            String url,
            Map<String, Object> params,
            List<File> files,
            UploadCallback callback
    ) {
        UploadImages.exe(url, params, "file", files, callback);
    }

    /**
     * 异步下载文件（支持断点续传 + 进度按时间间隔回调）
     */
    public static void download(String url, DownloadCallback callback) {
        if (LiteHelper.invalidUrl(url)) {
            LiteHelper.notifyDownloadFailure(callback, "Invalid URL");
            return;
        }
        try {
            AppThreadPoolExecutor.getExecutor().execute(() -> Downloader.downloadInternal(url, callback));
        } catch (RejectedExecutionException e) {
            LiteHelper.notifyDownloadFailure(callback, e.toString());
        }
    }

    /**
     * 异步下载文件（多线程切块下载 + 支持断点续传 + 进度按1%回调）
     */
    public static void fastDownload(String url, DownloadCallback callback) {
        FastDownloader.fastDownload(url, callback);
    }


}
