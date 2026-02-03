package com.wcl.test.download;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;

public class DownloadButton extends AppCompatTextView {

    private String url;
    private LifecycleOwner owner;

    /**
     * 当前任务（弱状态，不缓存引用）
     */
    private DownloadTask task;

    /**
     * ⚠️ 核心：callback 是成员变量，整个 View 生命周期唯一
     */
    private final DownloadListener callback = new DownloadListener() {
        @Override
        public void onProgress(DownloadTask task) {
            post(DownloadButton.this::syncState);
        }

        @Override
        public void onStatusChanged(DownloadTask task) {
            post(DownloadButton.this::syncState);
        }
    };

    public DownloadButton(Context context) {
        super(context);
        init();
    }

    public DownloadButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DownloadButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setOnClickListener(this::handleClick);
        setText("下载");
    }

    /**
     * 对外唯一入口
     */
    public void bind(String url, LifecycleOwner owner) {
        this.url = url;
        this.owner = owner;

        DownloadTask task = DownloadManager.ins().getTask(url);
        if (task != null) {
            DownloadManager.ins().start(url, owner, callback);
        }

        syncState();
    }

    private void handleClick(View v) {
        if (url == null || owner == null) return;

        task = DownloadManager.ins().getTask(url);

        if (task == null) {
            // 从未下载过
            DownloadManager.ins().start(url, owner, callback);
            return;
        }

        switch (task.status) {
            case WAITING:
            case PAUSED:
            case ERROR:
                DownloadManager.ins().start(url, owner, callback);
                break;

            case DOWNLOADING:
                DownloadManager.ins().pause(url);
                break;

            case FINISHED:
                // 已完成，通常不处理或提示
                break;
        }
    }

    /**
     * 根据 DownloadTask 刷新 UI
     */
    private void syncState() {
        if (url == null) return;

        task = DownloadManager.ins().getTask(url);

        if (task == null) {
            setText("下载");
            return;
        }

        switch (task.status) {
            case WAITING:
                setText("等待中");
                break;

            case DOWNLOADING:
                setText(String.format("下载中 %.1f%%", task.progress));
                break;

            case PAUSED:
                setText("继续");
                break;

            case FINISHED:
                setText("已完成");
                break;

            case ERROR:
                setText("重试");
                break;
        }
    }
}
