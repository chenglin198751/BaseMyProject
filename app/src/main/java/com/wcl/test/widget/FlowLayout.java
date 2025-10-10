package com.wcl.test.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 优化版 FlowLayout（自动换行布局）
 * 支持特性：
 * - 控制水平/垂直间距
 * - 控制对齐方式（左/中/右）
 * - 控制最大行数或最大 item 数
 * - 行数变化监听
 */
public class FlowLayout extends ViewGroup {

    private int mChildHorizontalSpacing;
    private int mChildVerticalSpacing;
    private int mGravity;

    private static final int MODE_LINES = 0;
    private static final int MODE_NUMBER = 1;

    private int mMaxMode = MODE_LINES;
    private int mMaximum = Integer.MAX_VALUE;
    private int mLineCount = 0;
    private OnLineCountChangeListener mOnLineCountChangeListener;

    private final List<Integer> mLineItemCounts = new ArrayList<>();
    private final List<Integer> mLineWidthSums = new ArrayList<>();

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mChildHorizontalSpacing = array.getDimensionPixelSize(
                R.styleable.FlowLayout_ui_childHorizontalSpacing, 0);
        mChildVerticalSpacing = array.getDimensionPixelSize(
                R.styleable.FlowLayout_ui_childVerticalSpacing, 0);
        mGravity = array.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.LEFT);

        int maxLines = array.getInt(R.styleable.FlowLayout_android_maxLines, -1);
        if (maxLines >= 0) setMaxLines(maxLines);

        int maxNumber = array.getInt(R.styleable.FlowLayout_ui_maxNumber, -1);
        if (maxNumber >= 0) setMaxNumber(maxNumber);

        array.recycle();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int maxLineHeight = 0;
        int totalHeight = getPaddingTop();
        int lineWidth = getPaddingLeft();
        int parentWidth = (widthMode == MeasureSpec.EXACTLY) ? widthSize : Integer.MAX_VALUE;

        mLineItemCounts.clear();
        mLineWidthSums.clear();

        int childCount = getChildCount();
        int measuredCount = 0;

        for (int i = 0; i < childCount; i++) {
            if (mMaxMode == MODE_NUMBER && measuredCount >= mMaximum) break;
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            int childW = child.getMeasuredWidth();
            int childH = child.getMeasuredHeight();

            // 换行判断
            if (lineWidth + childW + getPaddingRight() > parentWidth) {
                mLineItemCounts.add(mLineWidthSums.size(), measuredCount - getTotalItems());
                mLineWidthSums.add(lineWidth - mChildHorizontalSpacing - getPaddingLeft());
                totalHeight += maxLineHeight + mChildVerticalSpacing;
                lineWidth = getPaddingLeft();
                maxLineHeight = 0;
                if (mMaxMode == MODE_LINES && mLineWidthSums.size() >= mMaximum) break;
            }

            lineWidth += childW + mChildHorizontalSpacing;
            maxLineHeight = Math.max(maxLineHeight, childH);
            measuredCount++;
        }

        // 最后一行补上
        if (measuredCount > getTotalItems()) {
            mLineItemCounts.add(measuredCount - getTotalItems());
            mLineWidthSums.add(lineWidth - mChildHorizontalSpacing - getPaddingLeft());
            totalHeight += maxLineHeight;
        }

        totalHeight += getPaddingBottom();

        int finalWidth = (widthMode == MeasureSpec.EXACTLY) ? widthSize
                : Math.min(widthSize, lineWidth + getPaddingRight());
        int finalHeight = (heightMode == MeasureSpec.EXACTLY) ? heightSize
                : Math.min(totalHeight, heightSize);

        setMeasuredDimension(finalWidth, finalHeight);

        int newLineCount = mLineWidthSums.size();
        if (mLineCount != newLineCount && mOnLineCountChangeListener != null) {
            mOnLineCountChangeListener.onChange(mLineCount, newLineCount);
        }
        mLineCount = newLineCount;
    }

    private int getTotalItems() {
        int total = 0;
        for (int c : mLineItemCounts) total += c;
        return total;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int parentWidth = r - l;
        int childIndex = 0;
        int y = getPaddingTop();

        for (int line = 0; line < mLineItemCounts.size(); line++) {
            int countInLine = mLineItemCounts.get(line);
            int lineWidth = mLineWidthSums.get(line);
            int x = getStartXByGravity(parentWidth, lineWidth);

            int lineHeight = 0;
            for (int i = 0; i < countInLine && childIndex < getChildCount(); i++) {
                View child = getChildAt(childIndex++);
                if (child.getVisibility() == GONE) {
                    i--;
                    continue;
                }
                int w = child.getMeasuredWidth();
                int h = child.getMeasuredHeight();
                child.layout(x, y, x + w, y + h);
                x += w + mChildHorizontalSpacing;
                lineHeight = Math.max(lineHeight, h);
            }
            y += lineHeight + mChildVerticalSpacing;
        }

        // 把剩余子View隐藏
        for (; childIndex < getChildCount(); childIndex++) {
            View child = getChildAt(childIndex);
            if (child.getVisibility() != GONE) {
                child.layout(0, 0, 0, 0);
            }
        }
    }

    private int getStartXByGravity(int parentWidth, int lineWidth) {
        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                return getPaddingLeft() + (parentWidth - getPaddingLeft() - getPaddingRight() - lineWidth) / 2;
            case Gravity.RIGHT:
                return parentWidth - getPaddingRight() - lineWidth;
            default:
                return getPaddingLeft();
        }
    }

    // ================== 公共方法 ==================

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return mGravity;
    }

    public void setMaxNumber(int maxNumber) {
        mMaximum = maxNumber;
        mMaxMode = MODE_NUMBER;
        requestLayout();
    }

    public int getMaxNumber() {
        return mMaxMode == MODE_NUMBER ? mMaximum : -1;
    }

    public void setMaxLines(int maxLines) {
        mMaximum = maxLines;
        mMaxMode = MODE_LINES;
        requestLayout();
    }

    public int getMaxLines() {
        return mMaxMode == MODE_LINES ? mMaximum : -1;
    }

    public void setChildHorizontalSpacing(int spacing) {
        mChildHorizontalSpacing = spacing;
        requestLayout();
    }

    public void setChildVerticalSpacing(int spacing) {
        mChildVerticalSpacing = spacing;
        requestLayout();
    }

    public void setOnLineCountChangeListener(OnLineCountChangeListener listener) {
        mOnLineCountChangeListener = listener;
    }

    public int getLineCount() {
        return mLineCount;
    }

    public interface OnLineCountChangeListener {
        void onChange(int oldLineCount, int newLineCount);
    }
}
