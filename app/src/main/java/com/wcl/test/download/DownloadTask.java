package com.wcl.test.download;

public class DownloadTask {

    public enum Status {
        IDLE,        // 未操作
        WAITING,     // 等待调度
        DOWNLOADING, // 下载中
        PAUSED,      // 已暂停
        FINISHED,    // 已完成
        ERROR,       // 出错
        DELETED      // 已删除
    }

    // 以下是 UI 层使用，不参与序列化
    public transient boolean isSelected;

    // 以下是不可被修改变量
    public final String taskId;
    public final String url;
    public final String savePath;

    // 以下是可变状态（只允许 DownloadWorker 改）
    public volatile long totalBytes;
    public volatile Status status;
    public volatile String errorMsg;
    public volatile long downloadedBytes;
    public volatile double progress;

    public DownloadTask(String taskId,
                        String url,
                        String savePath) {
        this.taskId = taskId;
        this.url = url;
        this.savePath = savePath;
        this.status = Status.IDLE;
    }
}
