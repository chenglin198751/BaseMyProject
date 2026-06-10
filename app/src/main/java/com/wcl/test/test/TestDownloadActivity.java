package com.wcl.test.test;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.base.BaseRecyclerViewHolder;
import com.wcl.test.download.DownloadManager;
import com.wcl.test.download.ui.DownloadButton;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.view.RecyclerDivider;

public class TestDownloadActivity extends BaseActivity {
    private TestRecyclerAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("下载引擎测试");
        setContentLayout(R.layout.test_download_layout);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new TestRecyclerAdapter(getContext());
        adapter.setDataList(TestUrls.DownUrls);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerDivider(Color.BLUE, AppUtils.dp2px(5), false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.w("tag_99","33 DownloadManager.ins().getTasks().size()="+ DownloadManager.ins().getTasks().size());
    }

    public static class TestRecyclerAdapter extends BaseRecyclerViewAdapter<String> {
        private final BaseActivity activity;

        public TestRecyclerAdapter(BaseActivity activity) {
            this.activity = activity;
        }


        @NonNull
        @Override
        public BaseRecyclerViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.test_download_item, parent, false);
            return new ListHolder(view);
        }

        private static class ListHolder extends BaseRecyclerViewHolder<String> {
            DownloadButton downloadButton;
            Button delete;

            public ListHolder(View itemView) {
                super(itemView);
                downloadButton = itemView.findViewById(R.id.down_btn);
                delete = itemView.findViewById(R.id.delete);
            }

            @Override
            public void onBind(@NonNull String url, int position) {
                downloadButton.bind(url);
                delete.setOnClickListener(v -> {
                    DownloadManager.ins().delete(url);
                });
            }

        }

    }
}
