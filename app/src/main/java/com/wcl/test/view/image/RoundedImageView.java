package com.wcl.test.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

public class RoundedImageView extends AppCompatImageView {

    protected float cornerRadius = 0f;
    protected boolean isOval = false;
    protected float aspectRatio = 0f;
    protected float borderWidth = 0f;
    protected int borderColor = Color.TRANSPARENT;
    protected int solidColor = Color.TRANSPARENT;
    private GradientDrawable backgroundDrawable;

    public RoundedImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
            cornerRadius = a.getDimension(R.styleable.RoundedImageView_riv_corner_radius, 0f);
            isOval = a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);
            aspectRatio = a.getFloat(R.styleable.RoundedImageView_riv_aspect_ratio, 0f);
            borderWidth = a.getDimension(R.styleable.RoundedImageView_riv_border_width, 0f);
            borderColor = a.getColor(R.styleable.RoundedImageView_riv_border_color, Color.TRANSPARENT);
            solidColor = a.getColor(R.styleable.RoundedImageView_riv_solid_color, Color.TRANSPARENT);
            a.recycle();
        }

        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        applyDrawableState();
        setBackground(backgroundDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (aspectRatio <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        boolean widthIsZero = widthMode == MeasureSpec.EXACTLY && widthSize == 0;
        boolean heightIsZero = heightMode == MeasureSpec.EXACTLY && heightSize == 0;

        // 只在“宽或高为0”时启用ratio
        if (widthIsZero && !heightIsZero) {
            int measuredHeight = resolveSize(heightSize, heightMeasureSpec);
            int measuredWidth = (int) (measuredHeight * aspectRatio);
            setMeasuredDimension(measuredWidth, measuredHeight);
            return;
        }

        if (heightIsZero && !widthIsZero) {
            int measuredWidth = resolveSize(widthSize, widthMeasureSpec);
            int measuredHeight = (int) (measuredWidth / aspectRatio);
            setMeasuredDimension(measuredWidth, measuredHeight);
            return;
        }

        // 其他情况交给父类
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // ---------------- Drawable ----------------

    private void applyDrawableState() {
        if (isOval) {
            backgroundDrawable.setShape(GradientDrawable.OVAL);
        } else {
            backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
            backgroundDrawable.setCornerRadius(cornerRadius);
        }

        backgroundDrawable.setColor(solidColor);
        backgroundDrawable.setStroke((int) borderWidth, borderColor);

        if (borderWidth > 0) {
            int padding = Math.max(0, (int) borderWidth);
            setPadding(padding, padding, padding, padding);
        } else {
            setPadding(0, 0, 0, 0);
        }
    }

    // ---------------- Public API ----------------

    /**
     * 设置圆角半径（单位 px）
     */
    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        isOval = false;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置是否为圆形背景
     */
    public void setOval(boolean oval) {
        isOval = oval;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置宽高比（宽 / 高）
     */
    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    /**
     * 设置边框宽度（单位 px）
     */
    public void setBorderWidth(float width) {
        borderWidth = width;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置边框颜色
     */
    public void setBorderColor(int color) {
        borderColor = color;
        applyDrawableState();
        invalidate();
    }
}
