package com.wcl.test.test;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseListViewAdapter;
import com.wcl.test.helper.BannerImageLoader;
import com.wcl.test.view.image.GlideBorderImageView;
import com.wcl.test.view.pullrefresh.PullToRefreshView;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

public class TestPullDownRefreshActivity extends BaseActivity {
    private static final int VIEW_TYPE_BANNER = 0;
    private static final int VIEW_TYPE_LIST = 1;

    private PullToRefreshView mPullToRefreshView;
    private ListView mListView;
    private ParentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_pull_down_refresh_layout);
        getTitleHelper().setTitle("测试");

        mListView = findViewById(R.id.list_view);
        mPullToRefreshView = findViewById(R.id.swipe_refresh);

        mAdapter = new ParentAdapter(this);
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
                    boolean hasData = setData(10, false);
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
        return mAdapter.getCount() <= 100;
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

    // ---------------- Adapter ----------------
    private static class ParentAdapter extends BaseListViewAdapter<DataItem, BaseListViewAdapter.BaseListViewHolder<DataItem>> {
        private final Context mContext;

        public ParentAdapter(Context context) {
            super(context);
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
        protected BaseListViewHolder<DataItem> createViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_BANNER) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.banner_layout, parent, false);
                return new BannerHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_3, parent, false);
                return new ListHolder(view);
            }
        }

        @Override
        protected void bindViewHolder(@NonNull BaseListViewHolder<DataItem> holder, @NonNull DataItem item, int position) {
            holder.onBind(item, position);
        }

        // 普通Item
        static class ListHolder extends BaseListViewHolder<DataItem> {
            TextView title;
            GlideBorderImageView webImageView;
            ListView childListView;
            ChildAdapter childAdapter;

            public ListHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void bindViews(@NonNull View itemView) {
                title = itemView.findViewById(R.id.title);
                webImageView = itemView.findViewById(R.id.image_view);
                childListView = itemView.findViewById(R.id.child_list_view);
                childAdapter = new ChildAdapter(itemView.getContext());
                childListView.setAdapter(childAdapter);
            }

            @Override
            public void onBind(@NonNull DataItem item, int position) {
                title.setText("标题 - " + position);
                webImageView.loadImage(item.imgUrl);

                List<DataItem> list2 = new ArrayList<>();
                int num = (int) (Math.random() * 6);
                for (int i = 0; i < num; i++) {
                    DataItem item2 = new DataItem();
                    item2.viewType = VIEW_TYPE_LIST;
                    item2.imgUrl = "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png";
                    list2.add(item2);
                }
                childAdapter.setDataList(list2);
            }
        }

        // BannerItem
        static class BannerHolder extends BaseListViewHolder<DataItem> {
            Banner banner;

            public BannerHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void bindViews(@NonNull View itemView) {
                banner = (Banner) itemView;
            }

            @Override
            public void onBind(@NonNull DataItem item, int position) {
                banner.setAdapter(new BannerImageLoader(item.bannerImgUrl));
                banner.setBannerGalleryEffect(30, 10);
                banner.start();
            }
        }
    }

    private static class ChildAdapter extends BaseListViewAdapter<DataItem, BaseListViewAdapter.BaseListViewHolder<DataItem>> {
        private final Context mContext;

        public ChildAdapter(Context context) {
            super(context);
            this.mContext = context;
        }

        @NonNull
        @Override
        protected BaseListViewHolder<DataItem> createViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_3_child, parent, false);
                return new ListHolder(view);
        }

        @Override
        protected void bindViewHolder(@NonNull BaseListViewHolder<DataItem> holder, @NonNull DataItem item, int position) {
            holder.onBind(item, position);
        }

        // 普通Item
        static class ListHolder extends BaseListViewHolder<DataItem> {
            TextView child_title;

            public ListHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void bindViews(@NonNull View itemView) {
                child_title = itemView.findViewById(R.id.child_title);
            }

            @Override
            public void onBind(@NonNull DataItem item, int position) {
            }
        }

    }

    // ---------------- Data Model ----------------
    private static class DataItem {
        public int viewType;
        public String imgUrl;
        public List<String> bannerImgUrl;
    }
}
