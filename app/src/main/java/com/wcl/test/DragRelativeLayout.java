package com.wcl.test;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wcl.test.utils.AppLogUtils;


public class DragRelativeLayout extends RelativeLayout {
    private static final int TOUCH_THRESHOLD = 10;
    private static final int MARGIN_EDGE = 0;
    private float downX, downY;
    private float lastX, lastY;
    private float curX, curY;
    private int mParentWidth, mParentHeight;
    private OnClickListener mListener;

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

    //设置父布局view,在哪个父布局内移动
    public void setParentView(View parent) {
        parent.post(() -> {
            Rect rect = new Rect();
            parent.getGlobalVisibleRect(rect);
            mParentHeight = rect.height();//可见部分的高度
            mParentWidth = rect.width();//可见部分的宽度
            AppLogUtils.d("DragRelativeLayout", "父view 可见部分宽高:" + mParentHeight + "---" + mParentWidth);

            AppLogUtils.d("DragRelativeLayout", "父view : " + "上边界距离屏幕顶部" + rect.top + "，下边界距离屏幕顶部"
                    + rect.bottom + "，左边界距离屏幕左边" + rect.left + "，右边界距离屏幕左边" + rect.right);

            int[] location = new int[2];
            parent.getLocationInWindow(location);
            int x = location[0]; // view距离window 左边的距离（即x轴方向）
            int y = location[1]; // view距离window 顶边的距离（即y轴方向）
            AppLogUtils.d("DragRelativeLayout", " view距离window的宽高:" + x + "---" + y);
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = getLeft();
                layoutParams.topMargin = getTop();
                layoutParams.setMargins(getLeft(), getTop(), 0, 0);
                setLayoutParams(layoutParams);
                onScrollEdge();
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


    private void onScrollEdge() {
        if (Math.abs(curX - downX) < TOUCH_THRESHOLD && Math.abs(curY - downY) < TOUCH_THRESHOLD) {
            if (mListener != null) {
                mListener.onClick(this);
            }
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

}
