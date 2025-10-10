package com.wcl.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.wcl.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 高性能 FlowLayout
 * 特点：
 * ✅ 支持左右中对齐
 * ✅ 支持最大行数 / 最大item数
 * ✅ 缓存行信息（避免重复measure）
 * ✅ 子View动态变化后仅重新计算布局
 */
public class FlowLayout extends ViewGroup {

    private int mHorizontalSpacing;
    private int mVerticalSpacing;
    private int mGravity = Gravity.LEFT;

    private static final int MODE_LINES = 0;
    private static final int MODE_NUMBER = 1;
    private int mMaxMode = MODE_LINES;
    private int mMaximum = Integer.MAX_VALUE;

    private final List<LineInfo> mLines = new ArrayList<>();
    private int mLineCount;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_ui_childHorizontalSpacing, 0);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_ui_childVerticalSpacing, 0);
        mGravity = a.getInt(R.styleable.FlowLayout_android_gravity, Gravity.LEFT);
        int maxLines = a.getInt(R.styleable.FlowLayout_android_maxLines, -1);
        int maxNumber = a.getInt(R.styleable.FlowLayout_ui_maxNumber, -1);
        if (maxLines > 0) setMaxLines(maxLines);
        else if (maxNumber > 0) setMaxNumber(maxNumber);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mLines.clear();
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int maxWidth = (widthMode == MeasureSpec.UNSPECIFIED) ? Integer.MAX_VALUE : parentWidth - getPaddingLeft() - getPaddingRight();

        int totalHeight = getPaddingTop();
        int lineWidth = 0, lineHeight = 0;
        LineInfo currentLine = new LineInfo();

        int childCount = getChildCount();
        int measuredItems = 0;

        for (int i = 0; i < childCount; i++) {
            if (mMaxMode == MODE_NUMBER && measuredItems >= mMaximum) break;
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int childW = child.getMeasuredWidth();
            int childH = child.getMeasuredHeight();

            // 超出一行换行
            if (lineWidth + childW > maxWidth && !currentLine.views.isEmpty()) {
                mLines.add(currentLine);
                totalHeight += lineHeight + mVerticalSpacing;

                if (mMaxMode == MODE_LINES && mLines.size() >= mMaximum) break;

                currentLine = new LineInfo();
                lineWidth = 0;
                lineHeight = 0;
            }

            currentLine.views.add(child);
            currentLine.lineWidth = lineWidth + childW;
            lineWidth += childW + mHorizontalSpacing;
            lineHeight = Math.max(lineHeight, childH);
            measuredItems++;
        }

        // 加入最后一行
        if (!currentLine.views.isEmpty() &&
                (mMaxMode != MODE_LINES || mLines.size() < mMaximum)) {
            mLines.add(currentLine);
            totalHeight += lineHeight;
        }

        totalHeight += getPaddingBottom();

        mLineCount = mLines.size();

        int finalWidth = (widthMode == MeasureSpec.EXACTLY) ? parentWidth :
                getPaddingLeft() + getMaxLineWidth() + getPaddingRight();
        int finalHeight = resolveSize(totalHeight, heightMeasureSpec);

        setMeasuredDimension(finalWidth, finalHeight);
    }

    private int getMaxLineWidth() {
        int max = 0;
        for (LineInfo line : mLines) {
            if (line.lineWidth > max) max = line.lineWidth;
        }
        return max;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int x, y = getPaddingTop();

        for (LineInfo line : mLines) {
            int lineHeight = line.getLineHeight();
            int contentWidth = line.lineWidth;
            x = getStartX(r - l, contentWidth);

            for (View child : line.views) {
                if (child.getVisibility() == GONE) continue;
                int w = child.getMeasuredWidth();
                int h = child.getMeasuredHeight();
                child.layout(x, y, x + w, y + h);
                x += w + mHorizontalSpacing;
            }

            y += lineHeight + mVerticalSpacing;
        }

        // 隐藏未布局子View
        for (int i = getVisibleCount(); i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) child.layout(0, 0, 0, 0);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }


    private int getVisibleCount() {
        int count = 0;
        for (LineInfo line : mLines) count += line.views.size();
        return count;
    }

    private int getStartX(int parentWidth, int contentWidth) {
        int available = parentWidth - getPaddingLeft() - getPaddingRight();
        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                return getPaddingLeft() + (available - contentWidth) / 2;
            case Gravity.RIGHT:
                return parentWidth - getPaddingRight() - contentWidth;
            default:
                return getPaddingLeft();
        }
    }

    // =================== public api ===================

    public void setChildHorizontalSpacing(int px) {
        mHorizontalSpacing = px;
        requestLayout();
    }

    public void setChildVerticalSpacing(int px) {
        mVerticalSpacing = px;
        requestLayout();
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
        requestLayout();
    }

    public int getLineCount() {
        return mLineCount;
    }

    public void setMaxLines(int maxLines) {
        mMaxMode = MODE_LINES;
        mMaximum = maxLines;
        requestLayout();
    }

    public void setMaxNumber(int maxNumber) {
        mMaxMode = MODE_NUMBER;
        mMaximum = maxNumber;
        requestLayout();
    }

    // 内部结构
    private static class LineInfo {
        final List<View> views = new ArrayList<>();
        int lineWidth;

        int getLineHeight() {
            int h = 0;
            for (View v : views) {
                if (v.getVisibility() != GONE)
                    h = Math.max(h, v.getMeasuredHeight());
            }
            return h;
        }
    }
}
