package com.wcl.test.download;

public class DownloadTask {

    public enum Status {
        IDLE,        // 未操作
        WAITING,     // 等待调度
        DOWNLOADING, // 下载中
        PAUSED,      // 已暂停
        FINISHED,    // 已完成
        ERROR,       // 出错
    }

    // 以下是 UI 层使用，不参与序列化
    public transient boolean isSelected;

    // 以下是不可被修改变量
    public final String taskId;
    public final String url;
    public final String savePath;

    // 数据库自增字段，不参与序列化
    public transient long _id;
    public transient long create_time;
    public transient long update_time;

    // 以下是可变状态（只允许 DownloadWorker 改）
    public volatile long totalBytes;
    public volatile String errorMsg;
    public volatile transient Status status;
    public volatile transient long downloadedBytes;
    public volatile transient double progress;

    // 扩展信息，比如应用详情json之类，会被保存到DB
    public transient String extra;

    public DownloadTask(String url) {
        this.taskId = DownloadUtils.getTaskId(url);
        this.url = url;
        this.savePath = DownloadUtils.getDownloadPath(url);
        this.status = Status.IDLE;
    }
}
