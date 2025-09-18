package com.wcl.test.main.sothos;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;
// 豆包写的
public class RecyclerViewExposureUtil {

    private static final int EXPOSURE_TIME_THRESHOLD = 2000; // 曝光时间阈值，2秒
    private static final float VISIBLE_PERCENT_THRESHOLD = 0.5f; // 可见比例阈值，50%

    private RecyclerView recyclerView;
    private Handler handler;
    private Set<Integer> exposedPositions;
    private OnExposureListener onExposureListener;

    private boolean isScrolling;
    private int lastScrollState;
    private int currentVisibleItemCount;
    private int currentFirstVisibleItemPosition;

    public RecyclerViewExposureUtil(@NonNull RecyclerView recyclerView, @NonNull OnExposureListener onExposureListener) {
        this.recyclerView = recyclerView;
        this.handler = new Handler(Looper.getMainLooper());
        this.exposedPositions = new HashSet<>();
        this.onExposureListener = onExposureListener;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastScrollState = newState;
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    isScrolling = false;
                    checkExposure();
                } else {
                    isScrolling = true;
                    handler.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    currentVisibleItemCount = layoutManager.getChildCount();
                    currentFirstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                }
            }
        });

        // 初始化时检查曝光
        handler.postDelayed(this::checkExposure, EXPOSURE_TIME_THRESHOLD);
    }

    private void checkExposure() {
        if (isScrolling) {
            return;
        }
        handler.postDelayed(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            for (int i = 0; i < currentVisibleItemCount; i++) {
                int position = currentFirstVisibleItemPosition + i;
                if (exposedPositions.contains(position)) {
                    continue;
                }
                View child = recyclerView.getChildAt(i);
                if (isItemExposed(child)) {
                    exposedPositions.add(position);
                    onExposureListener.onItemExposed(position);
                }
            }
        }, EXPOSURE_TIME_THRESHOLD);
    }

    private boolean isItemExposed(View itemView) {
        if (itemView == null) {
            return false;
        }
        int itemHeight = itemView.getHeight();
        int itemTop = itemView.getTop();
        int itemBottom = itemView.getBottom();
        int visibleHeight = Math.max(0, Math.min(itemBottom, recyclerView.getHeight()) - Math.max(0, itemTop));
        return (float) visibleHeight / itemHeight >= VISIBLE_PERCENT_THRESHOLD;
    }

    public interface OnExposureListener {
        void onItemExposed(int position);
    }
}