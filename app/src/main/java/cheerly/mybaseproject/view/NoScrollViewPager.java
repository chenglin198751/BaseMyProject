package cheerly.mybaseproject.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by chenglin on 2017-5-24.
 */

public class NoScrollViewPager extends ViewPager {
    private boolean isScroll = false;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isScroll;
    }

    public void setScrollEnable(boolean isScrollEnable) {
        isScroll = isScrollEnable;
    }
}