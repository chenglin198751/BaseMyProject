package test;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.youth.banner.Banner;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import base.BaseListViewAdapter;
import cheerly.mybaseproject.R;
import helper.BannerImageLoader;
import pullrefresh.PullToRefresh;
import utils.Constants;
import utils.BaseUtils;
import view.WebImageView;

public class TestPullDownRefreshActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private ListView mListView;
    private MyAdapter mAdapter;

    private List<String> imagesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.pull_down_refresh_layout);

        imagesList.add("http://img.zcool.cn/community/0166c756e1427432f875520f7cc838.jpg");
        imagesList.add("http://img.zcool.cn/community/018fdb56e1428632f875520f7b67cb.jpg");
        imagesList.add("http://img.zcool.cn/community/01c8dc56e1428e6ac72531cbaa5f2c.jpg");
        imagesList.add("http://img.zcool.cn/community/01fd2756e142716ac72531cbf8bbbf.jpg");
        imagesList.add("http://pic31.nipic.com/20130727/6949918_163332595163_2.jpg");

        getTitleHelper().setTitle("测试");
        mListView = (ListView) findViewById(R.id.list_view);
        Banner banner = (Banner) View.inflate(this, R.layout.banner_layout, null);
        banner.setImageLoader(new BannerImageLoader());
        banner.setImages(imagesList);
        banner.setBannerAnimation(Transformer.ZoomOutSlide);
        banner.start();

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(-1, Constants.getScreenWidth() / 2);
        banner.setLayoutParams(params);
        mListView.addHeaderView(banner);


        mPullToRefresh = (PullToRefresh) findViewById(R.id.swipe_refresh);

        mPullToRefresh.setListener(new PullToRefresh.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        setData(10, true);
                        mPullToRefresh.finishRefresh();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(5, false);
                        mPullToRefresh.finishLoadmore();
                    }
                }, 1500);
            }
        });


        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        setData(10, true);
    }

    private void setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter.getCount() + i));
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }

    }

    private static class MyAdapter extends BaseListViewAdapter<String> {
        private List<String> imagesList = new ArrayList<>();
        private Context mContext;

        public MyAdapter(Context context) {
            super(context);
            mContext = context;


            imagesList.add("http://img.zcool.cn/community/0166c756e1427432f875520f7cc838.jpg");
            imagesList.add("http://img.zcool.cn/community/018fdb56e1428632f875520f7b67cb.jpg");
            imagesList.add("http://img.zcool.cn/community/01c8dc56e1428e6ac72531cbaa5f2c.jpg");
            imagesList.add("http://img.zcool.cn/community/01fd2756e142716ac72531cbf8bbbf.jpg");
            imagesList.add("http://pic31.nipic.com/20130727/6949918_163332595163_2.jpg");

            imagesList.addAll(imagesList);
            imagesList.addAll(imagesList);
            imagesList.addAll(imagesList);
            imagesList.addAll(imagesList);
            imagesList.addAll(imagesList);
            imagesList.addAll(imagesList);
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.test_item_2, null);
                convertView.setClickable(true);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            WebImageView webImageView = (WebImageView) convertView.findViewById(R.id.image_view);
            title.setText("标题 - " + position);
            webImageView.load(imagesList.get(position), BaseUtils.dip2px(100f), BaseUtils.dip2px(100f));

            return convertView;
        }
    }


}
