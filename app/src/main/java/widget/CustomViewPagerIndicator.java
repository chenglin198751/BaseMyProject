package widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import cheerly.mybaseproject.R;
import utils.BaseUtils;

/**
 * Created by chenglin on 2017-2-21.
 * 用于viewPager下方的导航点点
 */

public class CustomViewPagerIndicator extends LinearLayout {
    private int mDotCount;
    private ViewPager mViewPager;
    private int mDotSelector = R.drawable.viewpage_dot_selector;
    private int mSize = BaseUtils.dip2px(8f);
    private int mMargin = BaseUtils.dip2px(5f);

    public CustomViewPagerIndicator(Context context) {
        super(context);
    }

    public CustomViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomViewPagerIndicator setCount(int count) {
        mDotCount = count;
        create();
        return this;
    }

    public CustomViewPagerIndicator setSize(int size) {
        mSize = size;
        return this;
    }

    public CustomViewPagerIndicator setMargin(int margin) {
        mMargin = margin / 2;
        return this;
    }

    public int getCount() {
        return mDotCount;
    }

    private void create() {
        this.setVisibility(View.VISIBLE);
        this.removeAllViews();
        this.setOrientation(LinearLayout.HORIZONTAL);

        if (mDotCount > 1) {
            for (int i = 0; i < mDotCount; i++) {
                addDot(i);
            }
            setSelected(0);
        }
    }

    public CustomViewPagerIndicator setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        return this;
    }

    public CustomViewPagerIndicator setSelector(int selector) {
        mDotSelector = selector;
        create();
        return this;
    }

    private void addDot(final int index) {
        View view = new View(getContext());
        view.setBackgroundResource(mDotSelector);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mSize, mSize);
        params.leftMargin = mMargin;
        params.rightMargin = mMargin;
        this.addView(view, params);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(index);
                }
            }
        });
    }

    public void clear() {
        this.removeAllViews();
        this.setVisibility(View.GONE);
    }

    public CustomViewPagerIndicator setSelected(int index) {
        if (index < 0) {
            return this;
        }

        for (int i = 0; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            if (i == index) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
        return this;
    }
}
