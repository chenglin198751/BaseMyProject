package com.wcl.test.download;

import com.wcl.test.utils.AppFileUtils;

import java.io.File;

public class DownloadTask {

    public enum Status {
        STATUS_WAITING,
        STATUS_DOWNLOADING,
        STATUS_PAUSED,
        STATUS_FINISHED,
        STATUS_ERROR,
        STATUS_CANCELED
    }

    public String taskId;
    public String url;
    public String savePath;
    public long totalBytes;
    public Status status;

    public DownloadTask(String url, long totalBytes) {
        this.url = url;
        this.savePath = DownloadUtils.getDownloadPath(url);
        this.totalBytes = totalBytes;
        this.taskId = DownloadUtils.getTaskId(url);
        this.status = Status.STATUS_WAITING;
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
