package cheerly.mybaseproject.test;

import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.common.CommonFragmentViewPagerAdapter;

public class TestSnapNestViewPagerActivity extends BaseActivity {
    private ViewPager mViewPager;
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
        mAdapter = new CommonFragmentViewPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTips.setText("第" + (position + 1) + "个tab");
            }
        });

    }
}
