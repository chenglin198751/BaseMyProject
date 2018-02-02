package test;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import base.BaseActivity;
import base.BaseRecyclerViewAdapter;
import base.BaseRecyclerViewHolder;
import bean.ApkItem;
import cheerly.mybaseproject.R;
import pullrefresh.PullToRefresh;
import utils.MyUtils;

public class TestActivity extends BaseActivity {
    private PullToRefresh mPullToRefresh;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.recyclerview_refresh_layout);


        getTitleHelper().hideTitle();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefresh = (PullToRefresh) findViewById(R.id.swipe_refresh);
        mPullToRefresh.setEnableLoadmore(false);

        mPullToRefresh.setListener(new PullToRefresh.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        getData();
                        mPullToRefresh.finishRefresh();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                        mPullToRefresh.finishLoadmore();
                    }
                }, 500);
            }
        });


        mAdapter = new MyAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mPullToRefresh.autoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TestCacheUtils.shutdown();
    }

    private void getData() {
        mAdapter.getData().clear();
        new Thread() {
            @Override
            public void run() {
                super.run();

                List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
                for (int i = 0; i < packages.size(); i++) {
                    PackageInfo packageInfo = packages.get(i);
                    ApkItem appInfo = new ApkItem();
                    appInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                    appInfo.packageName = packageInfo.packageName;
                    appInfo.appVersion = packageInfo.versionName;
                    appInfo.versionCode = packageInfo.versionCode;
//                    appInfo.image = packageInfo.applicationInfo.loadIcon(getPackageManager());
                    appInfo.packageInfo = packageInfo;
                    mAdapter.getData().add(appInfo);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mPullToRefresh.finishRefresh(true);
                    }
                });
            }
        }.start();


    }

    public static class MyAdapter extends BaseRecyclerViewAdapter<ApkItem> {
        private Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.test_apk_item, parent, false);
            return new ListHolder(itemView);
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder) {
                ListHolder listHolder = (ListHolder) holder;
                listHolder.setData(position);
            }

        }


        class ListHolder extends BaseRecyclerViewHolder {
            ImageView imageView;
            TextView appName;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
                appName = (TextView) itemView.findViewById(R.id.app_name);
            }

            public void setData(int position) {
                final ApkItem item = getData().get(position);
                appName.setText(item.appName);
                imageView.setTag(item);

                TestCacheUtils.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {
                        final Drawable drawable = item.packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());

                        MyUtils.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (imageView.getTag() != null && imageView.getTag() == item) {
                                    imageView.setImageDrawable(drawable);
                                }
                            }
                        });

                    }
                });
            }

        }
    }

}
