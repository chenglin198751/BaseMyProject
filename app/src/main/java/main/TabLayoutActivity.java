package main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import common.MyFragmentViewPagerAdapter;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TabLayoutActivity extends BaseActivity {
    private ViewPager mViewPager;
    private MyFragmentViewPagerAdapter mFragmentAdapter;
    private List<Fragment> mFragList = new ArrayList<>();
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        for (int i = 0; i < 4; i++) {
            TestFragment fragment = new TestFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index",i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }
        mFragmentAdapter = new MyFragmentViewPagerAdapter(getSupportFragmentManager(),mFragList);
        for (int i = 0; i < 4; i++) {
            mFragmentAdapter.mTitleList.add("标题");
        }
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
