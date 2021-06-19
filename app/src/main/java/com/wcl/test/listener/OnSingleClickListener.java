package com.wcl.test.listener;

import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    private long mLastClickTime;

    //时间间隔：比如1000毫秒内只能点击一次
    private long timeInterval = 1000L;

    public OnSingleClickListener() {
    }

    public OnSingleClickListener(long interval) {
        this.timeInterval = interval;
    }

    @Override
    @Deprecated
    public void onClick(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > timeInterval) {
            onSingleClick(v);
            mLastClickTime = nowTime;
        }
    }

    public abstract void onSingleClick(View v);
}