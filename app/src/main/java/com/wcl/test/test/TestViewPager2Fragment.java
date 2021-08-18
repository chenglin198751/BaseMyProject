package com.wcl.test.test;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestViewPager2Fragment extends BaseFragment {
    private final List<Fragment> mFragList = new ArrayList<>();
    private ViewPager2 mViewPager;
    private TestAdapter mFragmentAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < 4; i++) {
            TestViewPager2ChildFragment fragment = new TestViewPager2ChildFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index", i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }
    }


    @Override
    protected int getContentLayout() {
        return R.layout.test_viewpager2_fragment_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        TextView content = (TextView) getView().findViewById(R.id.content);
        content.setText(getArguments().getString("index"));

        mViewPager = (ViewPager2) view.findViewById(R.id.view_pager2);
        mFragmentAdapter = new TestAdapter(this);
        mViewPager.setAdapter(mFragmentAdapter);
    }

    private class TestAdapter extends FragmentStateAdapter {

        public TestAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public TestAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public TestAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return mFragList.get(position);
        }

        @Override
        public int getItemCount() {
            return mFragList.size();
        }
    }
}
