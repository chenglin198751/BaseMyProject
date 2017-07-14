package main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import view.PullToRefresh;

public class RecyclerViewRefreshActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.recyclerview_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefresh = (PullToRefresh) findViewById(R.id.swipe_refresh);

        mPullToRefresh.setListener(new PullToRefresh.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        setData(15, true);
                        mPullToRefresh.refreshComplete();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(5, false);
                        mPullToRefresh.refreshComplete();
                    }
                }, 1500);
            }
        });


        mAdapter = new MyAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(10, true);

    }

    private void setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter.getItemCount() + i));
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ListHolder> {
        private Context mContext;
        List<String> dataList = new ArrayList<String>();

        public MyAdapter(Context context) {
            this.mContext = context;
        }

        public void setDataList(List<String> list) {
            if (null != list) {
                dataList.clear();
                dataList.addAll(list);
                notifyDataSetChanged();
            }
        }


        public void appendDataList(List<String> list) {
            if (null != list) {
                dataList.addAll(list);
                notifyDataSetChanged();
            }
        }

        public void clear() {
            dataList.clear();
            notifyDataSetChanged();
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.test_item, null);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position) {
            holder.setData(position);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }


        class ListHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView content;

            public ListHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                content = (TextView) itemView.findViewById(R.id.content);
            }

            public void setData(int position) {
                title.setText("标题 " + position);
            }


        }
    }

}
