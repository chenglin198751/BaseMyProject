package com.wcl.test.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import com.wcl.test.R;
import com.wcl.test.test.TestRecyclerAdapter;
import com.wcl.test.utils.Constants;


/**
 * 用法：
 * BottomDialogFragment dialog = BottomDialogFragment.newInstance();
 * dialog.show(getSupportFragmentManager(), "");
 */
public class BottomDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView mRecyclerView;
    private TestRecyclerAdapter mAdapter;


    public static BottomDialogFragment newInstance() {
        final BottomDialogFragment fragment = new BottomDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_listview, container, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Constants.screenHeight / 3 * 2));
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new TestRecyclerAdapter(getContext());
        mRecyclerView = view.findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("" + (mAdapter.getItemCount() + i));
        }
        mAdapter.setDataList(list);
    }


}