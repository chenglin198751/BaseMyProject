package com.wcl.test.http;

import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

class Downloader {

    private static final int PROGRESS_INTERVAL = 500;

    static void downloadInternal(String url, OkHttpExecutor.DownloadCallback callback) {
        downloadInternal(url, callback, false);
    }

    static void downloadInternal(String url, OkHttpExecutor.DownloadCallback callback, boolean claimed) {
        File target = new File(LiteHelper.getDownloadPath(url));
        long totalLength = LiteHelper.fetchContentLength(url);
        if (totalLength <= 0) {
            LiteHelper.notifyDownloadFailure(callback, "无法获取文件信息");
            return;
        }

        if (target.exists() && target.length() == totalLength) {
            LiteHelper.postSuccess(callback, target.getAbsolutePath());
            return;
        }
        if (!claimed && !HttpRequestHelper.DOWNLOADING_URLS.add(url)) {
            LiteHelper.notifyDownloadFailure(callback, "file is downloading");
            return;
        }

        File temp = new File(target.getAbsolutePath() + ".temp");
        if (temp.length() > totalLength && !temp.delete()) {
            LiteHelper.notifyDownloadFailure(callback, "无法重置临时文件");
            HttpRequestHelper.DOWNLOADING_URLS.remove(url);
            return;
        }

        long downloaded = temp.exists() ? temp.length() : 0;
        try {
            if (downloaded == totalLength) {
                if (!LiteHelper.replaceFile(temp, target) || target.length() != totalLength) {
                    throw new IllegalStateException("完整临时文件替换失败");
                }
                LiteHelper.postSuccess(callback, target.getAbsolutePath());
                return;
            }

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Encoding", "identity");
            if (downloaded > 0) {
                builder.addHeader("Range", "bytes=" + downloaded + "-");
            } else {
                downloaded = 0;
            }

            try (Response response = HttpRequestHelper.CLIENT.newCall(builder.build()).execute()) {
                if (downloaded > 0 && response.code() == 200) {
                    downloaded = 0;
                    if (temp.exists() && !temp.delete()) {
                        throw new IllegalStateException("无法重置临时文件");
                    }
                } else if (downloaded > 0) {
                    LiteHelper.ContentRange range = LiteHelper.parseContentRange(response.header("Content-Range"));
                    if (response.code() != 206 || range == null || range.start() != downloaded
                            || range.end() != totalLength - 1 || range.total() != totalLength) {
                        throw new IllegalStateException("断点响应不匹配");
                    }
                } else if (response.code() != 200) {
                    throw new IllegalStateException("下载响应错误: " + response.code());
                }

                long expected = totalLength - downloaded;
                try (InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(temp, downloaded > 0)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    long received = 0;
                    long sum = downloaded;
                    long lastCallbackTime = 0;
                    while ((len = in.read(buffer)) != -1) {
                        if (received + len > expected) {
                            throw new IllegalStateException("下载响应超出文件长度");
                        }
                        out.write(buffer, 0, len);
                        received += len;
                        sum += len;
                        long now = System.currentTimeMillis();
                        if (now - lastCallbackTime >= PROGRESS_INTERVAL) {
                            lastCallbackTime = now;
                            long current = sum;
                            float percent = AppUtils.formatFloat((current * 100f) / totalLength, 2);
                            LiteHelper.postToUi(() -> {
                                if (callback != null) callback.onProgress(totalLength, current, percent);
                            });
                        }
                    }
                    if (received != expected || temp.length() != totalLength) {
                        throw new IllegalStateException("下载文件长度错误");
                    }
                }
            }

            if (!LiteHelper.replaceFile(temp, target) || target.length() != totalLength) {
                throw new IllegalStateException("下载文件替换失败");
            }
            LiteHelper.postSuccess(callback, target.getAbsolutePath());
        } catch (Exception e) {
            LiteHelper.notifyDownloadFailure(callback, e.toString());
        } finally {
            HttpRequestHelper.DOWNLOADING_URLS.remove(url);
        }
    }
}
