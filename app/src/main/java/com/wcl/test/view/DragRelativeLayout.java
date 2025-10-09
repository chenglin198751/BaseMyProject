package com.wcl.test.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DragRelativeLayout extends RelativeLayout {
    private static final int MARGIN_EDGE = 0; // 边距，可按需设置
    private final int touchSlop; // 系统级点击阈值

    private float downX, downY;
    private float lastX, lastY;
    private int parentWidth, parentHeight;
    private OnClickListener mClickListener;
    private ValueAnimator mScrollAnimator;

    public DragRelativeLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimator();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parentWidth = parent.getWidth();
            parentHeight = parent.getHeight();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimatorRunning()) return false;

        final float curX = event.getRawX();
        final float curY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = curX;
                downY = lastY = curY;
                cancelAnimator(); // 防止动画未结束时又被触摸
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = curX - lastX;
                float dy = curY - lastY;
                moveBy(dx, dy);
                lastX = curX;
                lastY = curY;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isClick(downX, downY, curX, curY)) {
                    if (mClickListener != null) mClickListener.onClick(this);
                } else {
                    animateToEdge();
                }
                break;
        }
        return true;
    }

    /**
     * 判断是否是点击而非拖拽
     */
    private boolean isClick(float downX, float downY, float upX, float upY) {
        return Math.abs(upX - downX) < touchSlop && Math.abs(upY - downY) < touchSlop;
    }

    /**
     * 移动视图，限制在父容器内
     */
    private void moveBy(float dx, float dy) {
        if (parentWidth == 0 || parentHeight == 0) return;

        int left = getLeft() + (int) dx;
        int top = getTop() + (int) dy;

        left = Math.max(MARGIN_EDGE, Math.min(left, parentWidth - getWidth() - MARGIN_EDGE));
        top = Math.max(MARGIN_EDGE, Math.min(top, parentHeight - getHeight() - MARGIN_EDGE));

        layout(left, top, left + getWidth(), top + getHeight());
    }

    /**
     * 松手后自动吸附到左右边缘
     */
    private void animateToEdge() {
        if (parentWidth == 0) return;

        final int startLeft = getLeft();
        final int endLeft = (getLeft() + getWidth() / 2f < parentWidth / 2f)
                ? MARGIN_EDGE
                : parentWidth - getWidth() - MARGIN_EDGE;

        if (startLeft == endLeft) return;

        mScrollAnimator = ValueAnimator.ofInt(startLeft, endLeft);
        mScrollAnimator.setDuration(300);
        mScrollAnimator.setInterpolator(new DecelerateInterpolator());
        mScrollAnimator.addUpdateListener(animation -> {
            int curLeft = (int) animation.getAnimatedValue();
            layout(curLeft, getTop(), curLeft + getWidth(), getBottom());
        });
        mScrollAnimator.start();
    }

    private boolean isAnimatorRunning() {
        return mScrollAnimator != null && mScrollAnimator.isRunning();
    }

    private void cancelAnimator() {
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.mClickListener = listener;
    }
}
