package com.wcl.test.download;

import java.io.File;

public class DownloadTask {

    public enum Status {
        WAITING,
        DOWNLOADING,
        PAUSED,
        FINISHED,
        ERROR,
        CANCELED
    }

    /**
     * 不可变身份
     */
    public final String taskId;
    public final String url;
    public final String savePath;

    /**
     * 可变状态（只允许 DownloadWorker 改）
     */
    public volatile long totalBytes;
    public volatile Status status;
    public volatile String errorMsg;
    public volatile long downloadedBytes;
    public volatile double progress;

    public DownloadTask(String taskId,
                        String url,
                        String savePath,
                        long totalBytes) {
        this.taskId = taskId;
        this.url = url;
        this.savePath = savePath;
        this.totalBytes = totalBytes;
        this.status = Status.WAITING;
    }
}
