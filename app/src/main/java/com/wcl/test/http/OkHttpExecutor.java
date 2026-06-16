package com.wcl.test.http;

import android.app.Application;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


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

    private OkHttpExecutor() {
    }

    /**
     * 构建 GET 请求
     */
    public static RequestBuilder get(String url) {
        return new RequestBuilder(url, "GET");
    }

    /**
     * 构建 POST 请求
     */
    public static RequestBuilder post(String url) {
        return new RequestBuilder(url, "POST");
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
        public void execute(Context context, HttpCallback callback) {
            // 为了保证UI安全，context不能传Application
            if (context instanceof Application) {
                throw new IllegalArgumentException("Application context not allowed. Use Activity context");
            }
            if (LiteHelper.isInvalidUrl(url)) {
                HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
                return;
            }
            Request request = buildRequest();
            HttpRequestHelper.enqueue(context, request, callback);
        }

        /**
         * 异步执行请求（Fragment 场景）
         */
        public void execute(Fragment fragment, HttpCallback callback) {
            if (LiteHelper.isInvalidUrl(url)) {
                HttpRequestHelper.notifyResult(callback, false, "Invalid URL");
                return;
            }
            Request request = buildRequest();
            HttpRequestHelper.enqueue(fragment, request, callback);
        }

        /**
         * 同步执行请求，直接阻塞当前线程直到请求完成。
         * <p>
         * 注意：必须在子线程中调用，可搭配工程内线程池 AppThreadPoolExecutor 使用
         */
        public String executeSync() {
            if (LiteHelper.isInvalidUrl(url)) {
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
            if ("GET".equals(method)) {
                String finalUrl = LiteHelper.buildGetUrl(url, finalParams);
                return HttpRequestHelper.buildRequest(finalUrl, headers).get().build();
            } else {
                FormBody body = LiteHelper.buildFormBody(finalParams);
                return HttpRequestHelper.buildRequest(url, headers).post(body).build();
            }
        }
    }

    /**
     * 上传图片（异步，串行逐张上传，主线程回调）
     *
     * @param url      上传地址
     * @param params   附加表单参数
     * @param fileKey  文件对应的表单 key
     * @param files    要上传的图片文件列表
     * @param callback 上传回调（可为 null）
     */
    public static void uploadImages(
            String url,
            Map<String, Object> params,
            String fileKey,
            List<File> files,
            UploadCallback callback
    ) {
        if (LiteHelper.isInvalidUrl(url) || files == null || files.isEmpty()) {
            if (callback != null) {
                callback.onFinished(false, "无效的 URL 或文件列表为空");
            }
            return;
        }
        AppThreadPoolExecutor.getExecutor().execute(() -> {
            Map<String, Object> finalParams = HttpRequestHelper.withCommonParams(params);
            int totalCount = files.size();
            for (int i = 0; i < totalCount; i++) {
                File file = files.get(i);
                if (file == null || !file.exists()) {
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onFinished(false, "文件不存在: " + file);
                        }
                    });
                    return;
                }
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Map.Entry<String, Object> entry : finalParams.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
                }
                builder.addFormDataPart(fileKey, file.getName(),
                        RequestBody.create(LiteHelper.guessMediaType(file), file));
                Request request = new Request.Builder().url(url).post(builder.build()).build();
                try (Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String error = "上传失败: " + response.code();
                        LiteHelper.postToUi(() -> {
                            if (callback != null) {
                                callback.onFinished(false, error);
                            }
                        });
                        return;
                    }
                    final int index = i;
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onProgress(index, totalCount);
                        }
                    });
                } catch (IOException e) {
                    AppLogUtils.w(TAG, "uploadImages error: " + e);
                    String error = e.toString();
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onFinished(false, error);
                        }
                    });
                    return;
                }
            }
            LiteHelper.postToUi(() -> {
                if (callback != null) {
                    callback.onFinished(true, null);
                }
            });
        });
    }

    /**
     * 异步下载文件（支持断点续传 + 进度按时间间隔回调）
     */
    public static void download(String url, DownloadCallback callback) {
        if (LiteHelper.isInvalidUrl(url)) {
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
