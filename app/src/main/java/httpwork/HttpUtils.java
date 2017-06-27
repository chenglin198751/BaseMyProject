package httpwork;

import android.app.Activity;
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

        if (hashMap.size() > 0) {
            Iterator iter = hashMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                FormBuilder.add(key + "", value + "");
            }
        }
        RequestBody body = FormBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (activity != null && !activity.isFinishing()) {
                    httpCallback.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (activity != null && !activity.isFinishing()) {
                    httpCallback.onSuccess(result);
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
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (activity != null && !activity.isFinishing()) {
                    httpCallback.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (activity != null && !activity.isFinishing()) {
                    httpCallback.onSuccess(result);
                }
            }
        });
    }


    /**
     * 通用的上传图片
     */
    public void uploadImage(String reqUrl, Map<String, String> params, String picKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return;
        }
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);

        //遍历map中所有参数到builder
        if (params != null) {
            for (String key : params.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, params.get(key));
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
     * 构建get请求参数
     */
    public static String buildGetParams(HashMap<String, Object> hashMap) {
        StringBuilder params = new StringBuilder();
        if (hashMap.size() > 0) {
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
        }
        return params.toString();
    }
}
