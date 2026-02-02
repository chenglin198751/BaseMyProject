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

    private volatile boolean isPaused = false;
    private volatile boolean isCanceled = false;

    public DownloadWorker(DownloadTask task, DownloadCallback2 callback, OkHttpClient client) {
        this.task = task;
        this.callback = callback;
        this.client = client;
    }

    public void pause() {
        isPaused = true;
    }

    public void cancel() {
        isCanceled = true;
    }

    @Override
    public void run() {
        task.status = DownloadTask.Status.STATUS_DOWNLOADING;
        DownloadUtils.runOnUiThread(() -> callback.onStatusChanged(task));

        File targetFile = new File(task.savePath);
        File tempFile = new File(task.savePath + ".temp");

        long downloaded = tempFile.exists() ? tempFile.length() : 0;

        try {
            Request.Builder builder = new Request.Builder()
                    .url(task.url);
            if (downloaded > 0) {
                builder.addHeader("Range", "bytes=" + downloaded + "-");
            }

            Response response = client.newCall(builder.build()).execute();
            if (!response.isSuccessful()) {
                throw new Exception("HTTP error code: " + response.code());
            }

            long totalBytes = task.totalBytes > 0 ? task.totalBytes : response.body().contentLength();
            task.totalBytes = totalBytes;

            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream(tempFile, true);
            byte[] buffer = new byte[8192];
            int len;
            long sum = downloaded;
            long lastCallbackTime = System.currentTimeMillis();

            while ((len = in.read(buffer)) != -1) {
                if (isPaused) {
                    task.status = DownloadTask.Status.STATUS_PAUSED;
                    DownloadUtils.runOnUiThread(() ->
                            callback.onStatusChanged(task));
                    break;
                }
                if (isCanceled) {
                    task.status = DownloadTask.Status.STATUS_CANCELED;
                    task.errorMsg = "Task canceled";
                    DownloadUtils.runOnUiThread(() ->
                            callback.onStatusChanged(task));
                    break;
                }

                out.write(buffer, 0, len);
                sum += len;

                long now = System.currentTimeMillis();
                if (now - lastCallbackTime >= 1000) { // 每秒回调一次
                    double progress = Math.round((sum * 100.0 / totalBytes) * 100.0) / 100.0;
                    DownloadUtils.runOnUiThread(() ->
                            callback.onProgress(task, totalBytes, progress));
                    lastCallbackTime = now;
                }
            }

            out.flush();
            out.close();
            in.close();
            response.close();

            if (!isPaused && !isCanceled) {
                DownloadUtils.replaceFile(tempFile, targetFile);
                task.status = DownloadTask.Status.STATUS_FINISHED;
                DownloadUtils.runOnUiThread(() -> {
                    callback.onStatusChanged(task);
                });
            }

        } catch (Throwable t) {
            t.printStackTrace();
            task.status = DownloadTask.Status.STATUS_ERROR;
            task.errorMsg = t.toString();
            DownloadUtils.runOnUiThread(() ->
                    callback.onStatusChanged(task));
        }
    }
}
