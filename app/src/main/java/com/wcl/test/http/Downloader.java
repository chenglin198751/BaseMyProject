package com.wcl.test.http;

import com.wcl.test.utils.AppUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Request;
import okhttp3.Response;

class Downloader {

    /**
     * 异步下载文件（多线程切块下载 + 支持断点续传 + 进度按1%回调）
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
                if (!tempDir.exists()) tempDir.mkdirs();

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
                if (target.exists() && target.length() == totalLength) {
                    HttpHelper.postToUi(() -> callback.onFinished(true, target.getAbsolutePath(), null));
                    return;
                }

                int threadCount = 4;
                long blockSize = totalLength / threadCount;

                Thread[] threads = new Thread[threadCount];
                AtomicLong downloaded = new AtomicLong(0);
                AtomicInteger lastPercent = new AtomicInteger(0);

                // 计算已下载长度（每个分块已有文件）
                for (int i = 0; i < threadCount; i++) {
                    File partFile = new File(tempDir, "part_" + i);
                    if (partFile.exists()) {
                        downloaded.addAndGet(partFile.length());
                    }
                }

                for (int i = 0; i < threadCount; i++) {
                    long start = i * blockSize;
                    long end = (i == threadCount - 1) ? totalLength - 1 : (start + blockSize - 1);
                    int index = i;

                    threads[i] = new Thread(() -> {
                        File partFile = new File(tempDir, "part_" + index);
                        long existing = partFile.exists() ? partFile.length() : 0;
                        long rangeStart = start + existing;
                        if (rangeStart > end) return; // 已下载完成

                        try (RandomAccessFile raf = new RandomAccessFile(partFile, "rw")) {
                            raf.seek(existing);

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

                                        long curDownloaded = downloaded.addAndGet(len);
                                        float floatPercent = (curDownloaded * 100f) / totalLength;
                                        int percent = (int) floatPercent;
                                        int last = lastPercent.get();
                                        if (percent > last && lastPercent.compareAndSet(last, percent)) {
                                            HttpHelper.postToUi(() -> {
                                                float f = AppUtils.formatFloat(floatPercent, 2);
                                                callback.onProgress(totalLength, curDownloaded, f);
                                            });
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

                // 合并分块文件
                try (RandomAccessFile out = new RandomAccessFile(target, "rw")) {
                    byte[] buffer = new byte[8192];
                    for (int i = 0; i < threadCount; i++) {
                        File partFile = new File(tempDir, "part_" + i);
                        if (!partFile.exists()) continue;

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
                    float floatPercent = (sum * 100f) / totalLength;
                    int percent = (int) floatPercent;
                    if (percent > lastPercent) {
                        lastPercent = percent;
                        long curSum = sum;
                        HttpHelper.postToUi(() -> {
                            float f = AppUtils.formatFloat(floatPercent, 2);
                            callback.onProgress(totalLength, curSum, f);
                        });
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

}
