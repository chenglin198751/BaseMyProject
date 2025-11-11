package com.wcl.test.httpwork;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;
import com.wcl.test.utils.DeviceUtils;
import com.wcl.test.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//为了防止参数被人抓包恶意串改，所以建议客户端和服务器的参数传递做签名校验：
//1、把要上传的参数按照key值做升序排列(a-z升序排序)，并用&符号连接，md5之后转为小写。Java开发可以使用TreeMap自动排序。
//   特别注意，空值（空字符串或null）不参与签名运算。建议加入时间戳ts作为签名参数之一。示例如下：
//   String signature = to_lower_case(md5(key1=value1&ts=时间戳&...&keyN=valueN)))
//2、将被签名字符串转成字节数组时必须指定编码为utf-8。服务端也需要采用上述签名算法校验。
//3、把signature作为和普通参数同级的参数，传递给服务端。
//4、补充：如果还想要更安全，让服务器把返回结果用DES加密一下，客户端再解密使用。工具类：DESUtils.java

/**
 * 优化后的 HttpUtils
 * 功能：同步/异步 GET/POST、上传图片、下载文件（断点续传）、重试机制、UI线程回调
 */
public class HttpUtils {

    public interface HttpCallback {
        void onResponse(boolean isSuccessful, String result);
    }

    public interface HttpDownloadCallback {
        void onFinished(boolean isSuccessful, String filePath, String error);

        void onProgress(long fileTotalSize, long fileDowningSize, float percent);
    }

    private static final String TAG = "HttpUtils";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int TIME_OUT = 15;
    private static final File DOWNLOAD_DIR = new File(FileUtils.getExternalPath(), "download");
    private static final OkHttpClient mOkHttpClient;
    private static final List<String> mDowningUrls = new ArrayList<>();

    static {
        DOWNLOAD_DIR.mkdirs();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(1));

        if (!EnvToggle.isDebug()) {
            builder.proxy(Proxy.NO_PROXY);
        }

        mOkHttpClient = builder.build();
    }

    private HttpUtils() {
    }

    /**
     * RetryInterceptor
     **/
    public static class RetryInterceptor implements Interceptor {
        private final int maxRetry;

        public RetryInterceptor(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            int retryNum = 0;
            Request request = chain.request();
            long start = System.currentTimeMillis();
            Response response = chain.proceed(request);
            long end = System.currentTimeMillis();
            AppLogUtils.v(TAG, "网络请求时间：" + response.request().url() + " -- " + (end - start) + "ms");

            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                response.close();
                long retryStart = System.currentTimeMillis();
                response = chain.proceed(request);
                AppLogUtils.v(TAG, "第 " + retryNum + " 次重试,耗时：" + (System.currentTimeMillis() - retryStart) + "ms");
            }
            return response;
        }
    }

    /**
     * 通用 UI 线程回调
     **/
    private static void postToUi(Runnable r) {
        AppBaseUtils.getUiHandler().post(r);
    }

    private static boolean isDestroyed(Context context) {
        return context == null || (context instanceof Activity && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed()));
    }

    private static String parseResponseBody(Response response) throws IOException {
        String result = response.body().string();
        if (!TextUtils.isEmpty(result) && result.startsWith("\ufeff")) {
            result = result.substring(1);
        }
        response.body().close();
        response.close();
        return result;
    }

    /**
     * 构建 FormBody
     **/
    private static FormBody buildFormBody(Map<String, Object> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return builder.build();
    }

    /**
     * 异步 GET/POST 封装
     **/
    private static okhttp3.Callback createOkHttpCallback(final Context context, final HttpCallback callback) {
        return new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isDestroyed(context)) return;
                postToUi(() -> callback.onResponse(false, e.toString()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isDestroyed(context)) return;
                String result = response.isSuccessful() && response.body() != null ? parseResponseBody(response) : null;
                postToUi(() -> callback.onResponse(response.isSuccessful() && result != null, result != null ? result : response.toString()));
            }
        };
    }

    /**
     * POST 异步
     **/
    public static void post(final Context context, String url, Map<String, Object> params, final HttpCallback callback) {
        postWithHeaders(context, url, params, null, callback);
    }

    public static void postWithHeaders(final Context context, final String url, Map<String, Object> params, Map<String, String> headers, final HttpCallback callback) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            callback.onResponse(false, url + " 不是有效URL");
            return;
        }

        if (params == null) params = new HashMap<>();
        addCommonData(params);

        FormBody body = buildFormBody(params);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        if (headers != null && !headers.isEmpty()) {
            requestBuilder.headers(okhttp3.Headers.of(headers));
        }
        mOkHttpClient.newCall(requestBuilder.build()).enqueue(createOkHttpCallback(context, callback));
    }

    /**
     * GET 异步
     **/
    public static void get(final Context context, String url, Map<String, Object> params, final HttpCallback callback) {
        getWithHeaders(context, url, params, null, callback);
    }

    public static void getWithHeaders(final Context context, final String url, Map<String, Object> params, Map<String, String> headers, final HttpCallback callback) {
        final String urlWithParams = buildGetParams(url, params);
        Request.Builder requestBuilder = new Request.Builder().url(urlWithParams).get();
        if (headers != null && !headers.isEmpty()) {
            requestBuilder.headers(okhttp3.Headers.of(headers));
        }
        mOkHttpClient.newCall(requestBuilder.build()).enqueue(createOkHttpCallback(context, callback));
    }

    /**
     * 同步 GET 请求
     **/
    public static String getSync(final String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = mOkHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            return parseResponseBody(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 同步 POST 请求
     **/
    public static String postSync(final String url, Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        addCommonData(params);
        Request request = new Request.Builder().url(url).post(buildFormBody(params)).build();

        try (Response response = mOkHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            return parseResponseBody(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 上传图片
     **/
    public static void uploadImage(String url, Map<String, Object> params, String picKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        File file = new File(filePath);
        if (!file.exists()) return;

        if (params == null) params = new HashMap<>();
        addCommonData(params);

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        params.forEach((k, v) -> multipartBuilder.addFormDataPart(k, String.valueOf(v)));
        multipartBuilder.addFormDataPart(picKey, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));

        Request request = new Request.Builder().url(url).post(multipartBuilder.build()).build();
        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) parseResponseBody(response);
            }
        });
    }

    /**
     * 异步下载文件
     **/
    public static void downloadFile(final String fileUrl, final HttpDownloadCallback callback) {
        if (callback == null) throw new NullPointerException("HttpDownloadCallback不能为空");
        if (TextUtils.isEmpty(fileUrl)) {
            callback.onFinished(false, null, "下载URL不能为空");
            return;
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            callback.onFinished(false, null, fileUrl + " 不是有效URL");
            return;
        }

        AppThreadPoolExecutor.getExecutor().execute(() -> downloadFileSync(fileUrl, callback));
    }

    /**
     * 同步下载文件（断点续传）
     **/
    private static String downloadFileSync(final String fileUrl, final HttpDownloadCallback callback) {
        if (AppBaseUtils.isUiThread()) throw new RuntimeException("同步下载不能在UI线程执行");
        if (mDowningUrls.contains(fileUrl)) {
            postToUi(() -> callback.onFinished(false, null, "文件正在下载中"));
            return null;
        }

        mDowningUrls.add(fileUrl);
        File downFile = new File(getDownLoadFilePath(fileUrl));
        File tempFile = new File(downFile.getAbsolutePath() + ".temp");
        long contentLength = getFileContentLength(fileUrl);
        long downloadedLength = tempFile.exists() ? tempFile.length() : 0;

        try (RandomAccessFile outFile = new RandomAccessFile(tempFile, "rws")) {
            Request.Builder requestBuilder = new Request.Builder().url(fileUrl).get();
            if (downloadedLength > 0 && downloadedLength < contentLength) {
                requestBuilder.addHeader("RANGE", "bytes=" + downloadedLength + "-" + contentLength);
                outFile.seek(downloadedLength);
            }

            try (Response response = mOkHttpClient.newCall(requestBuilder.build()).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    postToUi(() -> callback.onFinished(false, null, "下载失败"));
                    return null;
                }

                try (InputStream input = response.body().byteStream()) {
                    byte[] buffer = new byte[2048];
                    int len;
                    long sum = downloadedLength;
                    long lastUpdate = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        outFile.write(buffer, 0, len);
                        sum += len;
                        int progress = (int) (sum * 100 / contentLength);
                        if (System.currentTimeMillis() - lastUpdate > 500) {
                            final long fSum = sum;
                            postToUi(() -> callback.onProgress(contentLength, fSum, fSum * 1f / contentLength));
                            lastUpdate = System.currentTimeMillis();
                        }
                    }
                }
            }

            if (tempFile.length() == contentLength && tempFile.renameTo(downFile)) {
                postToUi(() -> callback.onFinished(true, downFile.getAbsolutePath(), null));
                return downFile.getAbsolutePath();
            } else {
                postToUi(() -> callback.onFinished(false, null, "下载失败"));
                return null;
            }

        } catch (Throwable t) {
            t.printStackTrace();
            postToUi(() -> callback.onFinished(false, null, t.toString()));
            return null;
        } finally {
            mDowningUrls.remove(fileUrl);
        }
    }

    /**
     * 获取文件长度
     **/
    private static long getFileContentLength(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = mOkHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().contentLength();
            }
        } catch (IOException ignored) {
        }
        return 0;
    }

    /**
     * 构建 GET 参数
     **/
    public static String buildGetParams(String url, Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        addCommonData(params);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (sb.length() == 0 && !url.contains("?")) {
                sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
            } else {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return url + sb.toString();
    }

    /**
     * 文件下载路径
     **/
    public static String getDownLoadFilePath(String fileUrl) {
        return new File(DOWNLOAD_DIR, AppBaseUtils.MD5(fileUrl).toLowerCase() + getSuffixNameByHttpUrl(fileUrl)).getAbsolutePath();
    }

    private static String getSuffixNameByHttpUrl(final String url) {
        int index = url.lastIndexOf(".");
        return index > 0 ? url.substring(index) : "";
    }

    /**
     * 添加公共参数
     **/
    private static void addCommonData(Map<String, Object> params) {
        params.put("deviceId", DeviceUtils.getDeviceId());
        params.put("product", Build.MODEL);
        params.put("brand", Build.BRAND);
        params.put("sdkVer", Build.VERSION.SDK_INT);
        params.put("sdkVerName", Build.VERSION.RELEASE);
        params.put("appVer", AppBaseUtils.getVerCode());
        params.put("appVerName", AppBaseUtils.getVerName());
        params.put("phone", "android");
        params.put("channel", AppBaseUtils.getChannel());
        params.put("packageName", AppBaseUtils.getPackageName());
    }
}
