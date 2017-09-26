package main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import base.BaseListViewAdapter;
import cheerly.mybaseproject.R;
import view.PullToRefresh;

public class PullDownRefreshActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private ListView mListView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.pull_down_refresh_layout);

        getTitleHelper().setTitle("测试");
        mListView = (ListView) findViewById(R.id.list_view);

        mPullToRefresh = (PullToRefresh) findViewById(R.id.swipe_refresh);

        mPullToRefresh.setListener(new PullToRefresh.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        setData(15, true);
                        mPullToRefresh.finishRefresh();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(5, false);
                        mPullToRefresh.finishLoadmore();
                    }
                }, 1500);
            }
        });


        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        setData(15, true);
    }

    private void setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter.getCount() + i));
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }

    }

    private static class MyAdapter extends BaseListViewAdapter<String> {
        private Context mContext;

        public MyAdapter(Context context) {
            super(context);
            mContext = context;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.test_item_2, null);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText("标题 - " + position);

            return convertView;
        }
    }

}
