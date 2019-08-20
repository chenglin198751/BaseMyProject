package cheerly.mybaseproject.test;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.utils.Constants;
import cheerly.mybaseproject.utils.SmartImageLoader;
import cheerly.mybaseproject.view.pullrefresh.PullToRefreshView;
import cheerly.mybaseproject.widget.ViewPagerLayoutManager;

/**
 * 资料：https://github.com/scwang90/SmartRefreshLayout
 */
public class TestRecyclerViewRefreshActivity extends BaseActivity {
    private PullToRefreshView mPullToRefreshView;
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
            , "http://5b0988e595225.cdn.sohucs.com/images/20170922/c7e95cf930a64a27b616e8c77525645b.jpeg",
            "http://www.95dm.com/a/pic/20151025/1-1505161500444V.gif"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_recyclerview_refresh_layout);


        getTitleHelper().hideTitleBar();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.swipe_refresh);

        mPullToRefreshView.setListener(new PullToRefreshView.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        setData(PIC_ARRAY.length, true);
                        mPullToRefreshView.finishRefresh();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(PIC_ARRAY.length, false);
                        mPullToRefreshView.finishLoadMore();
                    }
                }, 500);
            }
        });


        mAdapter = new MyAdapter(this);
        ViewPagerLayoutManager mLayoutManager = new ViewPagerLayoutManager(getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(13, true);

        mLayoutManager.setOnViewPagerListener(new ViewPagerLayoutManager.OnViewPagerListener() {
            @Override
            public void onPageRelease(boolean isNext, int position) {

            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {

            }

            @Override
            public void onLayoutComplete() {

            }
        });
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
            View view = LayoutInflater.from(mContext).inflate(R.layout.test_item, parent, false);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder) {
                ListHolder listHolder = (ListHolder) holder;
                listHolder.onBind(position);
            }

        }


        class ListHolder extends BaseRecyclerViewHolder {
            ImageView imageView;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);

                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = Constants.getScreenWidth();
                params.height = Constants.getScreenHeight();
                imageView.setLayoutParams(params);
            }

            @Override
            public void onBind(int position) {
                SmartImageLoader.getInstance().load(imageView, getData().get(position).url, -1, -1);
            }


        }
    }

    public static final class ModelData {
        public String url;
        public int radius;
    }

}
