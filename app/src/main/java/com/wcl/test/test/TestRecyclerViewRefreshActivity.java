package com.wcl.test.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.listener.OnSingleClickListener;
import com.wcl.test.utils.timer.CountDownManager;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.view.pullrefresh.PullToRefreshView;

import java.util.ArrayList;
import java.util.List;

/**
 * 资料：https://github.com/scwang90/SmartRefreshLayout
 * 支持每个Item独立倒计时的RecyclerView刷新示例。
 */
public class TestRecyclerViewRefreshActivity extends BaseActivity {
    private PullToRefreshView mPullToRefreshView;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    public static final String[] PIC_ARRAY = {
            "http://img.zcool.cn/community/01d4a0573bd4ba32f8757cb9f98a3f.gif",
            "https://b-ssl.duitang.com/uploads/item/201410/19/20141019095805_KaAju.thumb.700_0.gif",
            "https://b-ssl.duitang.com/uploads/blog/201501/02/20150102162511_8sA4h.thumb.700_0.gif",
            "http://img.zcool.cn/community/01bd32573bd4c432f8757cb9341633.gif",
            "http://img.zcool.cn/community/01c59d573bd4bc32f8757cb93c30b0.gif",
            "https://b-ssl.duitang.com/uploads/item/201510/06/20151006200129_HGuYP.thumb.700_0.gif",
            "http://img.zcool.cn/community/01d32c573bd4c36ac7253f9ac79aca.gif",
            "https://b-ssl.duitang.com/uploads/blog/201411/10/20141110185817_QUHed.thumb.700_0.gif",
            "http://img.zcool.cn/community/01eced573bd4b932f8757cb9ed9061.gif",
            "https://b-ssl.duitang.com/uploads/item/201411/24/20141124111818_tHQSz.thumb.700_0.gif",
            "http://img.zcool.cn/community/014da7573bd4bd6ac7253f9aea065b.gif",
            "http://5b0988e595225.cdn.sohucs.com/images/20170922/c7e95cf930a64a27b616e8c77525645b.jpeg",
            "http://www.95dm.com/a/pic/20151025/1-1505161500444V.gif"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_recyclerview_refresh_layout);

        getTitleHelper().hideTitleBar();
        mRecyclerView = findViewById(R.id.recycler_view);
        mPullToRefreshView = findViewById(R.id.swipe_refresh);

        mPullToRefreshView.setListener(new PullToRefreshView.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(() -> {
                    mAdapter.clear();
                    setData(PIC_ARRAY.length, true);
                    mPullToRefreshView.finishRefresh();
                }, 500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(() -> {
                    setData(PIC_ARRAY.length, false);
                    mPullToRefreshView.finishLoadMore();
                }, 500);
            }
        });

        mAdapter = new MyAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        setData(13, true);
    }

    @Override
    protected boolean onKeepSingleActivity() {
        return true;
    }

    private void setData(int count, boolean isRefresh) {
        List<ModelData> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ModelData modelData = new ModelData();
            modelData.url = PIC_ARRAY[i % PIC_ARRAY.length];
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
            // 仅刷新当前列表中的倒计时显示
            notifyItemRangeChanged(0, getItemCount(), "tick");
        }

        class ListHolder extends BaseRecyclerViewHolder {
            ImageView imageView;
            Button btnDelete;
            TextView countdowner;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                countdowner = itemView.findViewById(R.id.countdowner);
            }

            @Override
            public void onBind(final int position) {
                ModelData model = getData().get(position);
                SmartImageLoader.load(imageView, model.url, -1, -1, 0);
                btnDelete.setText("删除");

                // 倒计时展示
                updateCountdown(model);

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

                imageView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(v.getContext(), TestRecyclerViewRefreshActivity.class);
                        v.getContext().startActivity(intent);
                    }
                });
            }

            private void updateCountdown(ModelData model) {
                long remain = model.endTime - System.currentTimeMillis();
                if (remain <= 0) {
                    countdowner.setText("已结束");
                } else {
                    long sec = remain / 1000;
                    long min = sec / 60;
                    long s = sec % 60;
                    countdowner.setText(String.format("%02d:%02d", min, s));
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
        public int radius;
    }
}
