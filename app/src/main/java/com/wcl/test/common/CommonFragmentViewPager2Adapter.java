package com.wcl.test.common;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wcl.test.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 ViewPager2 的 FragmentStateAdapter 实现的通用适配器
 */
public class CommonFragmentViewPager2Adapter extends FragmentStateAdapter {
    private final List<BaseFragment> mFragments = new ArrayList<>();

    public CommonFragmentViewPager2Adapter(@NonNull FragmentActivity fragmentActivity, List<BaseFragment> fragments) {
        super(fragmentActivity);
        this.mFragments.clear();
        this.mFragments.addAll(fragments);
    }

    public CommonFragmentViewPager2Adapter(@NonNull Fragment fragment, List<BaseFragment> fragments) {
        super(fragment);
        this.mFragments.clear();
        this.mFragments.addAll(fragments);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    // 如果你需要更新数据，建议配合 DiffUtil 使用 submitList 或手动刷新
    public void updateFragments(List<BaseFragment> newFragments) {
        if (newFragments == null || newFragments.isEmpty()) {
            mFragments.clear();
            notifyDataSetChanged();
            return;
        }

        this.mFragments.clear();
        this.mFragments.addAll(newFragments);
        notifyDataSetChanged();
    }
}
