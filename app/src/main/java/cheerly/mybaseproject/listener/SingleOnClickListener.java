package cheerly.mybaseproject.listener;

import android.view.View;

public abstract class SingleOnClickListener implements View.OnClickListener {
    private long mLastClickTime;

    //时间间隔：比如1000毫秒内只能点击一次
    private long timeInterval = 1000L;

    public SingleOnClickListener() {
    }

    public SingleOnClickListener(long interval) {
        this.timeInterval = interval;
    }

    @Override
    public void onClick(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > timeInterval) {
            onSingleClick(v);
            mLastClickTime = nowTime;
        }
    }

    public abstract void onSingleClick(View v);
}