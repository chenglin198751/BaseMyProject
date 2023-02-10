package com.wcl.test.httpwork;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.DeviceUtils;
import com.wcl.test.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        void onSuccess(String filePath);

        //fileTotalSize  文件总大小
        //fileDowningSize  文件已经下载的大小
        //percent  文件下载的进度百分比
        void onProgress(Call call, long fileTotalSize, long fileDowningSize, float percent);

        void onFailure(IOException e);
    }

    //用于扩展一些网络功能，比如写入header,写入cookies等操作
    public static class HttpBuilder {
        public Map<String, String> headersMap = null;
    }

    private final static String TAG = "HttpUtils";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int TIME_OUT = 15;
    private static final String HTTP_DOWNLOAD_PATH = FileUtils.getExternalPath() + "/download";
    private static final OkHttpClient.Builder builder;
    public static final OkHttpClient client;

    static {
        File downloadDir = new File(HTTP_DOWNLOAD_PATH);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        builder = new OkHttpClient
                .Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(1));

        if (!EnvToggle.isDebug()) {
            //禁用抓包工具抓包
            builder.proxy(Proxy.NO_PROXY);
        }
        client = builder.build();
    }

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
            public void onFailure(Call call, final IOException e) {
                if (context == null) {
                    return;
                }
                e.printStackTrace();

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                httpCallback.onResponse(false, e.toString());
                            }
                        });
                    }
                } else {
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onResponse(false, e.toString());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (context == null) {
                    return;
                }

                if (response.code() != HttpURLConnection.HTTP_OK) {
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onResponse(false, response.toString());
                        }
                    });
                    return;
                }

                //有时服务端返回json带了bom头，会导致解析生效
                String tempStr = response.body().string();
                if (!TextUtils.isEmpty(tempStr) && tempStr.startsWith("\ufeff")) {
                    tempStr = tempStr.substring(1);
                }

                final String result = tempStr;

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        handleHttpSuccessOnUiThread(httpCallback, result);
                    }
                } else {
                    handleHttpSuccessOnUiThread(httpCallback, result);
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
        final CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        cacheBuilder.noCache();//不使用缓存，全部走网络
        cacheBuilder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = cacheBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().cacheControl(cache).url(url).post(body);
        if (builder.headersMap != null && builder.headersMap.size() > 0) {
            requestBuilder.headers(Headers.of(builder.headersMap));
        }
        Request request = requestBuilder.build();

        Call call = client.newCall(request);
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
        final CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        cacheBuilder.noCache();//不使用缓存，全部走网络
        cacheBuilder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = cacheBuilder.build();

        final String url2 = buildGetParams(url, params);
        Request.Builder requestBuilder = new Request.Builder().cacheControl(cache).url(url2).get();
        if (builder.headersMap != null && builder.headersMap.size() > 0) {
            requestBuilder.headers(Headers.of(builder.headersMap));
        }
        Request request = requestBuilder.build();

        Call call = client.newCall(request);
        call.enqueue(createOkhttp3Callback(context, httpCallback));
    }

    /**
     * 同步get请求，必须放到线程中
     */
    public static String syncGet(final String url) {
        final CacheControl.Builder builder = new CacheControl.Builder();
        builder.noCache();//不使用缓存，全部走网络
        builder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = builder.build();
        Request request = new Request.Builder().cacheControl(cache).url(url).get().build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpURLConnection.HTTP_OK) {
                //有时服务端返回json带了bom头，会导致解析生效
                String tempStr = response.body().string();
                if (!TextUtils.isEmpty(tempStr) && tempStr.startsWith("\ufeff")) {
                    tempStr = tempStr.substring(1);
                }
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

        final CacheControl.Builder cacheBuilder = new CacheControl.Builder();
        cacheBuilder.noCache();//不使用缓存，全部走网络
        cacheBuilder.noStore();//不使用缓存，也不存储缓存
        CacheControl cache = cacheBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .cacheControl(cache)
                .url(url)
                .post(body);
        Request request = requestBuilder.build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpURLConnection.HTTP_OK) {
                //有时服务端返回json带了bom头，会导致解析生效
                String tempStr = response.body().string();
                if (!TextUtils.isEmpty(tempStr) && tempStr.startsWith("\ufeff")) {
                    tempStr = tempStr.substring(1);
                }
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
        } else if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            downloadCallback.onFailure(new IOException(fileUrl + " 不是有效的URL"));
            return;
        }

        if (!TextUtils.isEmpty(downPath)) {
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
        }

        final String defaultPath = HTTP_DOWNLOAD_PATH + File.separator + BaseUtils.MD5(fileUrl).toLowerCase() + getSuffixNameByHttpUrl(fileUrl);
        final String downLoadFilePath = TextUtils.isEmpty(downPath) ? defaultPath : downPath;
        final String tempPath = downLoadFilePath + ".temp";

        //防止下载时中断导致下载文件不全,但被使用了
        File tempFile = new File(tempPath);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        File cacheFile = new File(downLoadFilePath);
        if (cacheFile.exists()) {
            if (isNeedCache && cacheFile.length() > 0) {
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
            public void onResponse(final Call call, final Response response) {
                if (response.code() != HttpURLConnection.HTTP_OK) {
                    BaseUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            downloadCallback.onFailure(new IOException("下载失败：" + response.toString()));
                        }
                    });
                    return;
                }

                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;

                try {
                    fileOutputStream = new FileOutputStream(new File(tempPath));
                    long total = response.body().contentLength();
                    byte[] buffer = new byte[2048];
                    int len;
                    long sum = 0;
                    long timeStamp = System.currentTimeMillis();
                    int lastProgress = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100f);
                        if (lastProgress != progress) {
                            lastProgress = progress;

                            final long tempTotal = total;
                            final long tempSum = sum;

                            if (System.currentTimeMillis() - timeStamp > 1000L) {
                                timeStamp = System.currentTimeMillis();
                                BaseUtils.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        float progress = (tempSum * 1f / tempTotal);
                                        progress = BaseUtils.formatFloat(progress, 4);
                                        downloadCallback.onProgress(call, tempTotal, tempSum, progress);
                                    }
                                });
                            }
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
                                    downloadCallback.onProgress(call, tempFile.length(), tempFile.length(), 100f);
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
     * 可以自定义下载路径的通用的同步下载文件的方法，返回文件下载成功之后的所在路径，支持断点续传
     *
     * @param fileUrl     下载文件URL地址
     * @param isNeedCache 是否需要缓存，如果true ，那么此文件如果已经下载，就不再重复下载
     */
    public static String syncDownloadFile(final String fileUrl, boolean isNeedCache) {
        if (BaseUtils.isUiThread()) {
            throw new RuntimeException("Synchronized download file cannot be in UI thread");
        } else if (TextUtils.isEmpty(fileUrl)) {
            throw new RuntimeException("fileUrl is null");
        }

        try {
            final String downPath = HTTP_DOWNLOAD_PATH + File.separator + BaseUtils.MD5(fileUrl).toLowerCase() + getSuffixNameByHttpUrl(fileUrl);
            final String tempPath = downPath + ".temp";

            File downFile = new File(downPath);
            File tempFile = new File(tempPath);

            if (downFile.exists()) {
                if (isNeedCache) {
                    return downPath;
                } else {
                    downFile.delete();
                }
            }

            if (isFileDownloading(tempFile)) {
                AppLogUtils.w(TAG, fileUrl + " is being downloaded, do not download it again");
                return null;
            }

            long downloadLength = 0;
            final long contentLength = getContentLength(fileUrl);
            if (tempFile.exists()) {
                downloadLength = tempFile.length();
            }
            if (downloadLength == contentLength) {
                boolean isSuccess = tempFile.renameTo(downFile);
                if (isSuccess) {
                    return downPath;
                } else {
                    if (tempFile.delete()) {
                        downloadLength = 0;
                    }
                }
            }

            Request.Builder builder = new Request.Builder().url(fileUrl).get();
            RandomAccessFile savedFile = new RandomAccessFile(tempFile, "rws");

            //跳过已经下载的字节，实现断点续传
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
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.body().close();
                return null;
            }

            InputStream inputStream = response.body().byteStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                savedFile.write(buffer, 0, len);
            }
            inputStream.close();
            savedFile.close();
            response.body().close();

            //下载完成后把.temp的文件重命名为原文件
            if (tempFile.exists()) {
                boolean isSuccess = tempFile.renameTo(downFile);
                if (isSuccess) {
                    return downPath;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            AppLogUtils.w(TAG, "download failed:" + t);
        }
        return null;
    }

    /**
     * 获取被下载的文件的长度
     */
    private static long getContentLength(String downloadUrl) {
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断文件是否下载中
     */
    private static boolean isFileDownloading(File tempFile) {
        boolean isDownloading = false;
        if (tempFile != null && tempFile.exists()) {
            long lastModified = tempFile.lastModified();
            long current = System.currentTimeMillis();
            //如果3秒内文件有被更改，那么证明正在下载中
            if (current - lastModified <= 3 * 1000) {
                isDownloading = true;
            }
        }
        return isDownloading;
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
        params.put("appVer", BaseUtils.getVerCode());
        params.put("appVerName", BaseUtils.getVerName());
        params.put("phone", "android");
        params.put("channel", BaseUtils.getChannel());
        params.put("packageName", BaseUtils.getPackageName());
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
                params2.append("?" + key + "=" + value);
            } else {
                params2.append("&" + key + "=" + value);
            }
        }

        if (!TextUtils.isEmpty(url)) {
            return url + params2.toString();
        } else {
            return params2.toString();
        }
    }

    /**
     * 根据下载文件地址得到文件的后缀名
     */
    private static String getSuffixNameByHttpUrl(final String url) {
        int index = url.lastIndexOf(".");
        if (index > 0) {
            return url.substring(index);
        }
        return "";
    }

    private static void handleHttpSuccessOnUiThread(final HttpCallback httpCallback, final String result) {
        BaseUtils.getHandler().post(new Runnable() {
            @Override
            public void run() {
                httpCallback.onResponse(true, result);

//                //这里try catch的唯一目的就是防止在回调结果时，json解析错误、之类的crash没处理。
//                //如果你觉得回调结果的crash不要try，要直接暴露，你可以注释调我这里的try catch
//                try {
//                    httpCallback.onResponse(true, result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    httpCallback.onResponse(false, e.toString());
//                }
            }
        });
    }

}
