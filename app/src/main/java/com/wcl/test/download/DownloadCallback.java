package com.wcl.test.download;

public interface DownloadCallback {

    // 每秒回调一次进度，保留两位小数
    void onProgress(String taskId, long downloadedBytes, long totalBytes, double progress);

    // 状态变化回调
    void onStatusChanged(String taskId, DownloadTask.Status status, String errorMsg);

    // 下载完成回调
    void onFinished(String taskId, String filePath);
}
