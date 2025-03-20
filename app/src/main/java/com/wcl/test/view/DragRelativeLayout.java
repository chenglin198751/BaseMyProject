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
        //x方向上移动的距离,dx为负数，向左移
        int dx = (int) (curX - lastX);
        //y方向上移动的距离.dy位负数，向上移
        int dy = (int) (curY - lastY);

        int left = getLeft() + dx;
        int right = getRight() + dx;
        int top = getTop() + dy;
        int bottom = getBottom() + dy;

        //如果移动到了屏幕的左边
        if (left < 0) {
            left = 0;
            right = left + getWidth();
        }

        //如果移动到了屏幕的最右边
        if (right > mParentWidth) {
            right = mParentWidth;
            left = right - getWidth();
        }

        //如果移动到了父布局的最顶部
        if (top < MARGIN_EDGE) {
            dy = 0;
        }
        //如果移动到了父布局的最底部
        if (bottom > mParentHeight - MARGIN_EDGE) {
            dy = 0;
        }
        layout(left, getTop() + dy, right, getBottom() + dy);
    }

    // 松手后滚动到屏幕边缘
    private void scrollToEdgeAnimation() {
        mScrollAnimator = ValueAnimator.ofFloat(curX, 0f);
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
