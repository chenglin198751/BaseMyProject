package com.wcl.test.test;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.view.pullrefresh.PullToRefreshView;

public class TestSnapNestFragment extends BaseFragment {

    private PullToRefreshView mPullToRefreshView;
    private TestRecyclerAdapter mAdapter2;
    private RecyclerView mRecyclerView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.test_recyclerview_refresh_layout;
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mAdapter2 = new TestRecyclerAdapter(view.getContext());

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("" + (mAdapter2.getItemCount() + i));
        }
        mAdapter2.setDataList(list);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter2);


        mPullToRefreshView = view.findViewById(R.id.swipe_refresh);

        mPullToRefreshView.setListener(new PullToRefreshView.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter2.clear();
                        setData(10, true);
                        mPullToRefreshView.finishRefresh();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(10, false);
                        mPullToRefreshView.finishLoadMore();
                    }
                }, 500);
            }
        });
    }

    private void setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter2.getItemCount() + i));
        }

        if (isRefresh) {
            mAdapter2.setDataList(list);
        } else {
            mAdapter2.appendDataList(list);
        }
    }
}
