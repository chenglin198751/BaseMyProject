package com.wcl.test.test.sothos;

import android.os.Handler;
import android.os.Looper;

public class SothosTimer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long intervalMillis = 1 * 60 * 1000;
    private Runnable mListener;

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.run();
            }
            handler.postDelayed(this, intervalMillis);
        }
    };

    public void start(Runnable listener) {
        stop();
        mListener = listener;
        handler.postDelayed(task, intervalMillis);
    }

    public void stop() {
        handler.removeCallbacks(task);
    }
}
