package com.wcl.test.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenglin on 2021-07-23.
 * 使用ViewPager2来加载fragment
 * 优点：
 * 1、可以notifyChanged，增加一个，删除一个，改变一个等，并且传入的参数也会跟着变
 * 2、可以实现懒加载，因为默认只加载当前页
 */

public class TestViewPager2Activity extends BaseActivity {
    private ViewPager2 mViewPager;
    private TestAdapter mFragmentAdapter;
    //    private List<Class> mFragList = Arrays.asList(TestTabLayoutFragment.class, TestTabLayoutFragment.class, TestTabLayoutFragment.class, TestTabLayoutFragment.class);
    private final List<Fragment> mFragList = new ArrayList<>();
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mFragList.add(new TestTabLayoutFragment());
//        mFragList.add(new TestTabLayoutFragment());
//        mFragList.add(new TestTabLayoutFragment());
//        mFragList.add(new TestTabLayoutFragment());

        for (int i = 0; i < 4; i++) {
            TestTabLayoutFragment fragment = new TestTabLayoutFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index", i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }

        setContentLayout(R.layout.test_viewpager2_layout);
        mViewPager = (ViewPager2) findViewById(R.id.view_pager);
        mFragmentAdapter = new TestAdapter(this);
        mViewPager.setAdapter(mFragmentAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFragList.clear();
                for (int i = 0; i < 4; i++) {
                    TestTabLayoutFragment fragment = new TestTabLayoutFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("index", i + "ddd");
                    fragment.setArguments(bundle);
                    mFragList.add(fragment);
                }

                mFragmentAdapter = new TestAdapter(getContext());
                mViewPager.setAdapter(mFragmentAdapter);
                mFragmentAdapter.notifyDataSetChanged();
                Log.v("tag_99", "size2 = " + getSupportFragmentManager().getFragments().size());
            }
        });

        Log.v("tag_99", "size1 = " + getSupportFragmentManager().getFragments().size());
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
