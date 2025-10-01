package com.wcl.test.test.sothos;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

public class RecyclerViewExposureTracker {
    private long exposureDelay = 2000L; // 默认 2秒
    private float visibleRatio = 0.5f;  // 默认 50%

    private final RecyclerView recyclerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<Integer> exposedSet = new HashSet<>(); // 已曝光过的item

    private Runnable exposureTask;
    private OnExposureListener listener;

    public interface OnExposureListener {
        void onItemExposed(int position);
    }

    public RecyclerViewExposureTracker(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        init();
    }

    public void setOnExposureListener(OnExposureListener listener) {
        this.listener = listener;
    }

    /** 设置延迟时间（毫秒） */
    public void setExposureDelay(long delayMillis) {
        this.exposureDelay = delayMillis;
    }

    /** 设置可见比例 (0f ~ 1f) */
    public void setVisibleRatio(float ratio) {
        this.visibleRatio = Math.max(0f, Math.min(ratio, 1f));
    }

    private void init() {
        // 监听滚动状态
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scheduleExposureCheck();
                } else {
                    cancelExposureCheck();
                }
            }
        });

        // 初始 attach → 页面刚打开时
        recyclerView.post(this::scheduleExposureCheck);
    }

    private void scheduleExposureCheck() {
        cancelExposureCheck();
        exposureTask = () -> checkExposure(recyclerView);
        handler.postDelayed(exposureTask, exposureDelay);
    }

    private void cancelExposureCheck() {
        if (exposureTask != null) {
            handler.removeCallbacks(exposureTask);
        }
    }

    private void checkExposure(RecyclerView rv) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) {
            return; // 这里只处理 LinearLayoutManager，其他类型可以扩展
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) lm;
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();

        for (int i = first; i <= last; i++) {
            View itemView = layoutManager.findViewByPosition(i);
            if (itemView != null && isVisibleEnough(itemView, rv)) {
                if (!exposedSet.contains(i)) {
                    exposedSet.add(i); // 记录已曝光
                    if (listener != null) {
                        listener.onItemExposed(i);
                    } else {
                        Log.d("Exposure", "Item " + i + " 曝光");
                    }
                }
            }
        }
    }

    private boolean isVisibleEnough(View itemView, RecyclerView recyclerView) {
        int[] rvLocation = new int[2];
        int[] itemLocation = new int[2];
        recyclerView.getLocationOnScreen(rvLocation);
        itemView.getLocationOnScreen(itemLocation);

        int rvTop = rvLocation[1];
        int rvBottom = rvTop + recyclerView.getHeight();

        int itemTop = itemLocation[1];
        int itemBottom = itemTop + itemView.getHeight();

        int visibleHeight = Math.min(itemBottom, rvBottom) - Math.max(itemTop, rvTop);
        return visibleHeight >= itemView.getHeight() * visibleRatio;
    }

    public void reset() {
        exposedSet.clear();
    }
}
