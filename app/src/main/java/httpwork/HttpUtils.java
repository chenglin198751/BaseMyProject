package httpwork;

import android.app.Activity;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.Constants;

/**
 * Created by chenglin on 2017-5-24.
 */

public class HttpUtils {
    public static final OkHttpClient client = new OkHttpClient();

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
