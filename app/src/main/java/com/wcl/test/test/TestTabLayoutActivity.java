package com.wcl.test.test;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.R;
import com.wcl.test.common.CommonFragmentViewPagerAdapter;
import com.wcl.test.widget.MyTabLayout;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestTabLayoutActivity extends BaseActivity {
    private ViewPager2 mViewPager;
    private CommonFragmentViewPagerAdapter mFragmentAdapter;
    private List<Fragment> mFragList = new ArrayList<>();
    private MyTabLayout mTabLayout;
    private TabLayoutMediator mediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);

        for (int i = 0; i < 4; i++) {
            TestTabLayoutFragment fragment = new TestTabLayoutFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index", i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }
        mFragmentAdapter = new CommonFragmentViewPagerAdapter(this, mFragList);
        mViewPager.setAdapter(mFragmentAdapter);

        mediator = new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            TextView tabView = new TextView(getContext());
            tabView.setText("test" + position);
            tabView.setTextSize(15);
            tabView.setGravity(Gravity.CENTER);
            tabView.setTextColor(Color.BLUE);
            tab.setCustomView(tabView);
        });
        //要执行这一句才是真正将两者绑定起来
        mediator.attach();
    }
}
