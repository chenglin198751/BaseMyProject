package cheerly.mybaseproject.test;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseActivity;

public class TestSnapNestViewPagerActivity extends BaseActivity {
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.test_snap_nest_viewpager_layout);
        mViewPager = findViewById(R.id.view_pager);
        mAdapter = new ViewPagerAdapter(getContext());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(10);

    }


    private static class ViewPagerAdapter extends PagerAdapter {
        private Context mContext;

        private ViewPagerAdapter() {
        }

        public ViewPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            RecyclerView recyclerView = new RecyclerView(mContext);
            TestRecyclerAdapter mAdapter2 = new TestRecyclerAdapter(mContext);

            List<String> list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                list.add("" + (mAdapter2.getItemCount() + i));
            }
            mAdapter2.setDataList(list);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter2);

            container.addView(recyclerView, new ViewGroup.LayoutParams(-1, -1));
            return recyclerView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


    }
}
