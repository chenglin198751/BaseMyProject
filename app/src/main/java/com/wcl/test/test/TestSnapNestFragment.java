package com.wcl.test.test;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class TestSnapNestFragment extends BaseFragment {

    private SmartRefreshLayout refreshLayout;
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
        refreshLayout = view.findViewById(R.id.swipe_refresh);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {

                AppUtils.getUiHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(10, true);
                        refreshlayout.finishRefresh();
                    }
                }, 500);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                AppUtils.getUiHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(10, false);
                        refreshlayout.finishLoadMore();
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
