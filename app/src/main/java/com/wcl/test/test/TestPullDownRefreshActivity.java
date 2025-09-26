package com.wcl.test.test;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseListViewAdapter;
import com.wcl.test.helper.BannerImageLoader;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.view.pullrefresh.PullToRefreshView;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class TestPullDownRefreshActivity extends BaseActivity {
    private static final int VIEW_TYPE_BANNER = 0;
    private static final int VIEW_TYPE_LIST = 1;

    private PullToRefreshView mPullToRefreshView;
    private ListView mListView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_pull_down_refresh_layout);

        getTitleHelper().setTitle("测试");

        mListView = findViewById(R.id.list_view);
        mPullToRefreshView = findViewById(R.id.swipe_refresh);

        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);

        mPullToRefreshView.autoRefresh();
        mPullToRefreshView.setListener(new PullToRefreshView.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(() -> {
                    mAdapter.clear();
                    setData(10, true);
                    mPullToRefreshView.finishRefresh();
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(() -> {
                    boolean hasData = setData(5, false);
                    if (hasData) {
                        mPullToRefreshView.finishLoadMore();
                    } else {
                        mPullToRefreshView.finishLoadMoreWithNoMoreData();
                    }
                }, 1500);
            }
        });
    }

    private boolean setData(int count, boolean isRefresh) {
        if (isRefresh) {
            List<DataItem> list = new ArrayList<>();
            list.add(createBannerItem());
            list.addAll(getDatas(count));
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(createDatas(count));
        }
        return mAdapter.getCount() <= 20;
    }

    @NonNull
    private List<DataItem> getDatas(int count) {
        return createDatas(count);
    }

    private DataItem createBannerItem() {
        List<String> bannerImgs = new ArrayList<>();
        bannerImgs.add("https://qd.shouji.qihucdn.com/media/7596e61dd2bc80488dbca79665ec1252/660127d7974f7.png");
        bannerImgs.add("https://d02.qd.shouji.360tpcdn.com/media/3768e5340f2139e71661b805718e4cce/67d3e3a7d7717.png");
        bannerImgs.add("https://qd.shouji.qihucdn.com/media/80d15cfc4174f0bb48e9231400160487/6602aa5c7dfde.png");
        bannerImgs.add("https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png");
        bannerImgs.add("https://qd.shouji.qihucdn.com/media/3471cdbe7ce5812df964fbd68226edc0/6602aa4ad6b7f.png");

        DataItem item = new DataItem();
        item.viewType = VIEW_TYPE_BANNER;
        item.bannerImgUrl = bannerImgs;
        return item;
    }

    private List<DataItem> createDatas(int count) {
        List<DataItem> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DataItem item = new DataItem();
            item.viewType = VIEW_TYPE_LIST;
            item.imgUrl = "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png";
            list.add(item);
        }
        return list;
    }

    private static class MyAdapter extends BaseListViewAdapter<DataItem, BaseListViewAdapter.ViewHolder> {
        private final Context mContext;

        public MyAdapter(Context context) {
            super(context, 0); // 我们会根据 viewType 动态 inflate
            this.mContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            return getData().get(position).viewType;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @NonNull
        @Override
        protected ViewHolder createViewHolder(@NonNull View itemView) {
            // 不会走这里，改成用 getView 里的多布局逻辑
            throw new UnsupportedOperationException("Use getView instead for multi-type.");
        }

        @Override
        protected void bindViewHolder(@NonNull ViewHolder holder, @NonNull DataItem item, int position) {

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int viewType = getItemViewType(position);
            if (viewType == VIEW_TYPE_BANNER) {
                BannerHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.banner_layout, null);
                    holder = new BannerHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (BannerHolder) convertView.getTag();
                }
                holder.bind(getData().get(position));
            } else {
                ListHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(mContext, R.layout.test_item_2, null);
                    holder = new ListHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ListHolder) convertView.getTag();
                }
                holder.bind(getData().get(position), position);
            }
            return convertView;
        }

        // 普通Item
        static class ListHolder extends ViewHolder {
            TextView title;
            ImageView webImageView;

            public ListHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void initViews(@NonNull View itemView) {
                title = itemView.findViewById(R.id.title);
                webImageView = itemView.findViewById(R.id.image_view);
            }

            public void bind(DataItem item, int position) {
                title.setText("标题 - " + position);
                SmartImageLoader.load(webImageView, item.imgUrl,
                        AppBaseUtils.dip2px(100f), AppBaseUtils.dip2px(100f), AppBaseUtils.dip2px(8f));
            }
        }

        // BannerItem
        static class BannerHolder extends ViewHolder {
            Banner banner;

            public BannerHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void initViews(@NonNull View itemView) {
                banner = (Banner) itemView;
            }

            public void bind(DataItem item) {
                banner.setAdapter(new BannerImageLoader(item.bannerImgUrl));
                banner.setBannerGalleryEffect(30, 10);
                banner.start();
            }
        }
    }

    private static class DataItem {
        public int viewType;
        public String imgUrl;
        public List<String> bannerImgUrl;
    }
}
