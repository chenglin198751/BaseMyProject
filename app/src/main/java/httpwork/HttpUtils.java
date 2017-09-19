package httpwork;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.MyUtils;

/**
 * Created by chenglin on 2017-5-24.
 */

public class HttpUtils {
    public static final OkHttpClient client = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    /**
     * 通用的post请求，当Activity finish后，不会再返回请求结果
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
        call.enqueue(new Callback() {
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
                }
            }
        });
    }

    /**
     * 通用的get请求，当Activity finish后，不会再返回请求结果
     */
    public static void get(final Activity activity, final String url, final HttpCallback httpCallback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
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
                }
            }
        });
    }


    /**
     * 通用的上传图片
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
        client.newCall(request).enqueue(new Callback() {
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
}
