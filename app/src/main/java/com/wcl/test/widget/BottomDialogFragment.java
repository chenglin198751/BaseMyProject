package com.wcl.test.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wcl.test.R;
import com.wcl.test.test.TestRecyclerAdapter;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用法：
 * BottomDialogFragment dialog = BottomDialogFragment.newInstance();
 * dialog.show(getSupportFragmentManager(), "bottomDialog");
 */
public class BottomDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView mRecyclerView;
    private TestRecyclerAdapter mAdapter;

    public static BottomDialogFragment newInstance() {
        return new BottomDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_listview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 RecyclerView 和 Adapter
        mRecyclerView = view.findViewById(R.id.list_view);
        mAdapter = new TestRecyclerAdapter(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        // 填充测试数据
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("Item " + i);
        }
        mAdapter.setDataList(list);
    }

    @Override
    public void onStart() {
        super.onStart();

        // 获取 BottomSheet view
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);

            // 允许向下滑动隐藏
            behavior.setHideable(true);
            // 跳过折叠状态，向下拖动直接关闭
            behavior.setSkipCollapsed(true);

            bottomSheet.getLayoutParams().height = AppConstants.screenHeight / 2;
            bottomSheet.requestLayout();
        }
    }
}