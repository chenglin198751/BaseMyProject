package view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by chenglin on 2017-09-26.
 * <p>
 * 可以显示维持图片bitmap宽高的ImageView
 */
public class KeepScaleImageView extends WebImageView {
    private int mWidth = 0;

    public KeepScaleImageView(Context context) {
        super(context);
    }

    public KeepScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeepScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWidth(int width) {
        mWidth = width;
    }


    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            if (mWidth > 0) {
                int height = mWidth * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                getLayoutParams().width = mWidth;
                getLayoutParams().height = height;
            }
        }
        super.setImageDrawable(drawable);
    }
}
