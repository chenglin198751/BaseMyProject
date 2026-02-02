package com.wcl.test.download;

public interface DownloadCallback2 {

    // 每秒回调一次进度，保留两位小数
    void onProgress(DownloadTask task, long totalBytes, double progress);

    // 状态变化回调
    void onStatusChanged(DownloadTask task);
}
