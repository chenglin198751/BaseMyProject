package cheerly.mybaseproject.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import cheerly.mybaseproject.utils.BaseUtils;

/**
 * Created by chenglin on 2017-6-21.
 */

public class CornerLinearLayout extends LinearLayout {
    public CornerLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CornerLinearLayout(Context context) {
        super(context);
        init();
    }

    private final RectF roundRect = new RectF();
    private int corner = 4;
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    private void init() {
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        zonePaint.setAntiAlias(true);
        zonePaint.setColor(Color.WHITE);

        if (getTag() != null && getTag() instanceof String) {
            String tag = (String) getTag();
            if (TextUtils.isDigitsOnly(tag.trim())) {
                corner = Integer.parseInt(tag.trim());
            }
        }
        corner = BaseUtils.dip2px(corner);
    }

    /**
     * 设置View 的显示角度
     *
     * @param corner 单位是像素
     */
    public void setCorner(int corner) {
        this.corner = corner;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        roundRect.set(0, 0, getWidth(), getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(roundRect, corner, corner, zonePaint);
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        canvas.restore();
    }
}
