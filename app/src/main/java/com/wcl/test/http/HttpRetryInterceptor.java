package com.wcl.test.http;

import androidx.annotation.NonNull;

import com.wcl.test.utils.AppLogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 简单重试拦截器（仅在请求失败时重试）
 */
record HttpRetryInterceptor(int maxRetry) implements Interceptor {
    private static final String TAG = "HttpRetryInterceptor";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        long start = System.currentTimeMillis();
        Request request = chain.request();
        Response response = chain.proceed(request);
        int retry = 0;
        while (isRetryable(request, response) && retry < maxRetry) {
            retry++;
            response.close();
            response = chain.proceed(request);
        }
        long end = System.currentTimeMillis();
        AppLogUtils.v(TAG, "cost:" + (end - start) + "ms" + ",retry:" + retry + ",url:" + request.url());
        return response;
    }

    private static boolean isRetryable(Request request, Response response) {
        if (!("GET".equals(request.method()) || "HEAD".equals(request.method()))) {
            return false;
        }
        int code = response.code();
        return code == 408 || code >= 500;
    }
}