package com.wcl.test.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

/**
 * 圆角/圆形图片视图，支持背景色、边框和宽高比（View是被裁剪的纯圆角）
 */
class RoundedBgImageView2 extends AppCompatImageView implements IRoundedMethod {

    protected float cornerRadius = 0f;
    protected boolean isOval = false;
    protected float aspectRatio = 0f;
    protected float borderWidth = 0f;
    protected int borderColor = Color.TRANSPARENT;
    protected int solidColor = Color.TRANSPARENT;
    private GradientDrawable backgroundDrawable;

    public RoundedBgImageView2(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedBgImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedBgImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
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

        setClipToOutline(true);
        setOutlineProvider(new RoundedOutlineProvider());

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

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

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

        invalidateOutline();
    }

    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
    }

    public void setImageBitmap(@Nullable android.graphics.Bitmap bm) {
        super.setImageBitmap(bm);
    }

    public void setScaleType(@NonNull AppCompatImageView.ScaleType scaleType) {
        super.setScaleType(scaleType);
    }

    @Override
    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        isOval = false;
        applyDrawableState();
        invalidate();
    }

    @Override
    public void setOval(boolean oval) {
        isOval = oval;
        applyDrawableState();
        invalidate();
    }

    @Override
    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    @Override
    public void setBorderWidth(float width) {
        borderWidth = width;
        applyDrawableState();
        invalidate();
    }

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

    private class RoundedOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            if (isOval) {
                outline.setOval(0, 0, width, height);
            } else {
                outline.setRoundRect(0, 0, width, height, cornerRadius);
            }
        }
    }
}
