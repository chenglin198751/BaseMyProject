package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

public class CenterDrawable extends Drawable {
    private Context mContext;
    private Drawable mDrawable;

    public CenterDrawable(Context context, @DrawableRes int resId) {
        super();
        this.mContext = context;
        mDrawable = mContext.getResources().getDrawable(resId);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable == null || getBounds() == null) {
            return;
        }
        int l = (getBounds().width() - mDrawable.getIntrinsicWidth()) / 2;
        int t = (getBounds().height() - mDrawable.getIntrinsicHeight()) / 2;
        mDrawable.setBounds(l, t, l + mDrawable.getIntrinsicWidth(), t + mDrawable.getIntrinsicHeight());
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
