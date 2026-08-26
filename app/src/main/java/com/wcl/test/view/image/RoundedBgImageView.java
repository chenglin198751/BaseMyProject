package com.wcl.test.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

/**
 * 圆角/圆形图片视图，支持背景色、边框和宽高比（View并非是被裁剪的圆角，是被glide加载的圆角图片）
 */
class RoundedBgImageView extends AppCompatImageView implements IRoundedMethod {

    protected float cornerRadius = 0f;
    protected boolean isOval = false;
    protected float aspectRatio = 0f;
    protected float borderWidth = 0f;
    protected int borderColor = Color.TRANSPARENT;
    protected int solidColor = Color.TRANSPARENT;
    private GradientDrawable backgroundDrawable;

    RoundedBgImageView(Context context) {
        super(context);
        init(context, null);
    }

    RoundedBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    RoundedBgImageView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        // 双 EXACTLY：交给系统 / ConstraintLayout
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 高度确定 -> 推导宽度
        if (heightMode == MeasureSpec.EXACTLY) {
            int width = Math.round((float) heightSize * aspectRatio);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 宽度确定 -> 推导高度
        // 注意：match_parent 在 wrap_content 容器 / Dialog（默认窗口宽度 wrap_content）里会被降级为 AT_MOST，
        // 此时宽度虽为 AT_MOST 但会填满 widthSize；需要把宽度也钉成 EXACTLY，
        // 否则 ImageView 在 AT_MOST 下会收缩到图片原始宽度，导致比例失真
        boolean widthFixed = widthMode == MeasureSpec.EXACTLY
                || (widthMode == MeasureSpec.AT_MOST && getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT);
        if (widthFixed) {
            int height = Math.round((float) widthSize / aspectRatio);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 都不确定（wrap_content 等）：交给系统
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
    @Override
    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        isOval = false;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置是否为圆形背景
     */
    @Override
    public void setOval(boolean oval) {
        isOval = oval;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置宽高比（宽 / 高）
     */
    @Override
    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    /**
     * 设置边框宽度（单位 px）
     */
    @Override
    public void setBorderWidth(float width) {
        borderWidth = width;
        applyDrawableState();
        invalidate();
    }

    /**
     * 设置边框颜色
     */
    @Override
    public void setBorderColor(int color) {
        borderColor = color;
        applyDrawableState();
        invalidate();
    }

    @Override
    public void setSolidColor(int color) {
        solidColor = color;
        applyDrawableState();
        invalidate();
    }
}
