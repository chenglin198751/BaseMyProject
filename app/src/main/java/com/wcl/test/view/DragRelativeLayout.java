package com.wcl.test.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class DragRelativeLayout extends RelativeLayout {
    private static final int TOUCH_THRESHOLD = 10;
    private static final int MARGIN_EDGE = 0;
    private float downX, downY;
    private float lastX, lastY;
    private float curX, curY;
    private int mParentWidth, mParentHeight;
    private OnClickListener mListener;
    private ValueAnimator mScrollAnimator;

    public DragRelativeLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public DragRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DragRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            mParentWidth = parent.getWidth();
            mParentHeight = parent.getHeight();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimatorRunning()) {
            return super.onTouchEvent(event);
        }

        curX = event.getRawX();
        curY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = event.getRawX();
                downY = lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                onMove();
                lastX = curX;
                lastY = curY;
                break;
            case MotionEvent.ACTION_UP:
                performViewClick();
                scrollToEdgeAnimation();
                break;
        }
        return true;
    }


    private void onMove() {
        float dx = curX - lastX;
        float dy = curY - lastY;

        int newLeft = (int) (getLeft() + dx);
        int newTop = (int) (getTop() + dy);
        int newRight = (int) (getRight() + dx);
        int newBottom = (int) (getBottom() + dy);

        // 限制拖动范围在父 View 内
        if (newLeft < 0) {
            newLeft = 0;
            newRight = getWidth();
        } else if (newRight > mParentWidth) {
            newRight = mParentWidth;
            newLeft = mParentWidth - getWidth();
        }

        if (newTop < 0) {
            newTop = 0;
            newBottom = getHeight();
        } else if (newBottom > mParentHeight) {
            newBottom = mParentHeight;
            newTop = mParentHeight - getHeight();
        }

        layout(newLeft, newTop, newRight, newBottom);
        lastX = curX;
        lastY = curY;
    }

    // 松手后滚动到屏幕边缘
    private void scrollToEdgeAnimation() {
        int targetLeft;

        if (getLeft() < mParentWidth / 2 - getWidth() / 2) {
            targetLeft = 0;
        } else {
            targetLeft = mParentWidth;
        }

        mScrollAnimator = ValueAnimator.ofFloat(curX, targetLeft);
        mScrollAnimator.addUpdateListener(animation -> {
            curX = (float) animation.getAnimatedValue();
            onMove();
            lastX = curX;
        });
        mScrollAnimator.setDuration(300);
        mScrollAnimator.start();
    }

    // 拖拽松手后，执行view的点击事件
    private void performViewClick() {
        if (Math.abs(curX - downX) < TOUCH_THRESHOLD && Math.abs(curY - downY) < TOUCH_THRESHOLD) {
            if (mListener != null) {
                if (isAnimatorRunning()) {
                    return;
                }
                mListener.onClick(this);
            }
        }
    }

    private boolean isAnimatorRunning() {
        return mScrollAnimator != null && mScrollAnimator.isRunning();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

}
