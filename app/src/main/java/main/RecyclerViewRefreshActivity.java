package main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import base.BaseActivity;
import base.BaseRecyclerViewHolder;
import base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.R;
import utils.Constants;
import pullrefresh.PullToRefresh;
import view.WebImageView;

/**
 * 资料：https://github.com/scwang90/SmartRefreshLayout
 */
public class RecyclerViewRefreshActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    public static final String[] PIC_ARRAY = {"http://d.hiphotos.baidu.com/zhidao/pic/item/b8389b504fc2d5621e910222e31190ef77c66c60.jpg"
            , "http://img1.szhk.com/Image/2016/05/31/1464664339487.png"
            , "http://img3.duitang.com/uploads/item/201605/26/20160526235927_ZHh5A.jpeg"
            , "http://c.hiphotos.baidu.com/zhidao/pic/item/1e30e924b899a901e63b00dd18950a7b0308f5cb.jpg"
            , "http://img.weixinyidu.com/150603/05d6e52d.jpg"
            , "http://img.mp.itc.cn/upload/20161225/aba0f4945c2341b4942b79005c37e489_th.jpg"
            , "http://n.sinaimg.cn/translate/20170911/a1gc-fykuffc5152614.jpg"
            , "http://media.teeqee.com/game/img/214/184511b1760bc9476170f405d978bdb6.jpg"
            , "http://aliimg.changba.com/cache/photo/4310970_640_640.jpg"
            , "http://img2.niutuku.com/desk/1207/1658/bizhi-1658-15519.jpg"};

    public static final int[] RADIUS_ARRAY = {10, 20, 30, 40, 50, 60, 9, 14, 0, 5};

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
                        setData(10, true);
                        mPullToRefresh.finishRefresh();
                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(10, false);
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
        setData(10, true);

    }

    private void setData(int count, boolean isRefresh) {
        List<ModelData> list = new ArrayList<ModelData>();
        for (int i = 0; i < count; i++) {
            ModelData modelData = new ModelData();
            modelData.url = PIC_ARRAY[i];
            modelData.radius = RADIUS_ARRAY[i];
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

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                params.width = Constants.screenWidth;
                params.height = params.width;
                imageView.setLayoutParams(params);
            }

            public void setData(int position) {
//                title.setText("标题 " + position);
                imageView.load(getData().get(position).url, Constants.screenWidth, Constants.screenWidth);
            }


        }
    }

    public static final class ModelData {
        public String url;
        public int radius;
    }

}
