package com.wcl.test.download;

import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final List<DownloadListener> callbacks = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean paused;

    DownloadWorker(DownloadTask task, OkHttpClient client, Runnable finishCallback) {
        this.task = task;
        this.client = client;
        this.finishCallback = finishCallback;
    }

    /**
     * 添加回调
     */
    void addCallback(DownloadListener cb) {
        if (cb == null || callbacks.contains(cb)) return;

        callbacks.add(cb);
    }

    void pause() {
        paused = true;
    }

    /**
     * 移除指定回调，不影响下载任务
     */
    void removeCallback(DownloadListener cb) {
        synchronized (callbacks) {
            callbacks.remove(cb);
        }
    }

    private void notifyProgress() {
        runOnUiThread(() -> {
            for (DownloadListener cb : callbacks) cb.onProgress(task);
        });
    }

    private void notifyStatus() {
        runOnUiThread(() -> {
            for (DownloadListener cb : callbacks) cb.onStatusChanged(task);
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
            runOnUiThread(() -> DownloadManager.ins().delete(task.url));
            return;
        }

        long lastCallbackTime = 0;

        try {
            Request.Builder builder = new Request.Builder().url(task.url);
            if (downloaded > 0) builder.addHeader("Range", "bytes=" + downloaded + "-");

            Response response = client.newCall(builder.build()).execute();
            if (!response.isSuccessful()) {
                task.status = DownloadTask.Status.ERROR;
                task.errorMsg = response.toString();
                notifyStatus();
                return;
            }

            if (task.totalBytes <= 0) {
                task.totalBytes = response.body().contentLength();
                DownloadDBHelper.ins().saveTask(task);
            }

            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream(temp, true);

            byte[] buffer = new byte[8192];
            int len;
            long sum = downloaded;

            while ((len = in.read(buffer)) != -1) {
                if (paused) break;

                out.write(buffer, 0, len);
                sum += len;

                long now = System.currentTimeMillis();
                if (now - lastCallbackTime >= 1000) {
                    lastCallbackTime = now;
                    task.downloadedBytes = sum;
                    task.progress = DownloadUtils.roundProgress(sum, task.totalBytes);
                    notifyProgress();
                }
            }

            out.flush();
            out.close();
            in.close();
            response.close();

            if (paused) {
                task.status = DownloadTask.Status.PAUSED;
            } else {
                DownloadUtils.replaceFile(temp, target);
                task.downloadedBytes = task.totalBytes;
                task.progress = 100.0;
                notifyProgress();
                task.status = DownloadTask.Status.FINISHED;
            }
            DownloadDBHelper.ins().saveTask(task);
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
        runOnUiThread(() -> {
            if (finishCallback != null) finishCallback.run();
        });
    }

    private void runOnUiThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            DownloadUtils.runOnUiThread(r);
        }
    }
}
