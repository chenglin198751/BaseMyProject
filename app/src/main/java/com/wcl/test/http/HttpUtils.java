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

    private static final OkHttpClient CLIENT;
    private static final Set<String> DOWNLOADING_URLS = ConcurrentHashMap.newKeySet();

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
        AppThreadPoolExecutor.getExecutor().execute(() -> downloadInternal(url, callback));
    }

    /**
     * 异步下载文件（多线程切块下载 + 进度按1%回调）
     *
     * @param url      文件下载地址
     * @param callback 下载回调（主线程）
     */
    public static void fastDownload(String url, DownloadCallback callback) {
        new Thread(() -> {
            File target = new File(HttpHelper.getDownloadPath(url));
            File tempDir = new File(target.getAbsolutePath() + "_tmp");
            tempDir.mkdirs();

            // 获取文件总长度
            long totalLength = HttpHelper.fetchContentLength(CLIENT, url);

            // 文件已下载直接回调
            if (target.exists() && totalLength > 0 && totalLength == target.length()) {
                HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));
                return;
            }

            try {
                if (totalLength <= 0) {
                    HttpHelper.postToUi(() -> callback.onFinished(false, null, "无法获取文件大小"));
                    return;
                }

                int threadCount = 4;
                long blockSize = totalLength / threadCount;

                Thread[] threads = new Thread[threadCount];
                AtomicLong downloaded = new AtomicLong(0); // 累计下载长度
                AtomicInteger lastPercent = new AtomicInteger(0); // 上一次回调的百分比

                for (int i = 0; i < threadCount; i++) {
                    long start = i * blockSize;
                    long end = (i == threadCount - 1) ? totalLength - 1 : (start + blockSize - 1);
                    int index = i;

                    threads[i] = new Thread(() -> {
                        File partFile = new File(tempDir, "part_" + index);
                        try (RandomAccessFile raf = new RandomAccessFile(partFile, "rw")) {
                            Request request = new Request.Builder()
                                    .url(url)
                                    .addHeader("Range", "bytes=" + start + "-" + end)
                                    .build();

                            try (Response response = CLIENT.newCall(request).execute()) {
                                if (!response.isSuccessful() || response.body() == null) return;

                                try (InputStream in = response.body().byteStream()) {
                                    byte[] buffer = new byte[8192];
                                    int len;
                                    while ((len = in.read(buffer)) != -1) {
                                        raf.write(buffer, 0, len);

                                        // 累加已下载长度
                                        long curDownloaded = downloaded.addAndGet(len);
                                        int percent = (int) ((curDownloaded * 100) / totalLength);

                                        // 仅当当前百分比 > 上一次回调百分比时才回调
                                        int last = lastPercent.get();
                                        if (percent > last) {
                                            if (lastPercent.compareAndSet(last, percent)) {
                                                HttpHelper.postToUi(() ->
                                                        callback.onProgress(totalLength, curDownloaded, percent / 100f)
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    threads[i].start();
                }

                // 等待所有线程完成
                for (Thread t : threads) t.join();

                // 合并文件
                try (RandomAccessFile out = new RandomAccessFile(target, "rw")) {
                    byte[] buffer = new byte[8192];
                    for (int i = 0; i < threadCount; i++) {
                        File partFile = new File(tempDir, "part_" + i);
                        try (RandomAccessFile partRaf = new RandomAccessFile(partFile, "r")) {
                            int len;
                            while ((len = partRaf.read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                            }
                        }
                        partFile.delete();
                    }
                }
                tempDir.delete();

                // 下载完成回调
                HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));

            } catch (Throwable t) {
                t.printStackTrace();
                HttpHelper.postToUi(() -> callback.onFinished(false, null, t.toString()));
            }
        }).start();
    }

    private static void downloadInternal(String url, DownloadCallback callback) {
        long totalLength = HttpHelper.fetchContentLength(CLIENT, url);

        // 检查文件是否已经完整下载，如果已经被下载成功则直接返回file path
        File downFile = new File(HttpHelper.getDownloadPath(url));
        if (downFile.exists() && totalLength > 0 && totalLength == downFile.length()) {
            HttpHelper.postToUi(() -> callback.onFinished(true, downFile.getAbsolutePath(), null));
            return;
        }

        if (!DOWNLOADING_URLS.add(url)) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, "file is downloading"));
            return;
        }

        File target = new File(HttpHelper.getDownloadPath(url));
        File temp = new File(target.getAbsolutePath() + ".temp");
        long downloaded = temp.exists() ? temp.length() : 0;

        Request.Builder builder = new Request.Builder().url(url);
        if (downloaded > 0) {
            builder.addHeader("Range", "bytes=" + downloaded + "-");
        }

        try (Response response = CLIENT.newCall(builder.build()).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                HttpHelper.postToUi(() -> callback.onFinished(false, null, "download fail: " + response));
                return;
            }

            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(temp, true)) {

                byte[] buffer = new byte[4096];
                int len;
                long sum = downloaded;
                int lastPercent = (int) ((sum * 100) / totalLength);

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    sum += len;

                    if (totalLength > 0) {
                        int percent = (int) ((sum * 100) / totalLength);
                        if (percent > lastPercent) { // 每下载1%回调一次
                            lastPercent = percent;
                            long curSum = sum; // lambda里用final变量
                            HttpHelper.postToUi(() -> callback.onProgress(totalLength, curSum, percent / 100f));
                        }
                    }
                }
            }

            HttpHelper.replaceFile(temp, target);
            HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));
        } catch (Throwable t) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, t.toString()));
        } finally {
            DOWNLOADING_URLS.remove(url);
        }
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
                String result = ok ? response.body().string() : response.toString();
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
