package com.wcl.test.widget;

import android.content.Context;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wcl.test.R;

/**
 * Created by chenglin on 2017-9-15.
 */

public class MyTabLayout extends TabLayout {
    private ViewPager mViewPager;

    public MyTabLayout(Context context) {
        super(context);
    }

    public MyTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 手动初始化自定义的TabItem，如果你自定义你自己的TabItem，
     * 那么不需要调用我的这个方法，你只需要仿照我的这个方法再写一个方法实现即可
     */
    public void initCustomTabItem(ViewPager viewPager) {
        mViewPager = viewPager;
        for (int index = 0; index < getTabCount(); index++) {
            TabLayout.Tab tab = getTabAt(index);
            if (tab != null) {
                View itemView = View.inflate(getContext(), R.layout.my_custom_tab_item, null);
                tab.setCustomView(itemView);
                TextView textView = (TextView) itemView.findViewById(R.id.text);
                textView.setText(mViewPager.getAdapter().getPageTitle(index));
                ((ViewGroup) itemView.getParent()).setBackgroundResource(0);
            }
        }
    }

    /**
     * 返回自定义的TabItem
     */
    public View getCustomTabItem(final int currentIndex) {
        for (int index = 0; index < getTabCount(); index++) {
            TabLayout.Tab tab = getTabAt(index);
            if (tab != null && index == currentIndex) {
                return tab.getCustomView();
            }
        }
        return null;
    }
}
