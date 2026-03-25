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
    private TestRecyclerAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected int getContentLayout() {
        return R.layout.test_recyclerview_refresh_layout;
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new TestRecyclerAdapter(view.getContext());

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("" + (adapter.getItemCount() + i));
        }
        adapter.setDataList(list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refreshLayout = view.findViewById(R.id.swipe_refresh);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                loadData(10, true, refreshlayout);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                loadData(10, false, refreshlayout);
            }
        });
    }

    /**
     * 加载数据
     *
     * @param count         数据数量
     * @param isRefresh     是否为刷新操作
     * @param refreshLayout 刷新布局
     */
    private void loadData(int count, boolean isRefresh, RefreshLayout refreshLayout) {
        AppUtils.getUiHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setData(count, isRefresh);
                if (isRefresh) {
                    refreshLayout.finishRefresh();
                } else {
                    refreshLayout.finishLoadMore();
                }
            }
        }, 500);
    }

    /**
     * 设置数据
     *
     * @param count     数据数量
     * @param isRefresh 是否为刷新操作
     */
    private void setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("" + (adapter.getItemCount() + i));
        }

        if (isRefresh) {
            adapter.setDataList(list);
        } else {
            adapter.appendDataList(list);
        }
    }
}