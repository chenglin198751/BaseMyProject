package view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import base.MyApp;
import cheerly.mybaseproject.R;

public class CenterDrawable extends Drawable {
    private Drawable mDrawable;
    private Paint mPaint = new Paint();
    private RectF mRectF;
    private float mRadius = 0;

    private CenterDrawable() {
    }

    public CenterDrawable(@DrawableRes int resId) {
        this(resId, 0);
    }

    public CenterDrawable(@DrawableRes int resId, float radius) {
        super();
        if (radius > 0) {
            mRadius = radius;
        } else if (radius < 0) {
            mRadius = 1000;
        }
        mDrawable = MyApp.getApp().getResources().getDrawable(resId);
        mPaint.setColor(MyApp.getApp().getResources().getColor(R.color.image_bg));
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable == null) {
            return;
        }

        int l = (getBounds().width() - mDrawable.getIntrinsicWidth()) / 2;
        int t = (getBounds().height() - mDrawable.getIntrinsicHeight()) / 2;
        mDrawable.setBounds(l, t, l + mDrawable.getIntrinsicWidth(), t + mDrawable.getIntrinsicHeight());

        if (mRectF == null) {
            mRectF = new RectF(0, 0, getBounds().width(), getBounds().height());
        }

        canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
        mDrawable.draw(canvas);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void setAlpha(int arg0) {
    }

    @Override
    public void setColorFilter(ColorFilter arg0) {
    }


}
