package com.wcl.test.http;

import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;
import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Request;
import okhttp3.Response;

class FastDownloader {

    private static final String TAG = "FastDownloader";
    private static final int PROGRESS_INTERVAL = 500;
    private static final ExecutorService CHUNK_EXECUTOR = Executors.newFixedThreadPool(4);

    static void fastDownload(String url, OkHttpExecutor.DownloadCallback callback) {
        if (LiteHelper.isInvalidUrl(url)) {
            LiteHelper.notifyDownloadFailure(callback, "Invalid URL");
            return;
        }
        if (!HttpRequestHelper.DOWNLOADING_URLS.add(url)) {
            LiteHelper.notifyDownloadFailure(callback, "file is downloading");
            return;
        }

        try {
            AppThreadPoolExecutor.getExecutor().execute(() -> executeFastDownload(url, callback));
        } catch (RejectedExecutionException e) {
            HttpRequestHelper.DOWNLOADING_URLS.remove(url);
            LiteHelper.notifyDownloadFailure(callback, e.toString());
        }
    }

    private static void executeFastDownload(String url, OkHttpExecutor.DownloadCallback callback) {
        try {
            File target = new File(LiteHelper.getDownloadPath(url));
            File tempDir = new File(target.getAbsolutePath() + "_tmp");
            if (!tempDir.exists() && !tempDir.mkdirs()) {
                throw new IllegalStateException("无法创建临时目录");
            }

            LiteHelper.DownloadMetadata remoteMetadata = LiteHelper.fetchDownloadMetadata(url);
            if (remoteMetadata == null) {
                LiteHelper.notifyDownloadFailure(callback, "无法获取文件信息");
                return;
            }
            long totalLength = remoteMetadata.totalLength();
            LiteHelper.DownloadMetadata localMetadata = LiteHelper.readDownloadMetadata(target);
            boolean metadataMatches = metadataMatches(localMetadata, remoteMetadata);
            if (!metadataMatches) {
                deleteParts(tempDir, 4);
                if (!target.exists()) {
                    LiteHelper.deleteDownloadMetadata(target);
                }
            }
            if (!target.exists()) {
                LiteHelper.writeDownloadMetadata(target, remoteMetadata);
            }

            if (metadataMatches && target.exists() && target.length() == totalLength) {
                postSuccess(callback, target.getAbsolutePath());
                return;
            }

            int threadCount = 4;
            long blockSize = totalLength / threadCount;
            DownloadSession session = new DownloadSession(url, tempDir, totalLength,
                    remoteMetadata.validator(), callback);
            AtomicBoolean hasError = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                long start = i * blockSize;
                long end = i == threadCount - 1 ? totalLength - 1 : start + blockSize - 1;
                DownloadPart part = new DownloadPart(i, start, end);
                try {
                    CHUNK_EXECUTOR.execute(() -> {
                        try {
                            downloadPart(session, part);
                        } catch (Exception e) {
                            hasError.set(true);
                            AppLogUtils.e(TAG, "chunk " + part.index() + " download error: " + e);
                        } finally {
                            latch.countDown();
                        }
                    });
                } catch (RejectedExecutionException e) {
                    hasError.set(true);
                    latch.countDown();
                    AppLogUtils.e(TAG, "chunk " + part.index() + " rejected: " + e);
                }
            }

            latch.await();
            if (hasError.get()) {
                LiteHelper.notifyDownloadFailure(callback, "部分分块下载失败");
                return;
            }

            File merged = new File(target.getAbsolutePath() + ".merge");
            try (RandomAccessFile out = new RandomAccessFile(merged, "rw")) {
                out.setLength(0);
                byte[] buffer = new byte[8192];
                for (int i = 0; i < threadCount; i++) {
                    long start = i * blockSize;
                    long end = i == threadCount - 1 ? totalLength - 1 : start + blockSize - 1;
                    File partFile = new File(tempDir, "part_" + i);
                    long expected = end - start + 1;
                    if (!partFile.isFile() || partFile.length() != expected) {
                        throw new IllegalStateException("分块文件长度错误");
                    }
                    try (RandomAccessFile partRaf = new RandomAccessFile(partFile, "r")) {
                        int len;
                        while ((len = partRaf.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }

            if (merged.length() != totalLength || !LiteHelper.replaceFile(merged, target)) {
                throw new IllegalStateException("合并文件校验失败");
            }
            LiteHelper.writeDownloadMetadata(target, remoteMetadata);
            deleteParts(tempDir, threadCount);
            tempDir.delete();
            postSuccess(callback, target.getAbsolutePath());
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "fastDownload error: " + t);
            LiteHelper.notifyDownloadFailure(callback, t.toString());
        } finally {
            HttpRequestHelper.DOWNLOADING_URLS.remove(url);
        }
    }

    private static void downloadPart(DownloadSession session, DownloadPart part) throws Exception {
        File partFile = new File(session.tempDir(), "part_" + part.index());
        long expected = part.end() - part.start() + 1;
        long existing = partFile.exists() ? partFile.length() : 0;
        if (existing > expected) {
            partFile.delete();
            existing = 0;
        }
        session.downloaded().addAndGet(existing);
        if (existing == expected) {
            return;
        }

        long rangeStart = part.start() + existing;
        Request.Builder builder = new Request.Builder()
                .url(session.url())
                .addHeader("Accept-Encoding", "identity")
                .addHeader("Range", "bytes=" + rangeStart + "-" + part.end());
        boolean hasValidator = !android.text.TextUtils.isEmpty(session.validator());
        if (hasValidator) {
            builder.addHeader("If-Range", session.validator());
        }
        Request request = builder.build();
        try (Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
            if (response.code() != 206) {
                throw new IllegalStateException("服务器不支持精确 Range");
            }
            ContentRange range = parseContentRange(response.header("Content-Range"));
            if (range == null || range.start != rangeStart || range.end != part.end()
                    || range.total != session.totalLength()) {
                throw new IllegalStateException("Content-Range 不匹配");
            }
            String responseValidator = getValidator(response);
            if (hasValidator && !session.validator().equals(responseValidator)) {
                throw new IllegalStateException("分块版本校验失败");
            }
            try (InputStream in = response.body().byteStream();
                 RandomAccessFile raf = new RandomAccessFile(partFile, "rw")) {
                raf.seek(existing);
                byte[] buffer = new byte[8192];
                long received = 0;
                int len;
                while ((len = in.read(buffer)) != -1) {
                    if (received + len > expected - existing) {
                        throw new IllegalStateException("分块响应超出范围");
                    }
                    raf.write(buffer, 0, len);
                    received += len;
                    long current = session.downloaded().addAndGet(len);
                    long now = System.currentTimeMillis();
                    long previous = session.lastCallbackTime().get();
                    if (now - previous >= PROGRESS_INTERVAL
                            && session.lastCallbackTime().compareAndSet(previous, now)) {
                        float percent = AppUtils.formatFloat(
                                (current * 100f) / session.totalLength(), 2);
                        LiteHelper.postToUi(() -> {
                            if (session.callback() != null) {
                                session.callback().onProgress(session.totalLength(), current, percent);
                            }
                        });
                    }
                }
                if (received != expected - existing || raf.length() != expected) {
                    throw new IllegalStateException("分块响应长度错误");
                }
            }
        }
    }

    private static void deleteParts(File tempDir, int count) {
        for (int i = 0; i < count; i++) {
            new File(tempDir, "part_" + i).delete();
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

    private record DownloadPart(int index, long start, long end) {
    }

    private record DownloadSession(
            String url,
            File tempDir,
            long totalLength,
            String validator,
            OkHttpExecutor.DownloadCallback callback,
            AtomicLong downloaded,
            AtomicLong lastCallbackTime
    ) {
        private DownloadSession(String url, File tempDir, long totalLength, String validator,
                                OkHttpExecutor.DownloadCallback callback) {
            this(url, tempDir, totalLength, validator, callback,
                    new AtomicLong(), new AtomicLong());
        }
    }

    private record ContentRange(long start, long end, long total) {
    }
}
