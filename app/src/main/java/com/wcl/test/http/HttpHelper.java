package com.wcl.test.http;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HttpHelper
 * 与 HTTP 协议无关的工具集合
 */
class HttpHelper {

    /**
     * 异步下载文件（多线程切块下载 + 断点续传 + 进度按1%回调）
     *
     * @param url      文件下载地址
     * @param callback 下载回调（主线程）
     */
    public static void fastDownload(String url, HttpUtils.DownloadCallback callback) {
        if (!HttpHelper.isValidUrl(url)) {
            callback.onFinished(false, null, "Invalid URL");
            return;
        }

        // 正在下载的不再重复下载
        if (!HttpUtils.DOWNLOADING_URLS.add(url)) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, "file is downloading"));
            return;
        }

        new Thread(() -> {
            try {
                File target = new File(HttpHelper.getDownloadPath(url));
                File tempDir = new File(target.getAbsolutePath() + "_tmp");
                tempDir.mkdirs();

                long totalLength = HttpHelper.fetchContentLength(url);
                if (totalLength <= 0) {
                    HttpHelper.postToUi(() -> callback.onFinished(false, null, "无法获取文件大小"));
                    return;
                }

                // 小文件直接使用普通下载
                if (totalLength < 50L * 1024 * 1024) {
                    HttpUtils.download(url, callback);
                    return;
                }

                // 文件已经完整下载
                if (target.exists() && totalLength == target.length()) {
                    HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));
                    return;
                }

                int threadCount = 4;
                long blockSize = totalLength / threadCount;

                Thread[] threads = new Thread[threadCount];
                AtomicLong downloaded = new AtomicLong(0);
                AtomicInteger lastPercent = new AtomicInteger(0);

                for (int i = 0; i < threadCount; i++) {
                    long start = i * blockSize;
                    long end = (i == threadCount - 1) ? totalLength - 1 : (start + blockSize - 1);
                    int index = i;

                    threads[i] = new Thread(() -> {
                        File partFile = new File(tempDir, "part_" + index);

                        long existingLength = partFile.exists() ? partFile.length() : 0;
                        long rangeStart = start + existingLength; // 支持断点续传

                        if (rangeStart > end) {
                            downloaded.addAndGet(end - start + 1);
                            return;
                        }

                        try (RandomAccessFile raf = new RandomAccessFile(partFile, "rw")) {
                            raf.seek(existingLength);

                            Request request = new Request.Builder()
                                    .url(url)
                                    .addHeader("Range", "bytes=" + rangeStart + "-" + end)
                                    .build();

                            try (Response response = HttpUtils.CLIENT.newCall(request).execute()) {
                                if (!response.isSuccessful()) return;

                                try (InputStream in = response.body().byteStream()) {
                                    byte[] buffer = new byte[8192];
                                    int len;
                                    while ((len = in.read(buffer)) != -1) {
                                        raf.write(buffer, 0, len);

                                        // 每下载1%回调一次下载进度
                                        long curDownloaded = downloaded.addAndGet(len);
                                        int percent = (int) ((curDownloaded * 100) / totalLength);
                                        int last = lastPercent.get();
                                        if (percent > last) {
                                            if (lastPercent.compareAndSet(last, percent)) {
                                                HttpHelper.postToUi(() ->
                                                        callback.onProgress(totalLength, curDownloaded, percent)
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    threads[i].start();
                }

                // 等待所有线程完成
                for (Thread t : threads) t.join();

                // 合并文件
                try (RandomAccessFile out = new RandomAccessFile(target, "rw")) {
                    byte[] buffer = new byte[8192];
                    for (int i = 0; i < threadCount; i++) {
                        File partFile = new File(tempDir, "part_" + i);
                        try (RandomAccessFile partRaf = new RandomAccessFile(partFile, "r")) {
                            int len;
                            while ((len = partRaf.read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                            }
                        }
                        partFile.delete();
                    }
                }
                tempDir.delete();

                // 下载完成回调
                HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));

            } catch (Throwable t) {
                t.printStackTrace();
                HttpHelper.postToUi(() -> callback.onFinished(false, null, t.toString()));
            } finally {
                HttpUtils.DOWNLOADING_URLS.remove(url);
            }
        }).start();
    }

    static void downloadInternal(String url, HttpUtils.DownloadCallback callback) {
        long totalLength = HttpHelper.fetchContentLength(url);

        if (totalLength <= 0) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, "无法获取文件大小"));
            return;
        }

        // 检查文件是否已经完整下载，如果已经被下载成功则直接返回file path
        File downFile = new File(HttpHelper.getDownloadPath(url));
        if (downFile.exists() && totalLength == downFile.length()) {
            HttpHelper.postToUi(() -> callback.onFinished(true, downFile.getAbsolutePath(), null));
            return;
        }

        if (!HttpUtils.DOWNLOADING_URLS.add(url)) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, "file is downloading"));
            return;
        }

        File target = new File(HttpHelper.getDownloadPath(url));
        File temp = new File(target.getAbsolutePath() + ".temp");
        long downloaded = temp.exists() ? temp.length() : 0;

        Request.Builder builder = new Request.Builder().url(url);
        if (downloaded > 0) {
            builder.addHeader("Range", "bytes=" + downloaded + "-");
        }

        try (Response response = HttpUtils.CLIENT.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                HttpHelper.postToUi(() -> callback.onFinished(false, null, "download fail: " + response));
                return;
            }

            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(temp, true)) {
                byte[] buffer = new byte[4096];
                int len;
                long sum = downloaded;
                int lastPercent = (int) ((sum * 100) / totalLength);

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    sum += len;
                    // 每下载1%回调一次下载进度
                    int percent = (int) ((sum * 100) / totalLength);
                    if (percent > lastPercent) {
                        lastPercent = percent;
                        long curSum = sum;
                        HttpHelper.postToUi(() -> callback.onProgress(totalLength, curSum, percent));
                    }
                }
            }

            HttpHelper.replaceFile(temp, target);
            HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));
        } catch (Throwable t) {
            HttpHelper.postToUi(() -> callback.onFinished(false, null, t.toString()));
        } finally {
            HttpUtils.DOWNLOADING_URLS.remove(url);
        }
    }

    // 如果是有效url则返回true
    static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"));
    }

    static void postToUi(Runnable r) {
        AppBaseUtils.getUiHandler().post(r);
    }

    static void addCommonParams(Map<String, Object> params) {
        params.put("deviceId", DeviceUtils.getDeviceId());
        params.put("product", Build.MODEL);
        params.put("brand", Build.BRAND);
        params.put("os_int", Build.VERSION.SDK_INT);
        params.put("os_release", Build.VERSION.RELEASE);
        params.put("appCode", AppBaseUtils.getVerCode());
        params.put("appName", AppBaseUtils.getVerName());
        params.put("channel", AppBaseUtils.getChannel());
        params.put("pkg", AppBaseUtils.getPackageName());
        params.put("os", "android");
    }

    static FormBody buildFormBody(Map<String, Object> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> e : params.entrySet()) {
            builder.add(e.getKey(), String.valueOf(e.getValue()));
        }
        return builder.build();
    }

    static String buildGetUrl(String url, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(url.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            if (!first) sb.append("&");
            sb.append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    // ======================= Download =======================

    // 获取文件下载路径
    static String getDownloadPath(String url) {
        File dir = BaseApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, AppBaseUtils.md5(url).toLowerCase() + getSuffix(url)).getAbsolutePath();
    }

    // 从url获取文件长度
    static long fetchContentLength(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = HttpUtils.CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().contentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static void replaceFile(File src, File dest) throws Exception {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
        src.delete();
    }

    private static String getSuffix(String url) {
        int i = url.lastIndexOf(".");
        return i > 0 ? url.substring(i) : "";
    }

    /**
     * 去除字符串开头的 UTF-8 BOM 字符
     */
    static String removeUtf8Bom(String input) {
        if (TextUtils.isEmpty(input)) return input;
        return input.charAt(0) == '\ufeff' ? input.substring(1) : input;
    }
}
