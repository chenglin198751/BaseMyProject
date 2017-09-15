package widget;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cheerly.mybaseproject.R;

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

    public void initCustomTabItem(ViewPager viewPager) {
        mViewPager = viewPager;
        for (int index = 0; index < getTabCount(); index++) {
            TabLayout.Tab tab = getTabAt(index);
            if (tab != null) {
                View itemView = View.inflate(getContext(), R.layout.my_custom_tab_item, null);
                tab.setCustomView(itemView);
                TextView textView = (TextView) itemView.findViewById(R.id.text);
                textView.setText(mViewPager.getAdapter().getPageTitle(index));
                ((ViewGroup)itemView.getParent()).setBackgroundResource(0);
            }
        }
    }
}
