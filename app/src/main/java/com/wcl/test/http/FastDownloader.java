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
        if (LiteHelper.invalidUrl(url)) {
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

            long totalLength = LiteHelper.fetchContentLength(url);
            if (totalLength <= 0) {
                LiteHelper.notifyDownloadFailure(callback, "无法获取文件信息");
                return;
            }

            if (target.exists() && target.length() == totalLength) {
                LiteHelper.postSuccess(callback, target.getAbsolutePath());
                return;
            }

            int threadCount = 4;
            long blockSize = totalLength / threadCount;
            DownloadSession session = new DownloadSession(url, tempDir, totalLength, callback);
            AtomicBoolean hasError = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                long start = i * blockSize;
                long end = i == threadCount - 1 ? totalLength - 1 : start + blockSize - 1;
                DownloadPart part = new DownloadPart(i, start, end);
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
            deleteParts(tempDir, threadCount);
            tempDir.delete();
            LiteHelper.postSuccess(callback, target.getAbsolutePath());
        } catch (Exception e) {
            AppLogUtils.e(TAG, "fastDownload error: " + e);
            LiteHelper.notifyDownloadFailure(callback, e.toString());
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
        Request request = new Request.Builder()
                .url(session.url())
                .addHeader("Accept-Encoding", "identity")
                .addHeader("Range", "bytes=" + rangeStart + "-" + part.end())
                .build();
        try (Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
            if (response.code() != 206) {
                throw new IllegalStateException("服务器不支持精确 Range");
            }
            LiteHelper.ContentRange range = LiteHelper.parseContentRange(response.header("Content-Range"));
            if (range == null || range.start() != rangeStart || range.end() != part.end()
                    || range.total() != session.totalLength()) {
                throw new IllegalStateException("Content-Range 不匹配");
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

    private record DownloadPart(int index, long start, long end) {
    }

    private record DownloadSession(
            String url,
            File tempDir,
            long totalLength,
            OkHttpExecutor.DownloadCallback callback,
            AtomicLong downloaded,
            AtomicLong lastCallbackTime
    ) {
        private DownloadSession(String url, File tempDir, long totalLength,
                                OkHttpExecutor.DownloadCallback callback) {
            this(url, tempDir, totalLength, callback,
                    new AtomicLong(), new AtomicLong());
        }
    }
}
