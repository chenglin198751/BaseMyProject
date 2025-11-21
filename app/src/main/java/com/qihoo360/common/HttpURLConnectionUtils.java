package com.qihoo360.common;

import com.qihoo.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpURLConnectionUtils {
    private static final String TAG = "HttpURLConnectionUtil";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public interface HttpCallback {
        void onSuccess(String response);

        void onError(Exception e);
    }

    /**
     * 发送 GET 请求
     */
    public static void get(String urlStr, HttpCallback callback) {
        LogUtils.v("HttpURLConnectionUtil", "请求开始:url=" + urlStr);
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                InputStream in = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
                String result = readStream(in);

                if (code == 200) {
                    LogUtils.v(TAG, "请求成功:code=" + code);
                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                } else {
                    String msg = "HTTP " + code + ": " + result;
                    LogUtils.v(TAG, "请求失败:" + msg);
                    if (callback != null) {
                        callback.onError(new IOException(msg));
                    }
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    /**
     * 发送 POST 请求
     */
    public static void post(String urlStr, String body, HttpCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // 发送请求体
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                InputStream in = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
                String result = readStream(in);

                if (code == 200) {
                    if (callback != null) callback.onSuccess(result);
                } else {
                    if (callback != null)
                        callback.onError(new IOException("HTTP " + code + ": " + result));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    // 工具：读取输入流
    private static String readStream(InputStream in) throws IOException {
        if (in == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString().trim();
    }
}
