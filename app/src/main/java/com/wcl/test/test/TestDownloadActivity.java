package com.wcl.test.test;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.base.BaseRecyclerViewHolder;
import com.wcl.test.download.ui.DownloadButton;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.view.RecyclerDivider;
import com.wcl.test.view.image.GlideImageView;

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
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerDivider(Color.BLUE, AppUtils.dp2px(20), AppUtils.dp2px(5), true));
    }


    public static class TestRecyclerAdapter extends BaseRecyclerViewAdapter<String> {

        private final Context mContext;

        public TestRecyclerAdapter(Context context) {
            this.mContext = context;
        }


        @NonNull
        @Override
        public BaseRecyclerViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.test_download_item, parent, false);
            return new ListHolder(view);
        }

        private static class ListHolder extends BaseRecyclerViewHolder<String> {
            DownloadButton downloadButton;
            TextView title;

            public ListHolder(View itemView) {
                super(itemView);
                downloadButton = itemView.findViewById(R.id.down_btn);
                title = itemView.findViewById(R.id.title);
            }

            @Override
            public void onBind(@NonNull String url, int position) {

            }

        }

    }
}
