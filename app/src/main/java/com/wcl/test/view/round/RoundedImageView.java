package com.wcl.test.view.round;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

import com.wcl.test.R;

public class RoundedImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float mCornerRadius; // px
    private boolean mOval;
    private float mAspectRatio;

    public RoundedImageView(Context context) {
        super(context);
        init(null);
    }

    public RoundedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
            mCornerRadius = a.getDimension(R.styleable.RoundedImageView_riv_corner_radius, 0f);
            mOval = a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);
            mAspectRatio = a.getFloat(R.styleable.RoundedImageView_riv_aspect_ratio, 0f);
            a.recycle();
        }

        // 默认 CENTER_CROP 保证图片填充 View
        setScaleType(ScaleType.CENTER_CROP);

        // 设置圆角/圆形裁剪（API 21+）
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (mOval) {
                    int size = Math.min(view.getWidth(), view.getHeight());
                    outline.setOval(0, 0, size, size);
                } else {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mCornerRadius);
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width / mAspectRatio);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    // ====== 公共 API ======

    /**
     * 设置圆角半径，单位 dp
     */
    public void setCornerRadius(float dp) {
        float density = getResources().getDisplayMetrics().density;
        mCornerRadius = dp * density;
        updateOutline();
    }

    /**
     * 设置是否圆形
     */
    public void setOval(boolean oval) {
        mOval = oval;
        updateOutline();
    }

    /**
     * 设置宽高比（width/height）
     */
    public void setAspectRatio(float ratio) {
        mAspectRatio = ratio;
        requestLayout();
    }

    private void updateOutline() {
        invalidateOutline();
    }
}
