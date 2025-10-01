package com.wcl.test.widget.banner;

import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViewPager2SlowScrollHelper {

    private ViewPager2 vp;
    private long duration;
    private RecyclerView recyclerView;
    private Object mAccessibilityProvider;
    private Object mScrollEventAdapter;
    private Method onSetNewCurrentItemMethod;
    private Method getRelativeScrollPositionMethod;
    private Method notifyProgrammaticScrollMethod;

    public ViewPager2SlowScrollHelper(ViewPager2 vp, long duration) {
        this.vp = vp;
        this.duration = duration;
        try {
            Field mRecyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            mRecyclerViewField.setAccessible(true);
            recyclerView = (RecyclerView) mRecyclerViewField.get(vp);

            Field mAccessibilityProviderField = ViewPager2.class.getDeclaredField("mAccessibilityProvider");
            mAccessibilityProviderField.setAccessible(true);
            mAccessibilityProvider = mAccessibilityProviderField.get(vp);

            onSetNewCurrentItemMethod = mAccessibilityProvider.getClass().getDeclaredMethod("onSetNewCurrentItem");
            onSetNewCurrentItemMethod.setAccessible(true);

            Field mScrollEventAdapterField = ViewPager2.class.getDeclaredField("mScrollEventAdapter");
            mScrollEventAdapterField.setAccessible(true);
            mScrollEventAdapter = mScrollEventAdapterField.get(vp);

            getRelativeScrollPositionMethod = mScrollEventAdapter.getClass().getDeclaredMethod("getRelativeScrollPosition");
            getRelativeScrollPositionMethod.setAccessible(true);

            notifyProgrammaticScrollMethod = mScrollEventAdapter.getClass().getDeclaredMethod("notifyProgrammaticScroll", int.class, boolean.class);
            notifyProgrammaticScrollMethod.setAccessible(true);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            Toast.makeText(vp.getContext(), "初始化ViewPager2SlowScrollHelper失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCurrentItem(int item) {
        item = Math.min(Math.max(item, 0), vp.getAdapter().getItemCount() - 1);
        if (item == vp.getCurrentItem() && vp.getScrollState() == ViewPager2.SCROLL_STATE_IDLE) {
            return;
        }
        if (item == vp.getCurrentItem()) {
            return;
        }
        vp.setCurrentItem(item);
        try {
            onSetNewCurrentItemMethod.invoke(mAccessibilityProvider);
            notifyProgrammaticScrollMethod.invoke(mScrollEventAdapter, item, true);
            smoothScrollToPosition(item, vp.getContext(), recyclerView.getLayoutManager());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(vp.getContext(), "设置ViewPager2当前页失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void smoothScrollToPosition(int item, Context context, RecyclerView.LayoutManager layoutManager) {
        RecyclerView.SmoothScroller linearSmoothScroller = getSlowLinearSmoothScroller(context);
        replaceDecelerateInterpolator(linearSmoothScroller);
        linearSmoothScroller.setTargetPosition(item);
        layoutManager.startSmoothScroll(linearSmoothScroller);
    }

    private RecyclerView.SmoothScroller getSlowLinearSmoothScroller(Context context) {
        return new LinearSmoothScroller(context) {
            @Override
            public float calculateSpeedPerPixel(@NonNull android.util.DisplayMetrics displayMetrics) {
                return duration / (vp.getWidth() * 3.0f);
            }
        };
    }

    private void replaceDecelerateInterpolator(RecyclerView.SmoothScroller linearSmoothScroller) {
        try {
            Field mDecelerateInterpolatorField = LinearSmoothScroller.class.getDeclaredField("mDecelerateInterpolator");
            mDecelerateInterpolatorField.setAccessible(true);
            mDecelerateInterpolatorField.set(linearSmoothScroller, new DecelerateInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    return input;
                }
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Toast.makeText(vp.getContext(), "替换差值器失败", Toast.LENGTH_SHORT).show();
        }
    }
}