package com.wcl.test.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class DragRelativeLayout extends RelativeLayout {
    public static final int TOUCH_THRESHOLD = 3;
    public int margin_edge;
    private Scroller scroller;
    private float downX, downY;
    private float lastX, lastY;
    private float curX, curY;
    private int lastOffset;
    private int width, height;
    private int viewHeight;
    private int viewWidth;
    private OnClickListener mListener;
    private Rect mRect;

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
//        resolveAttr(context, attrs);
        scroller = new Scroller(getContext());
        margin_edge = 0;
    }

    public void setRectF(Rect rect) {
        mRect = rect;
        height = mRect.height();
        width = mRect.width();
    }

//    private void resolveAttr(Context context, AttributeSet attrs) {
//        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragFrameLayout);
//        margin_edge = array.getDimensionPixelSize(R.styleable.DragFrameLayout_margin_edge, 0);
//        array.recycle();
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getWidth();
        viewHeight = getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scroller.computeScrollOffset()) {
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
                onScrollEdge();
                break;
        }
        return true;
    }

    private void onMove() {
        int dx = (int) (curX - lastX);
        int dy = (int) (curY - lastY);

        if (getLeft() + dx < margin_edge) {
            dx = 0;
        } else if (getLeft() + viewWidth + dx > width - margin_edge) {
            dx = 0;
        }

        if (getTop() + dy < margin_edge) {
            dy = 0;
        } else if (getTop() + viewHeight + dy > height - margin_edge) {
            dy = 0;
        }
        setPosition1(dx, dy);
        //setPosition2(dx, dy);
        //setPosition3(dx, dy);
        //setPosition4(dx, dy);
    }

    private void setPosition1(int dx, int dy) {
        layout(getLeft() + dx, getTop() + dy, getLeft() + viewWidth + dx, getTop() + viewHeight + dy);
    }

    private void setPosition2(int dx, int dy) {
        offsetLeftAndRight(dx);
        offsetTopAndBottom(dy);
    }

    int transX = 0;
    int transY = 0;

    /**
     * 需要修正实现方式
     *
     * @param dx
     * @param dy
     */
    private void setPosition3(int dx, int dy) {
        transX += dx;
        transY += dy;
        setTranslationX(transX);
        setTranslationY(transY);
    }

    private void setPosition4(int dx, int dy) {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        layoutParams.rightMargin = layoutParams.rightMargin - dx;
        layoutParams.topMargin = layoutParams.topMargin + dy;
        setLayoutParams(layoutParams);
    }

    private void onScrollEdge() {
        if (Math.abs(curX - downX) < TOUCH_THRESHOLD && Math.abs(curY - downY) < TOUCH_THRESHOLD) {
            if (mListener != null) {
                mListener.onClick(this);
            }
            return;
        }
//        int dx;
//        if (getLeft() > (width - getRight())) {
//            dx = width - getRight() - margin_edge;
//        } else {
//            dx = margin_edge - getLeft();
//        }
//        lastOffset = 0;
//        scroller.startScroll(getScrollX(), getScrollY(), dx, 0);
//        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int dx = scroller.getCurrX() - lastOffset;
            lastOffset = scroller.getCurrX();
            setPosition1(dx, 0);
            //setPosition2(dx, 0);
            //setPosition3(dx, 0);
            //setPosition4(dx, 0);
            invalidate();
        }
        super.computeScroll();
    }

    public void setOnClickListener2(OnClickListener listener2) {
        this.mListener = listener2;
    }

}
