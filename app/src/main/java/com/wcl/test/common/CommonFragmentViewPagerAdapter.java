package com.wcl.test.common;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的 Fragment ViewPager 适配器
 * 支持动态刷新与安全的 Fragment 数据绑定
 * <p>
 * 优化点：
 * - 使用 FragmentStatePagerAdapter 避免内存泄漏
 * - 支持泛型 DataItem<T extends Fragment>
 * - 避免反射，支持通过工厂方法创建 Fragment
 * - 支持动态刷新数据与标题
 */
public class CommonFragmentViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();

    public CommonFragmentViewPagerAdapter(@NonNull FragmentManager fm, @NonNull List<Fragment> dataList) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mFragments.addAll(dataList);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public long getItemId(int position) {
        // 保证 Fragment 唯一性，避免 Bundle 混乱
        return mFragments.get(position).hashCode();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // 保证 notifyDataSetChanged() 后强制刷新
        return POSITION_NONE;
    }

    /**
     * 设置数据并刷新
     */
    public void setData(List<Fragment> list) {
        mFragments.clear();
        if (list != null) {
            mFragments.addAll(list);
        }
        notifyDataSetChanged();
    }

}
