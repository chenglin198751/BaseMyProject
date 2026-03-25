package com.wcl.test.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.listener.OnSingleClickListener;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.utils.timer.CountDownManager;
import com.wcl.test.view.image.GlideBgImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 资料：https://github.com/scwang90/SmartRefreshLayout
 * 支持每个Item独立倒计时的RecyclerView刷新示例。
 */
public class TestRecyclerViewRefreshActivity extends BaseActivity {
    private static final String PAYLOAD_TICK = "tick";
    private static final String TEXT_DELETE = "删除";
    private static final String TEXT_EXPIRED = "已结束";
    private static final String TIME_FORMAT = "%02d:%02d";

    private SmartRefreshLayout refreshLayout;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_recyclerview_refresh_layout);

        getTitleHelper().hideTitleBar();
        mRecyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.swipe_refresh);

        // 配置刷新和加载监听
        refreshLayout.setOnRefreshListener(createRefreshListener());
        refreshLayout.setOnLoadMoreListener(createLoadMoreListener());

        mAdapter = new MyAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        setData(10, true);
    }

    @Override
    protected boolean onKeepSingleActivity() {
        return true;
    }

    /**
     * 创建下拉刷新监听器
     */
    private OnRefreshListener createRefreshListener() {
        return new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout layout) {
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
            public void onLoadMore(@NonNull RefreshLayout layout) {
                performRefresh(false, layout);
            }
        };
    }

    /**
     * 执行刷新（下拉刷新或上拉加载）
     *
     * @param isRefresh true 为下拉刷新（重置数据），false 为上拉加载（追加数据）
     * @param layout    刷新布局控件
     */
    private void performRefresh(final boolean isRefresh, final RefreshLayout layout) {
        AppUtils.getUiHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setData(10, isRefresh);
                if (isRefresh) {
                    layout.finishRefresh();
                } else {
                    layout.finishLoadMore();
                }
            }
        }, 500);
    }

    /**
     * 设置数据列表
     *
     * @param count     要生成的数据条数
     * @param isRefresh true 为刷新（替换数据），false 为加载更多（追加数据）
     */
    private void setData(int count, boolean isRefresh) {
        List<ModelData> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ModelData modelData = new ModelData();
            modelData.url = TestUrls.ImgUrls.get(i);
            // 模拟每个Item有不同倒计时：第i个比第一个多30秒
            modelData.endTime = now + (i + 1) * 30_000L;
            list.add(modelData);
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CountDownManager.getInstance().addListener(mAdapter);
        CountDownManager.getInstance().start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CountDownManager.getInstance().removeListener(mAdapter);
        CountDownManager.getInstance().stop();
    }

    // --------------------------
    // Adapter部分
    // --------------------------
    public static class MyAdapter extends BaseRecyclerViewAdapter<ModelData>
            implements CountDownManager.TickListener {

        private final Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.test_item, parent, false);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder) {
                ((ListHolder) holder).onBind(position);
            }
        }

        @Override
        public void onTick() {
            // 仅刷新当前列表中所有Item的倒计时显示
            notifyItemRangeChanged(0, getItemCount(), PAYLOAD_TICK);
        }

        class ListHolder extends BaseRecyclerViewHolder {
            private GlideBgImageView imageView;
            private Button btnDelete;
            private TextView countdowner;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                countdowner = itemView.findViewById(R.id.countdowner);
            }

            @Override
            public void onBind(final int position) {
                ModelData model = getData().get(position);
                imageView.loadImage(model.url);
                btnDelete.setText(TEXT_DELETE);

                // 倒计时展示
                updateCountdown(model);

                setupDeleteButton(position);
                setupImageClickListener();
            }

            /**
             * 设置删除按钮点击事件
             *
             * @param position 当前绑定位置
             */
            private void setupDeleteButton(final int position) {
                btnDelete.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        int currentPosition = getAbsoluteAdapterPosition();
                        if (currentPosition >= 0 && currentPosition < getData().size()) {
                            getData().remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                        }
                    }
                });
            }

            /**
             * 设置图片点击事件（跳转到当前Activity）
             */
            private void setupImageClickListener() {
                imageView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TestRecyclerViewRefreshActivity.class);
                        context.startActivity(intent);
                    }
                });
            }

            /**
             * 更新倒计时显示
             *
             * @param model 数据模型
             */
            private void updateCountdown(ModelData model) {
                long remain = model.endTime - System.currentTimeMillis();
                if (remain <= 0) {
                    countdowner.setText(TEXT_EXPIRED);
                } else {
                    long sec = remain / 1000;
                    long min = sec / 60;
                    long s = sec % 60;
                    countdowner.setText(String.format(TIME_FORMAT, min, s));
                }
            }
        }
    }

    // --------------------------
    // 数据模型
    // --------------------------
    public static final class ModelData {
        public String url;
        public long endTime; // 未来时间戳（单位：毫秒）
        // radius 字段已移除，因未使用
    }
}