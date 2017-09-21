package httpwork;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.MyUtils;
import utils.SDCardUtils;

/**
 * Created by chenglin on 2017-5-24.
 * 资料：http://liuwangshu.cn/application/network/6-okhttp3.html
 */

public class HttpUtils {
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private static final int TIME_OUT = 30;

    private static final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS);
    private static final OkHttpClient client = builder.build();

    /**
     * 通用的异步post请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void post(final Activity activity, String url, HashMap<String, Object> hashMap, final HttpCallback httpCallback) {
        FormBody.Builder FormBuilder = new FormBody.Builder();

        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        addCommonData(hashMap);

        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            FormBuilder.add(key + "", value + "");
        }
        RequestBody body = FormBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onFailure(e);
                        }
                    });
                } else {
                    call.cancel();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onSuccess(result);
                        }
                    });
                } else {
                    call.cancel();
                }
            }
        });
    }

    /**
     * 通用的异步get请求，为了防止内存泄露：当Activity finish后，不会再返回请求结果
     */
    public static void get(final Activity activity, final String url, final HttpCallback httpCallback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onFailure(e);
                        }
                    });
                } else {
                    call.cancel();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onSuccess(result);
                        }
                    });
                } else {
                    call.cancel();
                }
            }
        });
    }

    /**
     * 通用的上传图片 注：此方法暂时不可用，因为还没测试
     */
    public void uploadImage(String reqUrl, HashMap<String, Object> params, String picKey, String filePath) {
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
        if (params != null) {
            for (String key : params.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, params.get(key) + "");
            }
        }

        //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key
        multipartBodyBuilder.addFormDataPart(picKey, file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));

        //构建请求体
        RequestBody requestBody = multipartBodyBuilder.build();
        Request.Builder RequestBuilder = new Request.Builder();
        RequestBuilder.url(reqUrl);
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
     * 可以自定义下载路径的通用的异步下载文件的方法，返回文件下载成功之后的所在路径，不支持断点续传
     * 注意：不建议在 Activity 里开启下载，因为很容易造成内存泄漏，建议放到 service 或者 intentService 里面
     */
    public static void downloadFile(final String fileUrl, final HttpDownloadCallback downloadCallback) {
        downloadFile(fileUrl, null, downloadCallback);
    }

    /**
     * 默认的下载路径的通用的异步下载文件的方法，返回文件下载成功之后的所在路径，不支持断点续传
     * 注意：不建议在 Activity 里开启下载，因为很容易造成内存泄漏，建议放到 service 或者 intentService 里面
     */
    public static void downloadFile(final String fileUrl, final String fileDownloadPath, final HttpDownloadCallback downloadCallback) {
        Request request = new Request.Builder().url(fileUrl).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                MyUtils.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) {
                final String defaultFilePath = SDCardUtils.SDCARD_PATH + System.currentTimeMillis() + getSuffixNameByHttpUrl(fileUrl);
                final String filePath = TextUtils.isEmpty(fileDownloadPath) ? defaultFilePath : fileDownloadPath;

                if (downloadCallback != null) {
                    MyUtils.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            downloadCallback.onStart(filePath);
                        }
                    });
                }

                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;

                try {
                    fileOutputStream = new FileOutputStream(new File(filePath));
                    long total = response.body().contentLength();
                    byte[] buffer = new byte[2048];
                    int len;
                    long sum = 0;
                    int lastProgress = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100f);
                        if (downloadCallback != null) {
                            if (lastProgress != progress) {
                                lastProgress = progress;

                                final long tempTotal = total;
                                final long tempSum = sum;
                                final int tempProgress = progress;
                                MyUtils.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadCallback.onProgress(call, tempTotal, tempSum, tempProgress);
                                    }
                                });
                            }
                        }
                    }
                    fileOutputStream.flush();
                    inputStream.close();
                    fileOutputStream.close();
                    if (downloadCallback != null) {
                        MyUtils.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                downloadCallback.onSuccess(filePath);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 通用字段
     */
    private static void addCommonData(HashMap<String, Object> params) {
        params.put("IMEI", MyUtils.getDeviceId());
        params.put("product", Build.MODEL);
        params.put("sdkVer", Build.VERSION.SDK_INT);
        params.put("appVer", MyUtils.getVerCode());
        params.put("appVerName", MyUtils.getVerName());
        params.put("phone", "android");
        params.put("channel", MyUtils.getChannel());
        params.put("packageName", MyUtils.getPackageName());
    }


    /**
     * 构建get请求参数
     */
    public static String buildGetParams(HashMap<String, Object> hashMap) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        addCommonData(hashMap);

        StringBuilder params = new StringBuilder();
        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (params.length() <= 0) {
                params.append("?" + key + "=" + value);
            } else {
                params.append("&" + key + "=" + value);
            }
        }

        return params.toString();
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
}
