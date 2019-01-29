package cheerly.mybaseproject.test;

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

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.bean.ApkItem;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.pullrefresh.PullToRefreshView;
import cheerly.mybaseproject.utils.BaseUtils;

public class TestActivity extends BaseActivity {
    private PullToRefreshView mPullToRefreshView;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_recyclerview_refresh_layout);


        getTitleHelper().hideTitleBar();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.swipe_refresh);
        mPullToRefreshView.setEnableLoadMore(false);

        mPullToRefreshView.setListener(new PullToRefreshView.onListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        getData();
                        mPullToRefreshView.finishRefresh();
                    }
                }, 500);
            }

            @Override
            public void onLoadMore() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                        mPullToRefreshView.finishLoadMore();
                    }
                }, 500);
            }
        });


        mAdapter = new MyAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mPullToRefreshView.autoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        mPullToRefreshView.finishRefresh(true);
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
                listHolder.onBind(position);
            }

        }


        class ListHolder extends BaseRecyclerViewHolder {
            ImageView imageView;
            TextView appName;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                appName = itemView.findViewById(R.id.app_name);
            }

            @Override
            public void onBind(int position) {
                final ApkItem item = getData().get(position);
                appName.setText(item.appName);
                imageView.setTag(item);

                TestCacheUtils.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {
                        final Drawable drawable = item.packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());

                        BaseUtils.getHandler().post(new Runnable() {
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
