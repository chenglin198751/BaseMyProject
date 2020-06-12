package cheerly.mybaseproject.test;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.bean.ApkItem;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.view.pullrefresh.PullToRefreshView;
import cheerly.mybaseproject.utils.BaseUtils;

public class TestActivity extends BaseActivity {
    private ProgressBar mProgressBar;
    private int mProgress = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_activity_layout);

        mProgressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = mProgress + 5;
                mProgressBar.setProgress(mProgress);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
