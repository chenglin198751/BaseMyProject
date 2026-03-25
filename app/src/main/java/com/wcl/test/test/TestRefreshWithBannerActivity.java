package com.wcl.test.test;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseListViewAdapter;
import com.wcl.test.helper.BannerImageLoader;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.view.image.GlideBgImageView;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

/**
 * 下拉刷新与上拉加载示例（包含多类型 Item：Banner + 列表 + 嵌套子列表）
 */
public class TestRefreshWithBannerActivity extends BaseActivity {
    private static final int VIEW_TYPE_BANNER = 0;
    private static final int VIEW_TYPE_LIST = 1;
    private static final int PAGE_SIZE = 10;
    private static final int MAX_DATA_COUNT = 100;
    private static final int BANNER_GALLERY_EFFECT_RADIUS = 30;
    private static final int BANNER_GALLERY_EFFECT_SPACE = 10;

    private SmartRefreshLayout refreshLayout;
    private ListView mListView;
    private ParentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_refresh_with_banner_layout);
        getTitleHelper().setTitle("测试");

        mListView = findViewById(R.id.list_view);
        refreshLayout = findViewById(R.id.swipe_refresh);

        mAdapter = new ParentAdapter(this);
        mListView.setAdapter(mAdapter);

        refreshLayout.autoRefresh();

        // 配置刷新和加载监听
        refreshLayout.setOnRefreshListener(createRefreshListener());
        refreshLayout.setOnLoadMoreListener(createLoadMoreListener());
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 停止所有 Banner 的自动轮播，防止内存泄漏
        mAdapter.stopAllBanners();
    }

    /**
     * 创建下拉刷新监听器
     */
    private OnRefreshListener createRefreshListener() {
        return new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout layout) {
                performRefresh(true, layout);
            }
        };
    }

    /**
     * 创建上拉加载监听器
     */
    private OnLoadMoreListener createLoadMoreListener() {
        return new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout layout) {
                performRefresh(false, layout);
            }
        };
    }

    /**
     * 执行刷新或加载操作
     *
     * @param isRefresh true 为下拉刷新，false 为上拉加载
     * @param layout    刷新布局控件
     */
    private void performRefresh(final boolean isRefresh, final RefreshLayout layout) {
        AppUtils.getUiHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean hasMoreData = setData(PAGE_SIZE, isRefresh);
                if (isRefresh) {
                    layout.finishRefresh();
                } else {
                    if (hasMoreData) {
                        layout.finishLoadMore();
                    } else {
                        layout.finishLoadMoreWithNoMoreData();
                    }
                }
            }
        }, 500);
    }

    /**
     * 设置数据列表
     *
     * @param count     每页数据条数
     * @param isRefresh true 为刷新（替换数据），false 为加载更多（追加数据）
     * @return 是否还有更多数据（用于判断是否显示"没有更多数据"）
     */
    private boolean setData(int count, boolean isRefresh) {
        if (isRefresh) {
            List<DataItem> list = new ArrayList<>();
            list.add(createBannerDataItem());
            list.addAll(createListDataItems(count));
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(createListDataItems(count));
        }
        // 当总数小于最大值时，表示还有更多数据可加载
        return mAdapter.getCount() < MAX_DATA_COUNT;
    }

    /**
     * 创建 Banner 类型的 DataItem
     */
    private DataItem createBannerDataItem() {
        List<String> bannerImgs = new ArrayList<>(TestUrls.ImgUrls.subList(0, 5));

        DataItem item = new DataItem();
        item.viewType = VIEW_TYPE_BANNER;
        item.bannerImgUrl = bannerImgs;
        return item;
    }

    /**
     * 创建普通列表类型的 DataItem 集合
     */
    private List<DataItem> createListDataItems(int count) {
        List<DataItem> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DataItem item = new DataItem();
            item.viewType = VIEW_TYPE_LIST;
            item.imgUrl = TestUrls.ImgUrls.get(i);
            list.add(item);
        }
        return list;
    }

    // ------------------------------------------------------------------------
    // Adapter 部分
    // ------------------------------------------------------------------------
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
                return new BannerHolder2(view);
            } else {
                View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_3, parent, false);
                return new ListHolder(view);
            }
        }

        @Override
        protected void bindViewHolder(@NonNull BaseListViewHolder<DataItem> holder, @NonNull DataItem item, int position) {
            holder.onBind(item, position);
        }

        /**
         * 停止所有 Banner 的轮播
         */
        void stopAllBanners() {
            for (int i = 0; i < getCount(); i++) {
                DataItem item = getData().get(i);
                if (item != null && item.viewType == VIEW_TYPE_BANNER && item.bannerHolder != null) {
                    item.bannerHolder.stopBanner();
                }
            }
        }

        // 普通列表 Item Holder
        static class ListHolder extends BaseListViewHolder<DataItem> {
            private final TextView title;
            private final GlideBgImageView webImageView;
            private final ListView childListView;
            private final ChildAdapter childAdapter;

            public ListHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                webImageView = itemView.findViewById(R.id.image_view);
                childListView = itemView.findViewById(R.id.child_list_view);
                childAdapter = new ChildAdapter(itemView.getContext());
                childListView.setAdapter(childAdapter);
            }

            @Override
            protected void bindViews(@NonNull View itemView) {

            }

            @Override
            public void onBind(@NonNull DataItem item, int position) {
                title.setText("标题 - " + position);
                webImageView.loadImage(item.imgUrl);

                // 为每个父列表项生成随机数量的子项
                List<DataItem> childItems = createChildDataItems(position);
                childAdapter.setDataList(childItems);
            }

            /**
             * 创建子列表数据
             */
            private List<DataItem> createChildDataItems(int parentPosition) {
                List<DataItem> list = new ArrayList<>();
                // 均匀分布 0-5 个子项
                int num = (int) (Math.random() * 6);
                for (int i = 0; i < num; i++) {
                    DataItem childItem = new DataItem();
                    childItem.viewType = VIEW_TYPE_LIST;
                    childItem.imgUrl = "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png";
                    list.add(childItem);
                }
                return list;
            }
        }


    }

    // Banner Item Holder
    private static class BannerHolder2 extends BaseListViewAdapter.BaseListViewHolder<DataItem> {
        private Banner banner;

        public BannerHolder2(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected void bindViews(@NonNull View itemView) {
            // banner 布局本身就是 Banner 控件
            banner = (Banner) itemView;
        }

        @Override
        public void onBind(@NonNull DataItem item, int position) {
            // 保存引用以便生命周期管理
            item.bannerHolder = BannerHolder2.this;

            if (item.bannerImgUrl != null) {
                banner.setAdapter(new BannerImageLoader(item.bannerImgUrl));
                banner.setBannerGalleryEffect(BANNER_GALLERY_EFFECT_RADIUS, BANNER_GALLERY_EFFECT_SPACE);
                banner.start();
            }
        }

        /**
         * 停止 Banner 轮播
         */
        void stopBanner() {
            if (banner != null) {
                banner.stop();
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
            private TextView childTitle;

            public ListHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            protected void bindViews(@NonNull View itemView) {
                childTitle = itemView.findViewById(R.id.child_title);
            }

            @Override
            public void onBind(@NonNull DataItem item, int position) {
                // 子列表项暂不需要显示内容，预留扩展
            }
        }
    }

    // ---------------- Data Model ----------------

    /**
     * 列表项数据模型
     */
    private static class DataItem {
        public int viewType;
        public String imgUrl;
        public List<String> bannerImgUrl;
        // 保存 BannerHolder 引用，用于生命周期管理
        public BannerHolder2 bannerHolder;
    }
}