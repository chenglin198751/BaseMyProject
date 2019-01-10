package httpwork;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import okhttp3.Cache;
import okhttp3.CacheControl;
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
import utils.BaseUtils;
import utils.LogUtils;
import utils.SDCardUtils;

/**
 * Created by chenglin on 2017-5-24.
 * 资料：http://liuwangshu.cn/application/network/6-okhttp3.html
 */

public class HttpUtils {
    private final static String TAG = "HttpUtils";

    public interface HttpCallback {
        void onSuccess(String result);

        void onFailure(HttpException e);
    }

    public interface HttpDownloadCallback {
        void onSuccess(String filePath);

        //fileTotalSize  文件总大小
        //fileDowningSize  文件已经下载的大小
        //percent  文件下载的进度百分比
        void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent);

        void onFailure(IOException e);
    }

    public static class HttpBuilder {
        private boolean cache = false;

        //返回是否用缓存
        public boolean isCache() {
            return cache;
        }

        //设置是否用缓存
        public void setCache(boolean cache) {
            this.cache = cache;
        }
    }

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int TIME_OUT = 30;
    private static final String HTTP_CACHE_PATH = SDCardUtils.getPath() + "httpCache" + File.separator;
    private static final String HTTP_DOWNLOAD_PATH = SDCardUtils.getPath() + "download" + File.separator;

    static {
        File cacheDir = new File(HTTP_CACHE_PATH);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File downloadDir = new File(HTTP_DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    private static final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(new RetryInterceptor(2))
            .cache(new Cache(new File(HTTP_CACHE_PATH), 100 * 1024 * 1024));
    public static final OkHttpClient client = builder.build();


    private HttpUtils() {
    }

    /**
     * 重试拦截器
     */
    public static class RetryInterceptor implements Interceptor {
        //最大重试次数。假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）
        private int maxRetry;

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
            final String url = response.request().url().toString();
            LogUtils.v(TAG, "网络请求时间：" + url + " -- " + (end - start) + "毫秒");

            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                response = chain.proceed(request);
                LogUtils.v(TAG, "第 " + retryNum + " 次重试");
            }
            return response;
        }
    }

    /**
     * 通用的okhttp3.Callback封装
     */
    private static okhttp3.Callback createOkhttp3Callback(final Context context, final HttpCallback httpCallback) {
        return new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (context == null) {
                    return;
                }
                e.printStackTrace();

                final HttpException httpEx = new HttpException();
                httpEx.errorCode = HttpConst.ERROR_UNKNOWN;
                httpEx.errorMsg = e.getMessage();

                if (e instanceof SSLException) {
                    httpEx.errorCode = HttpConst.ERROR_CODE_SSL;
                    httpEx.errorMsg = HttpConst.HTTP_SSL_EXCEPTION;
                } else if (e instanceof ConnectTimeoutException) {
                    httpEx.errorCode = HttpConst.ERROR_CODE_TIME_OUT;
                    httpEx.errorMsg = HttpConst.HTTP_TIME_OUT;
                } else if (e instanceof SocketTimeoutException) {
                    httpEx.errorCode = HttpConst.ERROR_CODE_TIME_OUT;
                    httpEx.errorMsg = HttpConst.HTTP_TIME_OUT_RESPONSE;
                }

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                httpCallback.onFailure(httpEx);
                            }
                        });
                    }
                } else {
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onFailure(httpEx);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (context == null) {
                    return;
                }
                final String result = response.body().string();

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    httpCallback.onSuccess(result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                httpCallback.onSuccess(result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };
    }

    /**
     * 通用的异步post请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void post(final Activity activity, String url, Map<String, Object> paramsHashMap, final HttpBuilder builder, final HttpCallback httpCallback) {
        postWithHeader(activity, url, null, paramsHashMap, builder, httpCallback);
    }

    /**
     * 通用的异步post请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void postWithHeader(final Context context, String url, Map<String, String> headersMap, Map<String, Object> hashMap, HttpBuilder builder, final HttpCallback httpCallback) {
        FormBody.Builder FormBuilder = new FormBody.Builder();
        if (builder == null) {
            builder = new HttpBuilder();
        }

        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        addCommonData(hashMap);

        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            FormBuilder.add(key, value + "");
        }

        RequestBody body = FormBuilder.build();

        final CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        if (!builder.isCache()) {
            cacheBuilder.noCache();//不使用缓存，全部走网络
            cacheBuilder.noStore();//不使用缓存，也不存储缓存
        }
        CacheControl cache = cacheBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .cacheControl(cache)
                .url(url)
                .post(body);
        if (headersMap != null && headersMap.size() > 0) {
            requestBuilder.headers(Headers.of(headersMap));
        }
        Request request = requestBuilder.build();

        Call call = client.newCall(request);
        call.enqueue(createOkhttp3Callback(context, httpCallback));
    }

    /**
     * 通用的异步get请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void get(final Context context, final String url, final HttpCallback httpCallback) {
        final CacheControl.Builder builder = new CacheControl.Builder();
        builder.noCache();//不使用缓存，全部走网络
        builder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = builder.build();
        Request request = new Request.Builder().cacheControl(cache).url(url).get().build();

        Call call = client.newCall(request);
        call.enqueue(createOkhttp3Callback(context, httpCallback));
    }

    /**
     * 通用的上传图片 注：此方法暂时不可用，因为还没测试
     */
    public static void uploadImage(String reqUrl, HashMap<String, Object> params, String picKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return;
        }

        if (params == null) {
            params = new HashMap<>();
        }
        addCommonData(params);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);

        //遍历map中所有参数到builder
        for (String key : params.keySet()) {
            multipartBodyBuilder.addFormDataPart(key, params.get(key) + "");
        }

        //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key
        multipartBodyBuilder.addFormDataPart(picKey, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));

        final CacheControl.Builder builder = new CacheControl.Builder();
        builder.noCache();//不使用缓存，全部走网络
        builder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = builder.build();

        RequestBody requestBody = multipartBodyBuilder.build();
        Request.Builder RequestBuilder = new Request.Builder();
        RequestBuilder.url(reqUrl);
        RequestBuilder.cacheControl(cache);
        RequestBuilder.post(requestBody);
        Request request = RequestBuilder.build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                call.cancel();
            }
        });

    }

    /**
     * 默认下载路径的通用的异步下载文件的方法，返回文件下载成功之后的所在路径，不支持断点续传
     * 注意：不建议在 Activity 里开启下载，因为很容易造成内存泄漏，建议放到 service 或者 intentService 里面
     *
     * @param fileUrl          下载文件地址
     * @param downloadCallback 下载的回调监听
     */
    public static void downloadFile(final String fileUrl, final HttpDownloadCallback downloadCallback) {
        downloadFile(fileUrl, null, false, downloadCallback);
    }


    /**
     * 默认下载路径的通用的异步下载文件的方法，返回文件下载成功之后的所在路径，不支持断点续传
     * 注意：不建议在 Activity 里开启下载，因为很容易造成内存泄漏，建议放到 service 或者 intentService 里面
     *
     * @param fileUrl          下载文件地址
     * @param isNeedCache      是否需要缓存，如果true ，那么此文件同样地址只下载一次
     * @param downloadCallback 下载的回调监听
     */
    public static void downloadFile(final String fileUrl, boolean isNeedCache, final HttpDownloadCallback downloadCallback) {
        downloadFile(fileUrl, null, isNeedCache, downloadCallback);
    }

    /**
     * 可以自定义下载路径的通用的异步下载文件的方法，返回文件下载成功之后的所在路径，不支持断点续传
     * 注意：不建议在 Activity 里开启下载，因为很容易造成内存泄漏，建议放到 service 或者 intentService 里面
     *
     * @param fileUrl          下载文件地址
     * @param downPath         自定义文件下载路径
     * @param isNeedCache      是否需要缓存，如果true ，那么此文件同样地址只下载一次
     * @param downloadCallback 下载的回调监听
     */
    public static void downloadFile(final String fileUrl, final String downPath, boolean isNeedCache, final HttpDownloadCallback downloadCallback) {
        if (downloadCallback == null) {
            throw new NullPointerException("HttpDownloadCallback 不能为空");
        } else if (TextUtils.isEmpty(fileUrl)) {
            downloadCallback.onFailure(new IOException("下载URL不能为空"));
            return;
        }

        try {
            File file = new File(downPath);
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                if (!parent.exists()) {
                    downloadCallback.onFailure(new IOException("文件目录不存在"));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String defaultPath = HTTP_DOWNLOAD_PATH + BaseUtils.MD5(fileUrl).toLowerCase() + getSuffixNameByHttpUrl(fileUrl);
        final String downLoadFilePath = TextUtils.isEmpty(downPath) ? defaultPath : downPath;
        final String tempPath = downLoadFilePath + ".temp";

        //防止下载时中断导致下载文件不全,但被使用了
        File tempFile = new File(tempPath);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        File cacheFile = new File(downLoadFilePath);
        if (cacheFile.exists()) {
            if (isNeedCache) {
                downloadCallback.onSuccess(downLoadFilePath);
                return;
            } else {
                cacheFile.delete();
            }
        }

        final CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        if (!isNeedCache) {
            cacheBuilder.noCache();// 不使用缓存，全部走网络
            cacheBuilder.noStore();// 不使用缓存，也不存储缓存
        }
        CacheControl cache = cacheBuilder.build();
        Request request = new Request.Builder().cacheControl(cache).url(fileUrl).get().build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                BaseUtils.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;

                try {
                    fileOutputStream = new FileOutputStream(new File(tempPath));
                    long total = response.body().contentLength();
                    byte[] buffer = new byte[2048];
                    int len;
                    long sum = 0;
                    int lastProgress = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100f);
                        if (lastProgress != progress) {
                            lastProgress = progress;

                            final long tempTotal = total;
                            final long tempSum = sum;
                            final int tempProgress = progress;
                            BaseUtils.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadCallback.onProgress(call, tempTotal, tempSum, tempProgress);
                                }
                            });
                        }

                    }
                    fileOutputStream.flush();
                    inputStream.close();
                    fileOutputStream.close();

                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            File tempFile = new File(tempPath);
                            if (tempFile.exists()) {
                                boolean isSuccess = tempFile.renameTo(new File(downLoadFilePath));
                                if (isSuccess) {
                                    downloadCallback.onSuccess(downLoadFilePath);
                                } else {
                                    downloadCallback.onFailure(new IOException(tempPath + " rename to " + downLoadFilePath + " fail"));
                                }
                            } else {
                                downloadCallback.onFailure(new IOException(tempPath + " not exists "));
                            }
                        }
                    });

                } catch (final IOException e) {
                    e.printStackTrace();
                    //防止下载时中断导致下载文件不全,但被使用了
                    File tempFile = new File(tempPath);
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            downloadCallback.onFailure(e);
                        }
                    });
                }
            }
        });
    }

    /**
     * 通用字段
     */
    private static void addCommonData(Map<String, Object> params) {
        params.put("IMEI", BaseUtils.getDeviceId());
        params.put("product", Build.MODEL);
        params.put("brand", Build.BRAND);
        params.put("sdkVer", Build.VERSION.SDK_INT);
        params.put("sdkVerName", Build.VERSION.RELEASE);
        params.put("appVer", BaseUtils.getVerCode());
        params.put("appVerName", BaseUtils.getVerName());
        params.put("phone", "android");
        params.put("channel", BaseUtils.getChannel());
        params.put("packageName", BaseUtils.getPackageName());
    }

    /**
     * 构建get请求参数
     */
    public static String buildGetParams(HashMap<String, Object> hashMap) {
        return buildGetParams(null, hashMap);
    }

    /**
     * 构建get请求参数
     */
    public static String buildGetParams(String url, HashMap<String, Object> hashMap) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        addCommonData(hashMap);

        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (params.length() <= 0) {
                params.append("?" + key + "=" + value);
            } else {
                params.append("&" + key + "=" + value);
            }
        }

        if (!TextUtils.isEmpty(url)) {
            return url + params.toString();
        } else {
            return params.toString();
        }
    }

    /**
     * 根据下载文件地址得到文件的后缀名
     */
    private static String getSuffixNameByHttpUrl(final String url) {
        int index = url.lastIndexOf(".");
        if (index < url.length()) {
            return url.substring(index, url.length());
        }
        return "";
    }

    public static class HttpException {
        public int errorCode;
        public String errorMsg;
    }

    private static class HttpConst {
        static final int ERROR_UNKNOWN = 100;
        static final int ERROR_CODE_SSL = 200;
        static final int ERROR_CODE_TIME_OUT = 300;

        static String HTTP_TIME_OUT = "请求超时，请稍后再试...";
        static String HTTP_TIME_OUT_RESPONSE = "响应超时，请稍后再试...";
        static String HTTP_SSL_EXCEPTION = "连接服务器失败，请正确设置手机日期或稍后重试";
    }
}
