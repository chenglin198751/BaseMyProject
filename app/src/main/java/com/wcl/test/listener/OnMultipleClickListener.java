package com.wcl.test.listener;

import android.view.View;

/**
 * 比如，2秒内点击10次。支持自定义点击时间间隔和次数
 */
public abstract class OnMultipleClickListener implements View.OnClickListener {
    private static final long DEFAULT_INTERVAL = 2 * 1000; // 默认时间间隔（毫秒）

    private int mCount = 0;
    private int totalCount = 10;
    private long startTime = 0;
    private long timeInterval = DEFAULT_INTERVAL;

    public OnMultipleClickListener() {
    }

    /**
     * @param interval 时间间隔，单位毫秒。比如 interval 毫秒内点击10次。
     * @param count    点击次数，比如2000毫秒内点击 totalCount 次
     */
    public OnMultipleClickListener(long interval, int count) {
        this.timeInterval = interval;
        this.totalCount = count;
    }

    @Override
    @Deprecated
    public void onClick(View v) {
        if (mCount == 0) {
            startTime = System.currentTimeMillis();
        }
        mCount++;

        long elapsed = System.currentTimeMillis() - startTime;

        if (mCount >= totalCount || elapsed > timeInterval) {
            if (mCount >= totalCount && elapsed <= timeInterval) {
                mCount = 0;
                startTime = 0;
                onMultipleClick(v);
            } else {
                mCount = 0;
                startTime = 0;
            }
        }
    }

    public abstract void onMultipleClick(View v);
}