package com.wcl.test.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.wcl.test.utils.AppBaseUtils;

public class CornerRelativeLayout extends RelativeLayout {
    private float topLeftRadius;
    private float topRightRadius;
    private float bottomLeftRadius;
    private float bottomRightRadius;
    private Path clipPath;

    public CornerRelativeLayout(Context context) {
        super(context);
        init();
    }

    public CornerRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CornerRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        clipPath = new Path();
    }

    public void setCorner(int radii) {
        setCorner(radii, radii, radii, radii);
    }

    public void setCorner(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        this.topLeftRadius = AppBaseUtils.dip2px(topLeft);
        this.topRightRadius = AppBaseUtils.dip2px(topRight);
        this.bottomLeftRadius = AppBaseUtils.dip2px(bottomLeft);
        this.bottomRightRadius = AppBaseUtils.dip2px(bottomRight);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clipPath.reset();
        RectF rect = new RectF(0, 0, w, h);
        float[] radii = {topLeftRadius, topLeftRadius, topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius};
        clipPath.addRoundRect(rect, radii, Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(clipPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}