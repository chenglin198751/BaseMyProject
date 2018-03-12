package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by chenglin on 2017-10-27.
 */

public class AutoSizeImageView extends WebImageView {
    public AutoSizeImageView(Context context) {
        super(context);
    }

    public AutoSizeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSizeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final RectF roundRect = new RectF();
    private float mCorner = 0f;
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();
    private Paint paint;
    private int wantWidth = 0;
    private int wantHeight = 0;
    public int bitmapWidth, bitmapHeight;


    public void setCorner(float corner) {
        if (corner <= 0f) {
            return;
        }
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        zonePaint.setAntiAlias(true);
        zonePaint.setColor(Color.BLACK);

        paint = new Paint();
        mCorner = dip2px(corner);
        invalidate();
    }


    public void setWidth(int width) {
        wantWidth = width;
    }

    public void setHeight(int height) {
        wantHeight = height;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null && getLayoutParams() != null) {
            bitmapWidth = drawable.getIntrinsicWidth();
            bitmapHeight = drawable.getIntrinsicHeight();

            ViewGroup.LayoutParams params = getLayoutParams();
            if (wantWidth > 0) {
                params.width = wantWidth;
                params.height = wantWidth * bitmapHeight / bitmapWidth;
            } else if (wantHeight > 0) {
                params.width = wantHeight * bitmapWidth / bitmapHeight;
                params.height = wantHeight;
            }
            setLayoutParams(params);
        }
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getWidth();
        int h = getHeight();
        roundRect.set(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCorner > 0) {
            canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
            canvas.drawRoundRect(roundRect, mCorner, mCorner, zonePaint);
            canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        }
        super.draw(canvas);
        if (mCorner > 0) {
            canvas.restore();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCorner > 0) {
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            canvas.drawColor(Color.alpha(0));
            paint.setStrokeWidth(1f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(roundRect, mCorner, mCorner, paint);
        }
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
