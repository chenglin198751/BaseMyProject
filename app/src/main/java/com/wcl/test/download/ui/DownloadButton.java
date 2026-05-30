package com.wcl.test.download.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.wcl.test.R;
import com.wcl.test.download.DownloadListener;
import com.wcl.test.download.DownloadManager;
import com.wcl.test.download.DownloadTask;
import com.wcl.test.listener.OnSingleClickListener;

/**
 * 下载按钮控件
 */
public class DownloadButton extends ProgressColorTextView {

    private String url;

    private final DownloadListener callback = new DownloadListener() {
        @Override
        public void onProgress(DownloadTask task) {
            if (TextUtils.equals(url, task.url)) {
                syncState();
            }
        }

        @Override
        public void onStatusChanged(DownloadTask task) {
            if (TextUtils.equals(url, task.url)) {
                syncState();
            }
        }

        @Override
        public void onDeleted(String _url) {
            if (TextUtils.equals(url, _url)) {
                syncState();
            }
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
        setText(R.string.down_download);
        setClickable(true);

        setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleClick();
            }
        });
    }

    /**
     * 对外唯一入口：订阅下载状态（不启动下载）
     */
    public void bind(String url) {
        this.url = url;
        syncState();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DownloadManager.ins().addListener(callback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DownloadManager.ins().removeListener(callback);
    }

    // 首次点击才真正创建并启动任务
    private void handleClick() {
        if (url == null) return;

        DownloadTask task = DownloadManager.ins().getTask(url);
        if (task == null) {
            DownloadManager.ins().start(url);
            return;
        }

        switch (task.status) {
            case IDLE:
            case WAITING:
            case PAUSED:
            case ERROR:
                DownloadManager.ins().start(url);
                break;
            case DOWNLOADING:
                DownloadManager.ins().pause(url);
                break;
            case FINISHED:
                break;
        }
    }

    /**
     * 根据 DownloadTask 刷新 UI
     */
    private void syncState() {
        if (url == null) return;

        DownloadTask task = DownloadManager.ins().getTask(url);
        if (task == null) {
            setProgress(0);
            setText(R.string.down_download);
            return;
        }

        setProgress(task.progress);
        switch (task.status) {
            case IDLE:
                setText(R.string.down_download);
                break;
            case WAITING:
                setText(R.string.down_waiting);
                break;
            case DOWNLOADING:
                setText(getContext().getString(R.string.down_progress, task.progress));
                break;
            case PAUSED:
                setText(R.string.down_continue);
                break;
            case FINISHED:
                setText(R.string.down_finished);
                break;
            case ERROR:
                setText(R.string.down_retry);
                break;
        }

        setText(getText() + " " + task.status);
    }
}
