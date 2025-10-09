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

/**
 * 可拖拽的 RelativeLayout，支持拖动后自动吸附到屏幕边缘
 * 优化点：
 * 1. 修复了滑动到边缘的动画逻辑错误
 * 2. 使用系统触摸阈值代替硬编码
 * 3. 添加动画插值器，提升用户体验
 * 4. 修复内存泄漏风险
 * 5. 优化边界检测逻辑
 * 6. 添加可配置的边距参数
 */
public class DragRelativeLayout extends RelativeLayout {
    // 使用系统触摸阈值更合理
    private int mTouchSlop;
    // 吸附到边缘时的边距
    private int mMarginEdge = 0;
    // 动画持续时间
    private static final int ANIMATION_DURATION = 300;

    private float downX, downY;
    private float lastX, lastY;
    private int mParentWidth, mParentHeight;
    private OnClickListener mListener;
    private ValueAnimator mScrollAnimator;
    // 标记是否正在拖动
    private boolean isDragging = false;

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
        // 使用系统定义的触摸阈值
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateParentSize();
    }

    /**
     * 更新父容器尺寸
     */
    private void updateParentSize() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            mParentWidth = parent.getWidth();
            mParentHeight = parent.getHeight();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 动画运行时不响应触摸
        if (isAnimatorRunning()) {
            return super.onTouchEvent(event);
        }

        float rawX = event.getRawX();
        float rawY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = rawX;
                downY = lastY = rawY;
                isDragging = false;
                cancelAnimation();
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = rawX - lastX;
                float dy = rawY - lastY;

                // 判断是否开始拖动
                if (!isDragging) {
                    float totalDx = Math.abs(rawX - downX);
                    float totalDy = Math.abs(rawY - downY);
                    if (totalDx > mTouchSlop || totalDy > mTouchSlop) {
                        isDragging = true;
                    }
                }

                if (isDragging) {
                    moveView(dx, dy);
                }

                lastX = rawX;
                lastY = rawY;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 先判断点击，再执行动画
                performViewClick();
                if (isDragging) {
                    scrollToEdgeAnimation();
                }
                isDragging = false;
                break;
        }
        return true;
    }

    /**
     * 移动 View
     */
    private void moveView(float dx, float dy) {
        int left = getLeft();
        int top = getTop();
        int right = getRight();
        int bottom = getBottom();

        int newLeft = (int) (left + dx);
        int newTop = (int) (top + dy);
        int newRight = (int) (right + dx);
        int newBottom = (int) (bottom + dy);

        // 限制拖动范围在父 View 内
        if (newLeft < mMarginEdge) {
            newLeft = mMarginEdge;
            newRight = mMarginEdge + getWidth();
        } else if (newRight > mParentWidth - mMarginEdge) {
            newRight = mParentWidth - mMarginEdge;
            newLeft = mParentWidth - mMarginEdge - getWidth();
        }

        if (newTop < mMarginEdge) {
            newTop = mMarginEdge;
            newBottom = mMarginEdge + getHeight();
        } else if (newBottom > mParentHeight - mMarginEdge) {
            newBottom = mParentHeight - mMarginEdge;
            newTop = mParentHeight - mMarginEdge - getHeight();
        }

        layout(newLeft, newTop, newRight, newBottom);
    }

    /**
     * 松手后滚动到屏幕边缘（左边或右边）
     */
    private void scrollToEdgeAnimation() {
        // 确保父容器尺寸已更新
        updateParentSize();

        int currentLeft = getLeft();
        int targetLeft;

        // 判断靠近左边还是右边
        if (currentLeft < (mParentWidth - getWidth()) / 2) {
            targetLeft = mMarginEdge;
        } else {
            targetLeft = mParentWidth - getWidth() - mMarginEdge;
        }

        // 如果已经在目标位置，不需要动画
        if (currentLeft == targetLeft) {
            return;
        }

        // 使用属性动画平滑移动
        mScrollAnimator = ValueAnimator.ofInt(currentLeft, targetLeft);
        mScrollAnimator.addUpdateListener(animation -> {
            int left = (int) animation.getAnimatedValue();
            int top = getTop();
            layout(left, top, left + getWidth(), top + getHeight());
        });
        mScrollAnimator.setDuration(ANIMATION_DURATION);
        // 添加减速插值器，让动画更自然
        mScrollAnimator.setInterpolator(new DecelerateInterpolator());
        mScrollAnimator.start();
    }

    /**
     * 拖拽松手后，执行 view 的点击事件
     */
    private void performViewClick() {
        float dx = Math.abs(lastX - downX);
        float dy = Math.abs(lastY - downY);

        // 移动距离小于阈值，认为是点击
        if (dx < mTouchSlop && dy < mTouchSlop) {
            if (mListener != null && !isAnimatorRunning()) {
                mListener.onClick(this);
            }
        }
    }

    /**
     * 取消动画
     */
    private void cancelAnimation() {
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.cancel();
        }
    }

    /**
     * 判断动画是否正在运行
     */
    private boolean isAnimatorRunning() {
        return mScrollAnimator != null && mScrollAnimator.isRunning();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置吸附到边缘时的边距
     */
    public void setMarginEdge(int marginEdge) {
        this.mMarginEdge = marginEdge;
    }

    /**
     * 获取边距
     */
    public int getMarginEdge() {
        return mMarginEdge;
    }
}