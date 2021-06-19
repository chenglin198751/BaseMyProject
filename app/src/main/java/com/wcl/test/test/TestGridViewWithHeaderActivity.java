package com.wcl.test.test;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;

public class TestGridViewWithHeaderActivity extends BaseActivity {
    private NestedScrollView nestedScrollView;

    private RecyclerView mRecyclerView;
    private TestRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_gridview_with_header);

        mRecyclerView = findViewById(R.id.recycler_view);
        nestedScrollView = findViewById(R.id.nested_scroll_view);

        mAdapter = new TestRecyclerAdapter(getContext());
        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(20, true);
    }

    private boolean setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter.getItemCount() + i));
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }

        if (mAdapter.getItemCount() > 20) {
            return false;
        }

        return true;
    }

}
