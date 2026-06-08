package com.wcl.test.download;

import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * DownloadWorker 负责单个任务的下载（内部使用，不对外）
 */
class DownloadWorker implements Runnable {

    private final DownloadTask task;
    private final OkHttpClient client;
    private final Runnable finishCallback;
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private volatile Call call;
    private static final int PROGRESS_INTERVAL = 500;

    DownloadWorker(DownloadTask task, OkHttpClient client, Runnable finishCallback) {
        this.task = task;
        this.client = client;
        this.finishCallback = finishCallback;
    }

    void pause() {
        paused.set(true);
        if (call != null) {
            call.cancel();
        }
    }

    private void notifyProgress() {
        DownloadUtils.runOnUiThread(() -> {
            for (DownloadListener cb : DownloadManager.listeners) cb.onProgress(task);
        });
    }

    private void notifyStatus() {
        DownloadUtils.runOnUiThread(() -> {
            for (DownloadListener cb : DownloadManager.listeners) cb.onStatusChanged(task);
        });
    }

    @Override
    public void run() {
        File target = new File(task.savePath);

        // 已完成直接回调
        if (target.exists() && task.totalBytes > 0 && target.length() == task.totalBytes) {
            task.downloadedBytes = task.totalBytes;
            task.progress = 100.0;
            task.status = DownloadTask.Status.FINISHED;
            notifyStatus();
            runFinishCallback();
            return;
        }

        task.status = DownloadTask.Status.DOWNLOADING;
        notifyStatus();

        File temp = new File(task.savePath + ".temp");
        long downloaded = temp.exists() ? temp.length() : 0;
        task.downloadedBytes = downloaded;
        task.progress = task.totalBytes > 0 ? DownloadUtils.roundProgress(downloaded, task.totalBytes) : 0;

        // 断点续传异常则直接删除下载任务
        if (downloaded > 0 && task.totalBytes > 0 && downloaded > task.totalBytes) {
            DownloadUtils.runOnUiThread(() -> DownloadManager.ins().delete(task.url));
            return;
        }

        long lastCallbackTime = 0;

        try {
            Request.Builder builder = new Request.Builder().url(task.url);
            if (downloaded > 0) builder.addHeader("Range", "bytes=" + downloaded + "-");

            call = client.newCall(builder.build());
            try (Response response = call.execute()) {
                if (!response.isSuccessful()) {
                    task.status = DownloadTask.Status.ERROR;
                    task.errorMsg = response.toString();
                    DownloadDBHelper.ins().saveTask(task);
                    notifyStatus();
                    return;
                }

                if (task.totalBytes <= 0) {
                    task.totalBytes = response.body().contentLength();
                    DownloadDBHelper.ins().saveTask(task);
                }

                try (InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(temp, true)) {

                    byte[] buffer = new byte[8192];
                    int len;
                    long sum = downloaded;

                    while ((len = in.read(buffer)) != -1) {
                        if (paused.get()) break;

                        out.write(buffer, 0, len);
                        sum += len;

                        long now = System.currentTimeMillis();
                        if (now - lastCallbackTime >= PROGRESS_INTERVAL) {
                            lastCallbackTime = now;
                            task.downloadedBytes = sum;
                            task.progress = DownloadUtils.roundProgress(sum, task.totalBytes);
                            notifyProgress();
                        }
                    }

                    out.flush();
                }
            }

            if (paused.get()) {
                task.status = DownloadTask.Status.PAUSED;
            } else {
                DownloadUtils.replaceFile(temp, target);
                task.downloadedBytes = task.totalBytes;
                task.progress = 100.0;
                notifyProgress();
                task.status = DownloadTask.Status.FINISHED;
            }
            notifyStatus();
        } catch (IOException io) {
            // 调用call.cancel() 会抛异常，需区分主动暂停和真正错误
            if (paused.get()) {
                task.status = DownloadTask.Status.PAUSED;
            } else {
                task.status = DownloadTask.Status.ERROR;
                task.errorMsg = io.toString();
                DownloadDBHelper.ins().saveTask(task);
            }
            notifyStatus();
        } catch (Throwable t) {
            task.status = DownloadTask.Status.ERROR;
            task.errorMsg = t.toString();
            DownloadDBHelper.ins().saveTask(task);
            notifyStatus();
        } finally {
            runFinishCallback();
        }
    }

    private void runFinishCallback() {
        DownloadUtils.runOnUiThread(() -> {
            if (finishCallback != null) finishCallback.run();
        });
    }
}
