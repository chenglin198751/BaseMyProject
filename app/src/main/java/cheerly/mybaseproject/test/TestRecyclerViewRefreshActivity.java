package cheerly.mybaseproject.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.listener.OnSingleClickListener;
import cheerly.mybaseproject.utils.Constants;
import cheerly.mybaseproject.utils.SmartImageLoader;
import cheerly.mybaseproject.view.pullrefresh.PullToRefreshView;
import cheerly.mybaseproject.widget.ToastUtils;
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
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(13, true);
    }

    @Override
    protected boolean onKeepSingleActivity() {
        return true;
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
            Button btnDelete;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }

            @Override
            public void onBind(final int position) {
                Log.v("tag_5","position = " + position);
                SmartImageLoader.load(imageView, getData().get(position).url, -1, -1, 0);
                btnDelete.setText("删除 " + getAdapterPosition());
                btnDelete.setTag(getData().get(position).url);
                btnDelete.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        String url = (String) v.getTag();
                        int index = 0;
                        for (int i = 0; i < getData().size(); i++) {
                            if (getData().get(i).url.equals(url)){
                                index = i;
                                break;
                            }
                        }

                        Log.v("tag_999","index = " + index);
                        getData().remove(index);
                        notifyItemRemoved(index);
                    }
                });

                imageView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        Intent intent = new Intent(v.getContext(), TestRecyclerViewRefreshActivity.class);
                        v.getContext().startActivity(intent);
                    }
                });
            }

        }
    }

    public static final class ModelData {
        public String url;
        public int radius;
    }

}
