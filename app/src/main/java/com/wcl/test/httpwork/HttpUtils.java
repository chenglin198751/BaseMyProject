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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

//为了防止参数被人抓包恶意串改，所以建议客户端和服务器的参数传递做签名校验：
//1、把要上传的参数按照key值做升序排列(a-z升序排序)，并用&符号连接，md5之后转为小写。Java开发可以使用TreeMap自动排序。
//   特别注意，空值（空字符串或null）不参与签名运算。建议加入时间戳ts作为签名参数之一。示例如下：
//   String signature = to_lower_case(md5(key1=value1&ts=时间戳&...&keyN=valueN)))
//2、将被签名字符串转成字节数组时必须指定编码为utf-8。服务端也需要采用上述签名算法校验。
//3、把signature作为和普通参数同级的参数，传递给服务端。
//4、补充：如果还想要更安全，让服务器把返回结果用DES加密一下，客户端再解密使用。工具类：DESUtils.java

/**
 * Created by chenglin on 2017-5-24.
 * 资料：http://liuwangshu.cn/application/network/6-okhttp3.html
 */
public class HttpUtils {

    public interface HttpCallback {
        void onResponse(boolean isSuccessful, String result);
    }

    public interface HttpDownloadCallback {
        void onFinished(boolean isSuccessful, String filePath, Exception e);

        //fileTotalSize  文件总大小
        //fileDowningSize  文件已经下载的大小
        //percent  文件下载的进度百分比
        void onProgress(long fileTotalSize, long fileDowningSize, float percent);
    }

    //用于扩展一些网络功能，比如写入header,写入cookies等操作
    public static class HttpBuilder {
        public Map<String, String> headersMap = null;
    }

    private final static String TAG = "HttpUtils";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int TIME_OUT = 15;
    private static final String HTTP_DOWNLOAD_PATH = FileUtils.getExternalPath() + "/download";
    public static final OkHttpClient mOkHttpClient;

    static {
        File downloadDir = new File(HTTP_DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        final OkHttpClient.Builder builder = new OkHttpClient
                .Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(1));

        if (!EnvToggle.isDebug()) {
            //禁用抓包工具抓包
            builder.proxy(Proxy.NO_PROXY);
        }
        mOkHttpClient = builder.build();
    }

    private HttpUtils() {
    }

    /**
     * 重试拦截器
     */
    public static class RetryInterceptor implements Interceptor {
        //最大重试次数。假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）
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
            final String url = response.request().url().toString();
            AppLogUtils.v(TAG, "网络请求时间：" + url + " -- " + (end - start) + "毫秒");

            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                response.close();
                response = chain.proceed(request);
                AppLogUtils.v(TAG, "第 " + retryNum + " 次重试");
            }
            return response;
        }
    }

    /**
     * 通用的okhttp3.Callback封装
     */
    private static okhttp3.Callback createOkhttp3Callback(final Context context, final HttpCallback httpBack) {
        final HttpCallback httpCallback = new HttpCallback() {
            @Override
            public void onResponse(boolean isSuccessful, String result) {
                if (httpBack != null) {
                    httpBack.onResponse(true, result);
                }
            }
        };

        return new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                e.printStackTrace();
                if (context == null) {
                    return;
                }

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        HttpCallback2.onResponse(httpCallback, false, e.toString());
                    }
                } else {
                    HttpCallback2.onResponse(httpCallback, false, e.toString());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (context == null) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    HttpCallback2.onResponse(httpCallback, false, response.toString());
                    return;
                }

                //有时服务端返回json带了bom头，会导致解析异常
                String result = response.body().string();
                if (!TextUtils.isEmpty(result) && result.startsWith("\ufeff")) {
                    result = result.substring(1);
                }
                response.body().close();
                response.close();

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        HttpCallback2.onResponse(httpCallback, true, result);
                    }
                } else {
                    HttpCallback2.onResponse(httpCallback, true, result);
                }
            }
        };
    }

    /**
     * 通用的异步post请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void post(final Context context, String url, Map<String, Object> params, final HttpUtils.HttpCallback httpCallback) {
        HttpUtils.HttpBuilder builder = new HttpUtils.HttpBuilder();
        builder.headersMap = null;
        HttpUtils.postWithBuilder(context, url, params, builder, httpCallback);
    }

    public static void postWithBuilder(final Context context, final String url, Map<String, Object> params, HttpBuilder builder, final HttpCallback httpCallback) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            httpCallback.onResponse(false, (url + " 不是有效的URL"));
            return;
        }

        FormBody.Builder FormBuilder = new FormBody.Builder();
        if (builder == null) {
            builder = new HttpBuilder();
        }

        if (params == null) {
            params = new HashMap<>();
        }
        addCommonData(params);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            FormBuilder.add(key, value + "");
        }

        RequestBody body = FormBuilder.build();
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        if (builder.headersMap != null && builder.headersMap.size() > 0) {
            requestBuilder.headers(Headers.of(builder.headersMap));
        }
        Request request = requestBuilder.build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(createOkhttp3Callback(context, httpCallback));
    }

    /**
     * 通用的异步get请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void get(final Context context, final String url, Map<String, Object> params, final HttpCallback httpCallback) {
        HttpUtils.HttpBuilder builder = new HttpUtils.HttpBuilder();
        builder.headersMap = null;
        HttpUtils.getWithBuilder(context, url, params, builder, httpCallback);
    }

    public static void getWithBuilder(final Context context, final String url, Map<String, Object> params, HttpBuilder builder, final HttpCallback httpCallback) {
        final String url2 = buildGetParams(url, params);
        Request.Builder requestBuilder = new Request.Builder().url(url2).get();
        if (builder.headersMap != null && builder.headersMap.size() > 0) {
            requestBuilder.headers(Headers.of(builder.headersMap));
        }
        Request request = requestBuilder.build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(createOkhttp3Callback(context, httpCallback));
    }

    /**
     * 同步get请求，必须放到线程中
     */
    public static String syncGet(final String url) {
        Request request = new Request.Builder().url(url).get().build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.body() == null) {
                response.close();
                return null;
            }
            if (response.isSuccessful()) {
                //有时服务端返回json带了bom头，会导致解析异常
                String tempStr = response.body().string();
                if (!TextUtils.isEmpty(tempStr) && tempStr.startsWith("\ufeff")) {
                    tempStr = tempStr.substring(1);
                }
                response.body().close();
                response.close();
                return tempStr;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 同步post请求，必须放到线程中
     */
    public static String syncPost(final String url, Map<String, Object> params) {
        FormBody.Builder FormBuilder = new FormBody.Builder();

        if (params == null) {
            params = new HashMap<>();
        }
        addCommonData(params);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            FormBuilder.add(key, value + "");
        }

        RequestBody body = FormBuilder.build();
        Request.Builder requestBuilder = new Request
                .Builder()
                .url(url)
                .post(body);
        Request request = requestBuilder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.body() == null) {
                response.close();
                return null;
            }
            if (response.isSuccessful()) {
                //有时服务端返回json带了bom头，会导致解析异常
                String result = response.body().string();
                if (!TextUtils.isEmpty(result) && result.startsWith("\ufeff")) {
                    result = result.substring(1);
                }
                response.body().close();
                response.close();
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通用的上传图片 注：此方法暂时不可用，因为还没测试
     */
    public static void uploadImage(String reqUrl, HashMap<String, Object> params, String picKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
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
        RequestBody requestBody = multipartBodyBuilder.build();
        Request.Builder RequestBuilder = new Request.Builder();
        RequestBuilder.url(reqUrl);
        RequestBuilder.post(requestBody);
        Request request = RequestBuilder.build();

        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    //有时服务端返回json带了bom头，会导致解析异常
                    String result = response.body().string();
                    if (!TextUtils.isEmpty(result) && result.startsWith("\ufeff")) {
                        result = result.substring(1);
                    }
                }

            }
        });

    }

    /**
     * 同步下载文件：支持断点续传，必须在非UI线程下载
     *
     * @param fileUrl 下载文件URL地址
     */
    public static String syncDownloadFile(final String fileUrl) {
        return syncDownloadFile(fileUrl, null);
    }

    /**
     * 同步下载文件：支持断点续传，必须在非UI线程下载
     *
     * @param fileUrl      下载文件URL地址
     * @param downCallback 下载回调
     */
    private static String syncDownloadFile(final String fileUrl, final HttpDownloadCallback downCallback) {
        if (AppBaseUtils.isUiThread()) {
            throw new RuntimeException("Synchronized download file cannot be in UI thread");
        } else if (TextUtils.isEmpty(fileUrl)) {
            throw new NullPointerException("fileUrl is null");
        } else if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            String error = fileUrl + " is not a valid Url";
            FileHttpDownloadCallback.onFailure(error, downCallback, null);
            return null;
        }

        FileChannel fileChannel = null;
        FileLock fileLock = null;
        InputStream inputStream = null;

        try {
            final String downPath = getDownLoadFilePath(fileUrl);
            final String tempPath = downPath + ".temp";

            final File downFile = new File(downPath);
            final File tempFile = new File(tempPath);

            long downloadLength = 0;
            final long contentLength = getFileContentLength(fileUrl);

            if (downFile.exists() && downFile.length() == contentLength) {
                tempFile.delete();
                AppLogUtils.d(TAG, "The file already exist");
                FileHttpDownloadCallback.onFinished(downCallback, downPath);
                return downPath;
            }

            if (tempFile.exists()) {
                downloadLength = tempFile.length();
            }

            //====start 下载异常边界处理start:发生概率极低，不用太在意====
            if (downloadLength == contentLength) {
                boolean isSuccess = tempFile.renameTo(downFile);
                if (isSuccess) {
                    FileHttpDownloadCallback.onFinished(downCallback, downPath);
                    return downPath;
                } else {
                    if (tempFile.delete()) {
                        downloadLength = 0;
                    }
                }
            } else if (downloadLength > contentLength) {
                if (tempFile.delete()) {
                    downloadLength = 0;
                }
            }
            //====end 下载异常边界处理end:发生概率极低，不用太在意====

            final Request.Builder builder = new Request.Builder().url(fileUrl).get();
            final RandomAccessFile savedFile = new RandomAccessFile(tempFile, "rws");
            fileChannel = savedFile.getChannel();
            fileLock = fileChannel.tryLock();

            if (!fileLock.isValid()) {
                fileLock.close();
                fileChannel.close();
                String error = "the file is downloading";
                FileHttpDownloadCallback.onFailure(error, downCallback, new IOException(error));
                return null;
            }

            // 跳过已经下载的字节，实现断点续传
            if (downloadLength > 0) {
                savedFile.seek(downloadLength);

                // HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
                // 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
                // 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
                // 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
                if (contentLength > downloadLength) {
                    builder.addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength);
                }
            }

            //开始启动下载
            final Request request = builder.build();
            final Response response = mOkHttpClient.newCall(request).execute();
            if (response.body() == null) {
                FileHttpDownloadCallback.onFailure("null", downCallback, new NullPointerException("Response.body() is null"));
                return null;
            }
            if (!response.isSuccessful()) {
                response.body().close();
                response.close();
                fileLock.close();
                fileChannel.close();
                String error = response.message();
                FileHttpDownloadCallback.onFailure(error, downCallback, new Exception(response.message()));
                return null;
            }

            inputStream = response.body().byteStream();
            byte[] buffer = new byte[2048];
            int len;
            long sum = downloadLength;
            long timeStamp = System.currentTimeMillis();
            int lastProgress = 0;

            while ((len = inputStream.read(buffer)) != -1) {
                savedFile.write(buffer, 0, len);

                //计算下载进度
                if (downCallback != null) {
                    sum += len;
                    int progress1 = (int) (sum * 1.0f / contentLength * 100f);
                    if (lastProgress != progress1) {
                        lastProgress = progress1;
                        if (System.currentTimeMillis() - timeStamp > 1000L) {
                            timeStamp = System.currentTimeMillis();
                            float progress2 = (sum * 1f / contentLength);
                            progress2 = AppBaseUtils.formatFloat(progress2, 2);
                            FileHttpDownloadCallback.onProgress(downCallback, contentLength, sum, progress2);
                        }
                    }
                }
            }

            fileLock.close();
            fileChannel.close();
            inputStream.close();
            savedFile.close();
            response.body().close();
            response.close();
            fileLock = null;
            fileChannel = null;
            inputStream = null;

            //下载完成后把.temp的文件重命名为原文件
            if (tempFile.exists() && tempFile.length() == contentLength) {
                boolean isSuccess = tempFile.renameTo(downFile);
                if (isSuccess) {
                    FileHttpDownloadCallback.onFinished(downCallback, downPath);
                    return downPath;
                }
            } else {
                String error = "downloaded failed:tempFile.length() != contentLength";
                FileHttpDownloadCallback.onFailure(error, downCallback, new Exception(error));
                return null;
            }
        } catch (Throwable t) {
            try {
                if (fileLock != null) {
                    fileLock.close();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            t.printStackTrace();
            String error = t.toString();
            AppLogUtils.w(TAG, "download failed:" + error);
            FileHttpDownloadCallback.onFailure(error, downCallback, new Exception(error));
            return null;
        }

        // 兜底的下载callback
        String error = "download failed:unknown";
        FileHttpDownloadCallback.onFailure(error, downCallback, new Exception(error));
        return null;
    }

    /**
     * 异步下载文件的方法：支持断点续传
     *
     * @param fileUrl          下载文件地址
     * @param downloadCallback 下载的回调监听
     */
    public static void downloadFile(final String fileUrl, final HttpDownloadCallback downloadCallback) {
        if (downloadCallback == null) {
            throw new NullPointerException("HttpDownloadCallback 不能为空");
        } else if (TextUtils.isEmpty(fileUrl)) {
            downloadCallback.onFinished(false, null, new NullPointerException("下载URL不能为空"));
            return;
        } else if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            downloadCallback.onFinished(false, null, new IOException(fileUrl + " 不是有效的URL"));
            return;
        }

        AppThreadPoolExecutor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                syncDownloadFile(fileUrl, downloadCallback);
            }
        });
    }

    /**
     * 获取被下载的文件的长度
     */
    private static long getFileContentLength(String downloadUrl) {
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.body() == null) {
                response.close();
                return 0;
            }
            if (response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                response.close();
                return contentLength;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    private static class FileHttpDownloadCallback {
        public static void onFinished(HttpDownloadCallback downloadCallback, String filePath) {
            if (downloadCallback != null) {
                AppBaseUtils.getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.onFinished(true, filePath, null);
                    }
                });
            }
        }

        public static void onProgress(HttpDownloadCallback downloadCallback, long fileTotalSize, long fileDowningSize, float percent) {
            if (downloadCallback != null) {
                AppBaseUtils.getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.onProgress(fileTotalSize, fileDowningSize, percent);
                    }
                });
            }
        }

        public static void onFailure(String error, HttpDownloadCallback downloadCallback, Exception e) {
            if (e != null) {
                error = error + ":" + e;
            }
            AppLogUtils.w(TAG, error);
            if (downloadCallback != null) {
                AppBaseUtils.getUiHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.onFinished(false, null, e);
                    }
                });
            }
        }
    }

    /**
     * 通用字段
     */
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

    /**
     * 构建get请求参数
     */
    public static String buildGetParams(String url, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        addCommonData(params);

        StringBuilder params2 = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (params2.length() <= 0 && !url.contains("?")) {
                params2.append("?").append(key).append("=").append(value);
            } else {
                params2.append("&").append(key).append("=").append(value);
            }
        }

        if (!TextUtils.isEmpty(url)) {
            return url + params2;
        } else {
            return params2.toString();
        }
    }

    /**
     * 根据下载文件URL得到文件的后缀名
     */
    private static String getSuffixNameByHttpUrl(final String url) {
        int index = url.lastIndexOf(".");
        if (index > 0) {
            return url.substring(index);
        }
        return "";
    }

    /**
     * 根据下载文件URL得到文件的下载路径
     */
    public static String getDownLoadFilePath(String fileUrl) {
        return HTTP_DOWNLOAD_PATH + File.separator + AppBaseUtils.MD5(fileUrl).toLowerCase() + getSuffixNameByHttpUrl(fileUrl);
    }

    public static class HttpCallback2 {
        public static void onResponse(final HttpCallback httpCallback, boolean isSuccessful, String result) {
            AppBaseUtils.getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    httpCallback.onResponse(true, result);
                }
            });
        }
    }

}
