package com.wcl.test.download;

import java.io.File;

public class DownloadTask {

    // 下载状态
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_ERROR = 4;
    public static final int STATUS_CANCELED = 5;

    public String taskId;
    public String url;
    public String savePath;
    public long totalBytes;
    public int status; // int 状态
    public String etag;
    public String lastModified;

    // 构造函数
    public DownloadTask(String url, String savePath, long totalBytes) {
        this.url = url;
        this.savePath = savePath;
        this.totalBytes = totalBytes;
        this.taskId = DownloadUtils.md5(url + savePath);
        this.status = STATUS_WAITING;
    }

    // 获取已下载字节数（不存数据库）
    public long getDownloadedBytes() {
        File f = new File(savePath);
        return f.exists() ? f.length() : 0;
    }

    // 获取进度百分比，保留两位小数
    public double getProgress() {
        if (totalBytes <= 0) return 0;
        double progress = (getDownloadedBytes() * 100.0) / totalBytes;
        return Math.round(progress * 100.0) / 100.0;
    }
}
