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

    private static final int PAGE_SIZE = 10;

    private SmartRefreshLayout refreshLayout;
    private TestRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private int loadedPosition = 0;
    private int positon = 0;
    private boolean isFirst = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            positon = getArguments().getInt("position");
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.test_snap_nest_fragment_layout;
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new TestRecyclerAdapter(view.getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refreshLayout = view.findViewById(R.id.swipe_refresh);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout layout) {
                AppUtils.getUiHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getData(true);
                        layout.finishRefresh();
                    }
                }, 500);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout layout) {
                AppUtils.getUiHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getData(false);
                        boolean hasData = adapter.getItemCount() < TestUrls.ImgUrls.size();
                        if (hasData) {
                            layout.finishLoadMore();
                        } else {
                            layout.finishLoadMoreWithNoMoreData();
                        }
                    }
                }, 500);
            }
        });

        if (positon == 0) {
            onSelected(0);
        }
    }

    @Override
    public void onSelected(int index) {
        super.onSelected(index);

        if (isFirst && index == positon) {
            isFirst = false;
            refreshLayout.autoRefresh();
        }
    }

    private void getData(boolean isRefresh) {
        List<String> list = new ArrayList<>();

        if (isRefresh) {
            loadedPosition = 0;
        }

        int startIndex = isRefresh ? 0 : loadedPosition;
        int remaining = TestUrls.ImgUrls.size() - startIndex;
        int loadCount = Math.min(PAGE_SIZE, remaining);

        for (int i = 0; i < loadCount; i++) {
            list.add("" + (startIndex + i));
        }

        if (isRefresh) {
            adapter.setDataList(list);
        } else {
            adapter.appendDataList(list);
        }

        loadedPosition += loadCount;
    }
}