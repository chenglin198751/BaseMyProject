package com.wcl.test.listener;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final long DEFAULT_INTERVAL = 1000L;

    private long lastClickTime;
    private final long interval;

    public OnSingleClickListener() {
        this(DEFAULT_INTERVAL);
    }

    public OnSingleClickListener(long interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0");
        }
        this.interval = interval;
    }

    @Override
    public void onClick(View v) {
        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - lastClickTime > interval) {
            onSingleClick(v);
            lastClickTime = nowTime;
        }
    }

    public abstract void onSingleClick(View v);
}
