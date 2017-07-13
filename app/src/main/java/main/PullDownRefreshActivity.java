package main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import base.MyBaseAdapter;
import cheerly.mybaseproject.R;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler2;
import widget.PullToRefresh;

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
                        Log.v("tag_3","refreshComplete");
                        mPullToRefresh.refreshComplete();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("tag_3","onLoadMoreBegin");
//                        mPullToRefresh.refreshComplete();
                    }
                }, 1500);
            }
        });


        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            list.add("" + i);
        }
        mAdapter.setDataList(list);
        mAdapter.notifyDataSetChanged();
    }

    private class MyAdapter extends MyBaseAdapter<String> {
        private Context mContext;

        public MyAdapter(Context context) {
            super(context);
            mContext = context;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.test_item, null);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText("标题 - " + position);

            return convertView;
        }
    }

}
