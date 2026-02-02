package com.wcl.test.download;

import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class DownloadWorker implements Runnable {

    private final DownloadTask task;
    private final OkHttpClient client;
    private final Runnable finishCallback;

    private final List<DownloadCallback2> callbacks = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean paused;
    private volatile boolean deleted;

    DownloadWorker(DownloadTask task, OkHttpClient client, Runnable finishCallback) {
        this.task = task;
        this.client = client;
        this.finishCallback = finishCallback;
    }

    void addCallback(LifecycleOwner owner, DownloadCallback2 cb) {
        if (cb == null || callbacks.contains(cb)) return;

        callbacks.add(cb);

        // 自动解绑回调
        owner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                removeCallback(cb);
            }
        });
    }

    void removeCallback(DownloadCallback2 cb) {
        callbacks.remove(cb);
    }

    void clearCallbacks() {
        callbacks.clear();
    }

    void pause() {
        paused = true;
    }

    void delete() {
        deleted = true;
    }

    void notifyProgress() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (DownloadCallback2 cb : callbacks) cb.onProgress(task.taskId);
        } else {
            DownloadUtils.runOnUiThread(() -> {
                for (DownloadCallback2 cb : callbacks) cb.onProgress(task.taskId);
            });
        }
    }

    void notifyStatus() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (DownloadCallback2 cb : callbacks) cb.onStatusChanged(task.taskId);
        } else {
            DownloadUtils.runOnUiThread(() -> {
                for (DownloadCallback2 cb : callbacks) cb.onStatusChanged(task.taskId);
            });
        }
    }

    @Override
    public void run() {
        File target = new File(task.savePath);

        // 已经完成直接返回
        if (target.exists() && task.totalBytes > 0 && target.length() == task.totalBytes) {
            task.downloadedBytes = task.totalBytes;
            task.progress = 100.0;
            task.status = DownloadTask.Status.FINISHED;
            notifyProgress();
            notifyStatus();
            runFinishCallback();
            return;
        }

        task.status = DownloadTask.Status.DOWNLOADING;
        notifyStatus();

        File temp = new File(task.savePath + ".temp");
        long downloaded = temp.exists() ? temp.length() : 0;
        task.downloadedBytes = downloaded;
        task.progress = task.totalBytes > 0 ? Math.round((downloaded * 100.0 / task.totalBytes) * 100.0) / 100.0 : 0;

        long lastCallbackTime = 0;

        try {
            Request.Builder builder = new Request.Builder().url(task.url);
            if (downloaded > 0) builder.addHeader("Range", "bytes=" + downloaded + "-");

            Response response = client.newCall(builder.build()).execute();
            if (!response.isSuccessful()) throw new RuntimeException("HTTP " + response.code());

            if (task.totalBytes <= 0) task.totalBytes = response.body().contentLength();

            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream(temp, true);

            byte[] buffer = new byte[8192];
            int len;
            long sum = downloaded;

            while ((len = in.read(buffer)) != -1) {
                if (paused || deleted) break;

                out.write(buffer, 0, len);
                sum += len;

                long now = System.currentTimeMillis();
                if (now - lastCallbackTime >= 1000) {
                    lastCallbackTime = now;
                    task.downloadedBytes = sum;
                    task.progress = Math.round((sum * 100.0 / task.totalBytes) * 100.0) / 100.0;
                    notifyProgress();
                }
            }

            out.flush();
            out.close();
            in.close();
            response.close();

            if (deleted) {
                task.status = DownloadTask.Status.DELETED;
                task.errorMsg = "Task deleted";
                notifyStatus();
            } else if (paused) {
                task.status = DownloadTask.Status.PAUSED;
                notifyStatus();
            } else {
                DownloadUtils.replaceFile(temp, target);
                task.downloadedBytes = task.totalBytes;
                task.progress = 100.0;
                notifyProgress();
                task.status = DownloadTask.Status.FINISHED;
                notifyStatus();
            }

        } catch (Throwable t) {
            task.status = DownloadTask.Status.ERROR;
            task.errorMsg = t.toString();
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
