package com.wcl.test.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.common.CommonFragmentViewPagerAdapter;

import java.util.ArrayList;

public class TestSnapNestViewPagerActivity extends BaseActivity {
    private ViewPager2 mViewPager;
    private CommonFragmentViewPagerAdapter mAdapter;
    private TextView mTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.test_snap_nest_viewpager_layout);
        mTips = findViewById(R.id.tab_collect);
        mTips.setText("第1个tab");

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new TestSnapNestFragment());
        fragments.add(new TestSnapNestFragment());
        fragments.add(new TestSnapNestFragment());
        fragments.add(new TestSnapNestFragment());

        mViewPager = findViewById(R.id.view_pager);
        mAdapter = new CommonFragmentViewPagerAdapter(this, fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                //offset 滑动位移
            }
        });


        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // 页面滚动时调用，position 是当前页面位置，positionOffset 是滚动偏移量 (0-1)
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 新页面被选中时调用
                mTips.setText("第" + (position + 1) + "个tab");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // 页面滚动状态变化时调用
                // state 取值：SCROLL_STATE_IDLE (0)、SCROLL_STATE_DRAGGING (1)、SCROLL_STATE_SETTLING (2)
            }
        });
    }
}
