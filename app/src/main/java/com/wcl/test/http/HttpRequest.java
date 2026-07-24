package com.wcl.test.http;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wcl.test.EnvToggle;
import com.wcl.test.utils.AppLogUtils;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class HttpRequest {
    private static final String TAG = "HttpRequest";

    static final OkHttpClient CLIENT;
    static final Set<String> DOWNLOADING_URLS = ConcurrentHashMap.newKeySet();

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new HttpRetryInterceptor(1));

        if (!EnvToggle.isDebug()) {
            builder.proxy(Proxy.NO_PROXY);
        }
        CLIENT = builder.build();
    }

    /**
     * 统一的异步请求入口（Fragment 专用），自动切换到主线程回调
     */
    static void enqueue(Fragment fragment, Request request, OkHttpExecutor.HttpCallback callback) {
        enqueue(() -> LiteHelper.isFragmentAlive(fragment), request, callback);
    }

    /**
     * 统一的异步请求入口，自动切换到主线程回调
     */
    static void enqueue(Context context, Request request, OkHttpExecutor.HttpCallback callback) {
        enqueue(() -> LiteHelper.isActivityAlive(context), request, callback);
    }

    /**
     * 核心异步请求入口，自动切换到主线程回调
     *
     * @param isAlive 存活检测器，返回 false 时静默丢弃回调（用于 Activity/Fragment 生命周期安全判断）
     */
    static void enqueue(BooleanSupplier isAlive, Request request, OkHttpExecutor.HttpCallback callback) {
        CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!isAlive.getAsBoolean()) return;
                LiteHelper.postToUi(() -> {
                    if (isAlive.getAsBoolean()) {
                        notifyResult(callback, false, e.toString());
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (response) {
                    if (!isAlive.getAsBoolean()) return;

                    boolean ok = response.isSuccessful();
                    String responseContent;
                    try {
                        responseContent = response.body().string();
                    } catch (IOException e) {
                        postFailure(isAlive, callback, e.toString());
                        return;
                    }
                    final String result = LiteHelper.removeUtf8Bom(ok ? responseContent : response.toString());
                    AppLogUtils.i(TAG, "result:" + result);
                    LiteHelper.postToUi(() -> {
                        if (isAlive.getAsBoolean()) {
                            notifyResult(callback, ok, result);
                        }
                    });
                }
            }
        });
    }

    private static void postFailure(BooleanSupplier isAlive, OkHttpExecutor.HttpCallback callback, String error) {
        if (!isAlive.getAsBoolean()) return;
        LiteHelper.postToUi(() -> {
            if (isAlive.getAsBoolean()) {
                notifyResult(callback, false, error);
            }
        });
    }

    static Map<String, Object> withCommonParams(Map<String, Object> params) {
        Map<String, Object> finalParams = params == null ? new HashMap<>() : new HashMap<>(params);
        LiteHelper.addCommonParams(finalParams);
        return finalParams;
    }

    static void notifyResult(OkHttpExecutor.HttpCallback callback, boolean success, String result) {
        if (callback != null) {
            callback.onResult(success, result);
        }
    }

    /**
     * 构建支持 Header 的 Request.Builder
     */
    static Request.Builder buildRequest(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        if (headers != null && !headers.isEmpty()) {
            builder.headers(Headers.of(headers));
        }
        return builder;
    }

}
