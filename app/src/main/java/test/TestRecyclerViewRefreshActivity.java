package test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import base.BaseRecyclerViewAdapter;
import base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.R;
import pullrefresh.PullToRefresh;
import utils.Constants;
import view.KeepScaleImageView;
import view.WebImageView;

/**
 * 资料：https://github.com/scwang90/SmartRefreshLayout
 */
public class TestRecyclerViewRefreshActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    public static final String[] PIC_ARRAY = {"http://img.zcool.cn/community/01d4a0573bd4ba32f8757cb9f98a3f.gif"
            , "https://b-ssl.duitang.com/uploads/item/201410/19/20141019095805_KaAju.thumb.700_0.gif"
            , "https://b-ssl.duitang.com/uploads/blog/201501/02/20150102162511_8sA4h.thumb.700_0.gif"
            , "http://img.zcool.cn/community/01bd32573bd4c432f8757cb9341633.gif"
            , "http://img.zcool.cn/community/01c59d573bd4bc32f8757cb93c30b0.gif"
            , "https://b-ssl.duitang.com/uploads/item/201510/06/20151006200129_HGuYP.thumb.700_0.gif"
            , "http://img.zcool.cn/community/01d32c573bd4c36ac7253f9ac79aca.gif"
            , "https://b-ssl.duitang.com/uploads/blog/201411/10/20141110185817_QUHed.thumb.700_0.gif"
            , "http://img.zcool.cn/community/01eced573bd4b932f8757cb9ed9061.gif"
            , "https://b-ssl.duitang.com/uploads/item/201411/24/20141124111818_tHQSz.thumb.700_0.gif"
            , "http://img.zcool.cn/community/014da7573bd4bd6ac7253f9aea065b.gif"
            , "http://www.95dm.com/a/pic/20151025/1-1505161500444V.gif",
            "https://imgsa.baidu.com/forum/w%3D580/sign=8bf66bb9eaf81a4c2632ecc1e7286029/2456e6fb43166d22c88fcc59472309f79252d2b5.jpg"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.recyclerview_refresh_layout);


        getTitleHelper().hideTitle();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefresh = (PullToRefresh) findViewById(R.id.swipe_refresh);

        mPullToRefresh.setListener(new PullToRefresh.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        setData(13, true);
                        mPullToRefresh.finishRefresh();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(13, false);
                        mPullToRefresh.finishLoadmore();
                    }
                }, 1500);
            }
        });


        mAdapter = new MyAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(13, true);

    }

    private void setData(int count, boolean isRefresh) {
        List<ModelData> list = new ArrayList<ModelData>();
        for (int i = 0; i < count; i++) {
            ModelData modelData = new ModelData();
            modelData.url = PIC_ARRAY[i];
            list.add(modelData);
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }
    }

    public static class MyAdapter extends BaseRecyclerViewAdapter<ModelData> {
        private Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }


        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.test_item, null);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder) {
                ListHolder listHolder = (ListHolder) holder;
                listHolder.setData(position);
            }

        }


        class ListHolder extends BaseRecyclerViewHolder {
            TextView title;
            TextView content;
            WebImageView imageView;

            public ListHolder(View itemView) {
                super(itemView);
//                title = (TextView) itemView.findViewById(R.id.title);
//                content = (TextView) itemView.findViewById(R.id.content);
                imageView = (WebImageView) itemView.findViewById(R.id.image_view);
            }

            public void setData(int position) {
//                title.setText("标题 " + position);
                imageView.load(getData().get(position).url, -1, -1);
            }


        }
    }

    public static final class ModelData {
        public String url;
        public int radius;
    }

}
