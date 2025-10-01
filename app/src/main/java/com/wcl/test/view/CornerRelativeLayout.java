package com.wcl.test.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.wcl.test.R;

/**
 * 带圆角的 LinearLayout
 * 支持 XML 属性和代码设置圆角
 */
public class CornerRelativeLayout extends LinearLayout {

    private final RectF roundRect = new RectF();
    private final Path clipPath = new Path();
    private float cornerRadiusPx = dpToPx(8);

    public CornerRelativeLayout(Context context) {
        super(context);
        init(context, null);
    }

    public CornerRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CornerRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CornerViewGroup);
            cornerRadiusPx = a.getDimension(R.styleable.CornerViewGroup_corner, cornerRadiusPx);
            a.recycle();
        }
    }

    /**
     * 代码设置圆角
     *
     * @param dp 圆角，单位 dp
     */
    public void setCorner(float dp) {
        this.cornerRadiusPx = dpToPx(dp);
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        roundRect.set(0, 0, getWidth(), getHeight());
        clipPath.reset();
        clipPath.addRoundRect(roundRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(clipPath);
        super.draw(canvas);
        canvas.restore();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
