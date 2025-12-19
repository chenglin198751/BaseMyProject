package com.wcl.test.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;

public class FlowLayout extends ViewGroup {

    private int mChildHorizontalSpacing;
    private int mChildVerticalSpacing;
    private int mGravity;
    private boolean mClipLastItem;

    private static final int LINES = 0;
    private static final int NUMBER = 1;
    private int mMaxMode = LINES;
    private int mMaximum = Integer.MAX_VALUE;
    private int mLineCount = 0;
    private OnLineCountChangeListener mOnLineCountChangeListener;

    private int[] mItemNumberInEachLine;
    private int[] mWidthSumInEachLine;
    private int measuredChildCount;

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
        mChildHorizontalSpacing = array.getDimensionPixelSize(R.styleable.FlowLayout_ui_childHorizontalSpacing, 0);
        mChildVerticalSpacing = array.getDimensionPixelSize(R.styleable.FlowLayout_ui_childVerticalSpacing, 0);
        mGravity = array.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.LEFT);
        mClipLastItem = array.getBoolean(R.styleable.FlowLayout_ui_clipLastItem, false);

        int maxLines = array.getInt(R.styleable.FlowLayout_android_maxLines, -1);
        if (maxLines >= 0) {
            setMaxLines(maxLines);
        }

        int maxNumber = array.getInt(R.styleable.FlowLayout_ui_maxNumber, -1);
        if (maxNumber >= 0) {
            setMaxNumber(maxNumber);
        }
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int maxLineHeight = 0;
        int resultWidth;
        int resultHeight;

        final int count = getChildCount();

        mItemNumberInEachLine = new int[count];
        mWidthSumInEachLine = new int[count];
        int lineIndex = 0;

        measuredChildCount = 0;

        int childPositionX = getPaddingLeft();
        int childPositionY = getPaddingTop();
        int childMaxRight = (widthSpecMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : widthSpecSize - getPaddingRight());

        for (int i = 0; i < count; i++) {
            if (mMaxMode == NUMBER && measuredChildCount >= mMaximum) break;
            if (mMaxMode == LINES && lineIndex >= mMaximum) break;

            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            LayoutParams lp = child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), lp.height);
            child.measure(childWidthSpec, childHeightSpec);

            int childw = child.getMeasuredWidth();
            int childh = child.getMeasuredHeight();
            maxLineHeight = Math.max(maxLineHeight, childh);

            // 关键逻辑：clipLastItem 严格裁剪最后一个 item
            if (mClipLastItem && childPositionX + childw > childMaxRight) {
                break; // 最后一个 item 放不下，直接舍弃
            }

            // 换行判断（非第一行）
            if (childPositionX + childw > childMaxRight) {
                lineIndex++;
                if (mMaxMode == LINES && lineIndex >= mMaximum) break;
                childPositionX = getPaddingLeft();
                childPositionY += maxLineHeight + mChildVerticalSpacing;
                maxLineHeight = childh;
            }

            mItemNumberInEachLine[lineIndex]++;
            mWidthSumInEachLine[lineIndex] += childw + mChildHorizontalSpacing;
            childPositionX += childw + mChildHorizontalSpacing;
            measuredChildCount++;
        }

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            resultWidth = widthSpecSize;
        } else {
            resultWidth = childPositionX + getPaddingRight();
        }

        if (heightSpecMode == MeasureSpec.UNSPECIFIED) {
            resultHeight = childPositionY + maxLineHeight + getPaddingBottom();
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            resultHeight = Math.min(childPositionY + maxLineHeight + getPaddingBottom(), heightSpecSize);
        } else {
            resultHeight = heightSpecSize;
        }

        setMeasuredDimension(resultWidth, resultHeight);

        int measureLineCount = lineIndex + 1;
        if (mLineCount != measureLineCount && mOnLineCountChangeListener != null) {
            mOnLineCountChangeListener.onChange(mLineCount, measureLineCount);
        }
        mLineCount = measureLineCount;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                layoutWithGravityLeft(width);
                break;
            case Gravity.RIGHT:
                layoutWithGravityRight(width);
                break;
            case Gravity.CENTER_HORIZONTAL:
                layoutWithGravityCenter(width);
                break;
            default:
                layoutWithGravityLeft(width);
        }
    }

    private void layoutWithGravityLeft(int parentWidth) {
        int childMaxRight = parentWidth - getPaddingRight();
        int childPositionX = getPaddingLeft();
        int childPositionY = getPaddingTop();
        int lineHeight = 0;
        int layoutChildCount = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            if (layoutChildCount >= measuredChildCount) {
                child.layout(0, 0, 0, 0);
                continue;
            }

            int childw = child.getMeasuredWidth();
            int childh = child.getMeasuredHeight();

            // clipLastItem 严格裁剪
            if (mClipLastItem && childPositionX + childw > childMaxRight) {
                child.layout(0, 0, 0, 0);
                continue;
            }

            if (childPositionX + childw > childMaxRight) {
                childPositionX = getPaddingLeft();
                childPositionY += lineHeight + mChildVerticalSpacing;
                lineHeight = 0;
            }

            child.layout(childPositionX, childPositionY, childPositionX + childw, childPositionY + childh);
            childPositionX += childw + mChildHorizontalSpacing;
            lineHeight = Math.max(lineHeight, childh);
            layoutChildCount++;
        }
    }

    private void layoutWithGravityRight(int parentWidth) {
        int nextChildIndex = 0;
        int nextChildPositionY = getPaddingTop();
        int lineHeight = 0;
        int layoutChildCount = 0;
        int layoutChildEachLine = 0;

        for (int i = 0; i < mItemNumberInEachLine.length; i++) {
            if (mItemNumberInEachLine[i] == 0) break;

            int nextChildPositionX = parentWidth - getPaddingRight() - mWidthSumInEachLine[i];

            layoutChildEachLine = 0;
            while (layoutChildEachLine < mItemNumberInEachLine[i]) {
                if (nextChildIndex >= getChildCount()) break;
                final View child = getChildAt(nextChildIndex);
                if (child.getVisibility() == GONE) {
                    nextChildIndex++;
                    continue;
                }

                if (mClipLastItem && nextChildPositionX + child.getMeasuredWidth() > parentWidth - getPaddingRight()) {
                    child.layout(0, 0, 0, 0);
                    nextChildIndex++;
                    continue;
                }

                child.layout(nextChildPositionX, nextChildPositionY,
                        nextChildPositionX + child.getMeasuredWidth(),
                        nextChildPositionY + child.getMeasuredHeight());
                nextChildPositionX += child.getMeasuredWidth() + mChildHorizontalSpacing;
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight());
                layoutChildEachLine++;
                layoutChildCount++;
                nextChildIndex++;
                if (layoutChildCount >= measuredChildCount) break;
            }

            nextChildPositionY += lineHeight + mChildVerticalSpacing;
            lineHeight = 0;
        }

        // 多余的子View置0
        for (int i = nextChildIndex; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) child.layout(0, 0, 0, 0);
        }
    }

    private void layoutWithGravityCenter(int parentWidth) {
        int nextChildIndex = 0;
        int nextChildPositionY = getPaddingTop();
        int lineHeight = 0;
        int layoutChildCount = 0;

        for (int i = 0; i < mItemNumberInEachLine.length; i++) {
            if (mItemNumberInEachLine[i] == 0) break;

            int nextChildPositionX = getPaddingLeft() + (parentWidth - getPaddingLeft() - getPaddingRight() - mWidthSumInEachLine[i]) / 2;
            int layoutChildEachLine = 0;

            while (layoutChildEachLine < mItemNumberInEachLine[i]) {
                if (nextChildIndex >= getChildCount()) break;
                final View child = getChildAt(nextChildIndex);
                if (child.getVisibility() == GONE) {
                    nextChildIndex++;
                    continue;
                }

                if (mClipLastItem && nextChildPositionX + child.getMeasuredWidth() > parentWidth - getPaddingRight()) {
                    child.layout(0, 0, 0, 0);
                    nextChildIndex++;
                    continue;
                }

                child.layout(nextChildPositionX, nextChildPositionY,
                        nextChildPositionX + child.getMeasuredWidth(),
                        nextChildPositionY + child.getMeasuredHeight());
                nextChildPositionX += child.getMeasuredWidth() + mChildHorizontalSpacing;
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight());
                layoutChildEachLine++;
                layoutChildCount++;
                nextChildIndex++;
                if (layoutChildCount >= measuredChildCount) break;
            }

            nextChildPositionY += lineHeight + mChildVerticalSpacing;
            lineHeight = 0;
        }

        for (int i = nextChildIndex; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) child.layout(0, 0, 0, 0);
        }
    }

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
        mMaxMode = NUMBER;
        requestLayout();
    }

    public int getMaxNumber() {
        return mMaxMode == NUMBER ? mMaximum : -1;
    }

    public void setMaxLines(int maxLines) {
        mMaximum = maxLines;
        mMaxMode = LINES;
        requestLayout();
    }

    public int getMaxLines() {
        return mMaxMode == LINES ? mMaximum : -1;
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
