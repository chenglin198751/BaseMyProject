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
        LiteHelper.DownloadMetadata remoteMetadata = LiteHelper.fetchDownloadMetadata(url);
        if (remoteMetadata == null) {
            LiteHelper.notifyDownloadFailure(callback, "无法获取文件信息");
            return;
        }
        long totalLength = remoteMetadata.totalLength();
        LiteHelper.DownloadMetadata localMetadata = LiteHelper.readDownloadMetadata(target);
        boolean hasValidator = !android.text.TextUtils.isEmpty(remoteMetadata.validator());
        boolean canResume = metadataMatches(localMetadata, remoteMetadata);

        if (canResume && target.exists() && target.length() == totalLength) {
            postSuccess(callback, target.getAbsolutePath());
            return;
        }
        if (!claimed && !HttpRequestHelper.DOWNLOADING_URLS.add(url)) {
            LiteHelper.notifyDownloadFailure(callback, "file is downloading");
            return;
        }

        File temp = new File(target.getAbsolutePath() + ".temp");
        if (!canResume || temp.length() > totalLength) {
            if (temp.exists()) temp.delete();
            LiteHelper.deleteDownloadMetadata(target);
        }
        LiteHelper.writeDownloadMetadata(target, remoteMetadata);

        long downloaded = temp.exists() ? temp.length() : 0;
        try {
            if (canResume && downloaded == totalLength) {
                if (!LiteHelper.replaceFile(temp, target)) {
                    throw new IllegalStateException("完整临时文件替换失败");
                }
                postSuccess(callback, target.getAbsolutePath());
                return;
            }

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Encoding", "identity");
            if (canResume && downloaded > 0) {
                builder.addHeader("Range", "bytes=" + downloaded + "-");
                if (hasValidator) {
                    builder.addHeader("If-Range", remoteMetadata.validator());
                }
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
                    ContentRange range = parseContentRange(response.header("Content-Range"));
                    String responseValidator = getValidator(response);
                    boolean validatorMatches = !hasValidator
                            || remoteMetadata.validator().equals(responseValidator);
                    if (response.code() != 206 || range == null || range.start != downloaded
                            || range.end != totalLength - 1 || range.total != totalLength
                            || !validatorMatches) {
                        throw new IllegalStateException("断点响应不匹配");
                    }
                } else if (response.code() != 200) {
                    throw new IllegalStateException("下载响应错误: " + response.code());
                }

                if (downloaded == 0) {
                    String responseValidator = getValidator(response);
                    if (!android.text.TextUtils.isEmpty(responseValidator)) {
                        LiteHelper.writeDownloadMetadata(target,
                                new LiteHelper.DownloadMetadata(totalLength, responseValidator));
                    }
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
            postSuccess(callback, target.getAbsolutePath());
        } catch (Throwable t) {
            LiteHelper.notifyDownloadFailure(callback, t.toString());
        } finally {
            HttpRequestHelper.DOWNLOADING_URLS.remove(url);
        }
    }

    private static void postSuccess(OkHttpExecutor.DownloadCallback callback, String path) {
        LiteHelper.postToUi(() -> {
            if (callback != null) callback.onFinished(true, path, null);
        });
    }

    private static String getValidator(Response response) {
        String validator = response.header("ETag");
        return android.text.TextUtils.isEmpty(validator)
                ? response.header("Last-Modified") : validator;
    }

    private static boolean metadataMatches(LiteHelper.DownloadMetadata local,
                                           LiteHelper.DownloadMetadata remote) {
        if (local == null || local.totalLength() != remote.totalLength()) {
            return false;
        }
        return android.text.TextUtils.isEmpty(remote.validator())
                || remote.validator().equals(local.validator());
    }

    private static ContentRange parseContentRange(String value) {
        if (value == null || !value.startsWith("bytes ")) return null;
        int dash = value.indexOf('-', 6);
        int slash = value.indexOf('/', dash + 1);
        if (dash < 0 || slash < 0) return null;
        try {
            long start = Long.parseLong(value.substring(6, dash));
            long end = Long.parseLong(value.substring(dash + 1, slash));
            long total = Long.parseLong(value.substring(slash + 1));
            return new ContentRange(start, end, total);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record ContentRange(long start, long end, long total) {
    }
}
