package com.wcl.test.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class DownloadWorker implements Runnable {

    private final DownloadTask task;
    private final DownloadCallback2 callback;
    private final OkHttpClient client;

    private volatile boolean paused;
    private volatile boolean canceled;

    DownloadWorker(DownloadTask task,
                   DownloadCallback2 callback,
                   OkHttpClient client) {
        this.task = task;
        this.callback = callback;
        this.client = client;
    }

    void pause() {
        paused = true;
    }

    void cancel() {
        canceled = true;
    }

    @Override
    public void run() {
        File target = new File(task.savePath);

        // 已经下载完成则直接返回文件路径
        if (target.exists() && task.totalBytes > 0 && target.length() == task.totalBytes) {
            task.downloadedBytes = task.totalBytes;
            task.progress = 100.0;
            task.status = DownloadTask.Status.FINISHED;
            notifyStatus();
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
            if (downloaded > 0) {
                builder.addHeader("Range", "bytes=" + downloaded + "-");
            }

            Response response = client.newCall(builder.build()).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP " + response.code());
            }

            if (task.totalBytes <= 0) {
                task.totalBytes = response.body().contentLength();
            }

            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream(temp, true);

            byte[] buffer = new byte[8192];
            int len;
            long sum = downloaded;

            while ((len = in.read(buffer)) != -1) {

                if (paused) {
                    task.status = DownloadTask.Status.PAUSED;
                    notifyStatus();
                    break;
                }

                if (canceled) {
                    task.status = DownloadTask.Status.CANCELED;
                    task.errorMsg = "canceled";
                    notifyStatus();
                    break;
                }

                out.write(buffer, 0, len);
                sum += len;

                long now = System.currentTimeMillis();
                if (now - lastCallbackTime >= 1000) { // 每秒更新一次
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

            if (!paused && !canceled) {
                // 替换文件，确保完整
                DownloadUtils.replaceFile(temp, target);
                task.downloadedBytes = task.totalBytes;
                task.progress = 100.0;
                task.status = DownloadTask.Status.FINISHED;
                notifyStatus();
            }

        } catch (Throwable t) {
            task.status = DownloadTask.Status.ERROR;
            task.errorMsg = t.toString();
            notifyStatus();
        }
    }

    private void notifyProgress() {
        DownloadUtils.runOnUiThread(() ->
                callback.onProgress(task.taskId));
    }

    private void notifyStatus() {
        DownloadUtils.runOnUiThread(() ->
                callback.onStatusChanged(task.taskId));
    }
}
