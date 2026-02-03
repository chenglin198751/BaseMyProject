package com.wcl.test.download;

public interface DownloadListener {

    /**
     * 1s刷新一次下载进度
     */
    void onProgress(DownloadTask task);

    /**
     * 下载状态变化
     */
    void onStatusChanged(DownloadTask task);
}
