package com.wcl.test.common;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenglin on 2017-9-14.
 */

public class CommonFragmentViewPagerAdapter extends FragmentPagerAdapter {
    public List<Fragment> mFragments = new ArrayList<>();
    public List<String> mTitleList = new ArrayList<>();

    public CommonFragmentViewPagerAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mFragments = list;
    }


    @Override
    public Fragment getItem(int position) {
        return this.mFragments.get(position);
    }

    @Override
    public int getCount() {
        return this.mFragments.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemPosition(Object object) {
        // 最简单解决 notifyDataSetChanged() 页面不刷新问题的方法
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitleList.size() == mFragments.size()){
            return mTitleList.get(position);
        }else {
            return super.getPageTitle(position);
        }
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
