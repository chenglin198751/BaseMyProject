package base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import cheerly.mybaseproject.R;
import helper.MainTitleHelper;
import widget.LoadingView;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public class BaseActivity extends Activity {
    private final static String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
    private MainTitleHelper mTitleHelper;
    private LoadingView mLoadingView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();

        setContentView(R.layout.base_activity_layout);
        mTitleHelper = new MainTitleHelper(this);
    }

    /**
     * 设置Activity的内容布局，取代setContentView() 方法
     */
    public void setContentLayout(int layoutResID) {
        LinearLayout content_linear = (LinearLayout) this.findViewById(R.id.content_view);
        content_linear.addView(View.inflate(this, layoutResID, null), new LinearLayout.LayoutParams(-1, -1));
    }

    private MainTitleHelper getTitleHelper() {
        return mTitleHelper;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
    }

    public void onMyBroadcastReceive(String action, Bundle bundle) {
    }

    public void sendMyBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * 显示嵌入式进度条
     */
    public void showProgress() {
        removeProgress();
        addLoadView();
    }

    /**
     * 设置进度要要显示的文字提示
     */
    public void setProgressText(String text) {
        mLoadingView.setText(text);
    }

    /**
     * 清除contentView里面的加载进度
     */
    public void removeProgress() {
        if (mLoadingView != null) {
            LinearLayout contentView = (LinearLayout) this.findViewById(R.id.content_view);
            contentView.removeView(mLoadingView.getLoadingView());
            mLoadingView = null;
        }
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView() {
        addLoadView();
        mLoadingView.showNoNetView();
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView(View.OnClickListener listener, String text) {
        addLoadView();
        mLoadingView.showNoNetView(listener, text);
    }

    /**
     * 显示空数据的界面
     */
    public void showEmptyView(String text) {
        addLoadView();
        mLoadingView.showEmptyView();
        if (!TextUtils.isEmpty(text)) {
            mLoadingView.setEmptyText(text);
        }
    }

    private void addLoadView() {
        if (mLoadingView == null) {
            mLoadingView = new LoadingView(this);
            LinearLayout contentView = (LinearLayout) this.findViewById(R.id.content_view);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
            contentView.addView(mLoadingView.getLoadingView(), 0, params);
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BASE_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_BASE_BROADCAST)) {
                String myAction = intent.getStringExtra("action");
                onMyBroadcastReceive(myAction, intent.getBundleExtra("bundle"));
            }
        }
    };
}
