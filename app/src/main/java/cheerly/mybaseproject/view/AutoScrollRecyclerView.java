package cheerly.mybaseproject.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.lang.ref.WeakReference;

/**
 * Created by weichenglin  on 2018/6/22
 */
public class AutoScrollRecyclerView extends RecyclerView {
    private static final long TIME_AUTO_POLL = 16;
    AutoScrollTask autoPollTask;
    private boolean running; //标示是否正在自动轮询
    private boolean canRun;//标示是否可以自动轮询,可在不需要的是否置false

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        autoPollTask = new AutoScrollTask(this);
    }

    static class AutoScrollTask implements Runnable {
        private final WeakReference<AutoScrollRecyclerView> mReference;

        public AutoScrollTask(AutoScrollRecyclerView reference) {
            this.mReference = new WeakReference<AutoScrollRecyclerView>(reference);
        }

        @Override
        public void run() {
            AutoScrollRecyclerView recyclerView = mReference.get();
            if (recyclerView != null && recyclerView.running && recyclerView.canRun) {
                recyclerView.scrollBy(2, 2);
                recyclerView.postDelayed(recyclerView.autoPollTask, AutoScrollRecyclerView.TIME_AUTO_POLL);
            }
        }
    }

    public void start() {
        if (running) {
            stop();
        }
        canRun = true;
        running = true;
        postDelayed(autoPollTask, TIME_AUTO_POLL);
    }

    public void stop() {
        running = false;
        removeCallbacks(autoPollTask);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (running) {
                    stop();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (canRun) {
                    start();
                }
                break;
        }
        return super.onTouchEvent(e);
    }


}
