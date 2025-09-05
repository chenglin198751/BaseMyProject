package com.wcl.test.main.sothos;

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
    private static final long EXPOSURE_DELAY = 2000L; // 2秒
    private final RecyclerView recyclerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<Integer> exposedSet = new HashSet<>(); // 记录已曝光的item

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

    private void init() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 停止滚动 → 延迟2秒执行曝光检测
                    if (exposureTask != null) {
                        handler.removeCallbacks(exposureTask);
                    }

                    exposureTask = () -> checkExposure(rv);
                    handler.postDelayed(exposureTask, EXPOSURE_DELAY);

                } else {
                    // 滚动中 → 取消检测
                    if (exposureTask != null) {
                        handler.removeCallbacks(exposureTask);
                    }
                }
            }
        });
    }

    private void checkExposure(RecyclerView rv) {
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) {
            return; // 这里只处理 LinearLayoutManager，其他可扩展
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) lm;
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();

        for (int i = first; i <= last; i++) {
            View itemView = layoutManager.findViewByPosition(i);
            if (itemView != null && isVisibleEnough(itemView, rv)) {
                if (!exposedSet.contains(i)) {
                    exposedSet.add(i); // 标记已曝光
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
        return visibleHeight >= itemView.getHeight() / 2; // 至少50%可见才算
    }

    public void reset() {
        exposedSet.clear();
    }
}
