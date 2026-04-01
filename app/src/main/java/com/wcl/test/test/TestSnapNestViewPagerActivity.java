package com.wcl.test.test;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.common.CommonFragmentViewPager2Adapter;
import com.wcl.test.common.MagicIndicatorViewPager2Binder;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import java.util.ArrayList;


public class TestSnapNestViewPagerActivity extends BaseActivity {
    private ViewPager2 mViewPager2;
    private CommonFragmentViewPager2Adapter mAdapter;
    private final ArrayList<String> mTitleDataList = new ArrayList<>();
    private final ArrayList<BaseFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.test_snap_nest_viewpager_layout);

        mTitleDataList.add("tab0");
        mTitleDataList.add("tab1");
        mTitleDataList.add("tab2");
        mTitleDataList.add("tab3");

        for (int position = 0; position < mTitleDataList.size(); position++) {
            BaseFragment fragment = new TestSnapNestFragment();
            Bundle arg = new Bundle();
            arg.putInt("position", position);
            fragment.setArguments(arg);
            fragments.add(fragment);
        }

        mViewPager2 = findViewById(R.id.view_pager2);
        mViewPager2.setOffscreenPageLimit(mTitleDataList.size());
        mAdapter = new CommonFragmentViewPager2Adapter(this, fragments);
        mViewPager2.setAdapter(mAdapter);

        initMagicIndicator();
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, offset) -> {
            //offset 滑动位移
        });
    }

    private void initMagicIndicator() {
        MagicIndicator magicIndicator = findViewById(R.id.magic_indicator);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return mTitleDataList == null ? 0 : mTitleDataList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                // 可以自定义指示器View
                ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
                colorTransitionPagerTitleView.setNormalColor(Color.GRAY);
                colorTransitionPagerTitleView.setSelectedColor(Color.BLACK);
                colorTransitionPagerTitleView.setText(mTitleDataList.get(index));
                colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mViewPager2.setCurrentItem(index);
                    }
                });
                return colorTransitionPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        MagicIndicatorViewPager2Binder.bind(magicIndicator, mViewPager2);
    }
}
