package com.wcl.test.listener;

import android.view.View;

import androidx.annotation.IntRange;

/**
 * 比如，2秒内点击10次。支持自定义点击时间间隔和次数
 */
public abstract class OnMultipleClickListener implements View.OnClickListener {
    private int mCount = 0;
    private int totalCount = 10;
    private long mFirstTime = 0;
    private long timeInterval = 2 * 1000;


    public OnMultipleClickListener() {
    }

    /**
     * @param interval 时间间隔，比如 interval 毫秒内点击10次。
     * @param count    点击次数，比如2毫秒内点击 totalCount 次
     */
    public OnMultipleClickListener(
            @IntRange(from = 1, to = 10) long interval,
            @IntRange(from = 1, to = 100) int count) {
        this.timeInterval = interval * 1000;
        this.totalCount = count;
    }

    @Override
    @Deprecated
    public void onClick(View v) {
        if (mCount == 0) {
            mFirstTime = System.currentTimeMillis();
        }
        mCount++;

        long end = System.currentTimeMillis() - mFirstTime;
        if (mCount >= totalCount && end < timeInterval) {
            mCount = 0;
            mFirstTime = 0;
            onMultipleClick(v);
        } else if (end > timeInterval) {
            mCount = 0;
            mFirstTime = 0;
        }
    }

    public abstract void onMultipleClick(View v);
}