package com.wcl.test.test;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.base.BaseRecyclerViewHolder;
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
    private MyAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_pull_down_refresh_layout);

        getTitleHelper().setTitle("测试");
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mPullToRefreshView = findViewById(R.id.swipe_refresh);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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


        mAdapter = new MyAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private boolean setData(int count, boolean isRefresh) {
        if (isRefresh) {
            mAdapter.getData().clear();
            List<DataItem> list = new ArrayList<>();
            list.add(createBannerItem());
            list.addAll(getDatas(count));
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(createDatas(count));
        }
        return mAdapter.getItemCount() <= 20;
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

    private static class MyAdapter extends BaseRecyclerViewAdapter<DataItem> {
        private final Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            return getData().get(position).viewType;
        }

        @NonNull
        @Override
        public BaseRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_BANNER) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.banner_layout, parent, false);
                return new BannerHolder(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_2, parent, false);
                return new ListHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder listHolder) {
                listHolder.onBind(position);
            } else if (holder instanceof BannerHolder bannerHolder) {
                Banner banner = (Banner) bannerHolder.itemView;
                banner.setAdapter(new BannerImageLoader(getData().get(position).bannerImgUrl));
                banner.setBannerGalleryEffect(30, 10); //画廊效果
                banner.start();
                bannerHolder.onBind(position);
            }
        }

        class ListHolder extends BaseRecyclerViewHolder {
            TextView title;
            ImageView webImageView;

            public ListHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                webImageView = itemView.findViewById(R.id.image_view);
            }

            @Override
            public void onBind(final int position) {
                title.setText("标题 - " + position);
                SmartImageLoader.load(webImageView, getData().get(position).imgUrl, //
                        AppBaseUtils.dip2px(100f), AppBaseUtils.dip2px(100f), AppBaseUtils.dip2px(8f));
            }
        }

        class BannerHolder extends BaseRecyclerViewHolder {

            public BannerHolder(View itemView) {
                super(itemView);
            }

            @Override
            public void onBind(final int position) {
            }
        }


    }

    private static class DataItem {
        public int viewType;
        public String imgUrl;
        public List<String> bannerImgUrl;
    }
}
