package com.wcl.test.test;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import com.wcl.test.base.BaseActivity;
import com.wcl.test.R;
import com.wcl.test.common.CommonFragmentViewPagerAdapter;
import com.wcl.test.widget.MyTabLayout;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestTabLayoutActivity extends BaseActivity {
    private ViewPager mViewPager;
    private CommonFragmentViewPagerAdapter mFragmentAdapter;
    private List<Fragment> mFragList = new ArrayList<>();
    private MyTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (MyTabLayout) findViewById(R.id.tab_layout);

        for (int i = 0; i < 4; i++) {
            TestTabLayoutFragment fragment = new TestTabLayoutFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index",i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }
        mFragmentAdapter = new CommonFragmentViewPagerAdapter(getSupportFragmentManager(),mFragList);
        for (int i = 0; i < 4; i++) {
            mFragmentAdapter.mTitleList.add("标题");
        }
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.initCustomTabItem(mViewPager);
    }
}
