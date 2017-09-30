package base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import cheerly.mybaseproject.R;
import helper.MainTitleHelper;
import utils.Constants;
import widget.LoadingViewHelper;
import widget.MyDialog;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public class BaseActivity extends AppCompatActivity {
    private final static String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
    protected final static Gson gson = Constants.gson;
    private MainTitleHelper mTitleHelper;
    private LoadingViewHelper mLoadingViewHelper = null;
    private MyDialog mWaitDialog;
    private RelativeLayout mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();

        setContentView(R.layout.base_activity_layout);
        mContentView = (RelativeLayout) this.findViewById(R.id.content_view);
        mTitleHelper = new MainTitleHelper(this);

        if (getTitle() != null) {
            mTitleHelper.setTitle(getTitle().toString());
        }
    }

    public BaseActivity getContext() {
        return this;
    }

    /**
     * 设置Activity的内容布局，取代setContentView() 方法
     */
    public final void setContentLayout(int layoutResID) {
        mContentView.addView(View.inflate(this, layoutResID, null), new RelativeLayout.LayoutParams(-1, -1));
    }

    public MainTitleHelper getTitleHelper() {
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

    public final void sendMyBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * 显示等待的对话框
     */
    public MyDialog showWaitDialog(String text) {
        if (mWaitDialog == null) {
            mWaitDialog = new MyDialog(getContext());
            View dialogView = View.inflate(getContext(), R.layout.progress_layout, null);
            mWaitDialog.setFullScreenView(dialogView);
        }

        if (!TextUtils.isEmpty(text)) {
            TextView textView = (TextView) mWaitDialog.findViewById(R.id.text);
            textView.setText(text);
        }
        mWaitDialog.show();
        return mWaitDialog;
    }

    /**
     * 取消等待的对话框
     */
    public void dismissWaitDialog() {
        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
        }
    }

    /**
     * 显示嵌入式进度条
     */
    public final void showProgress(String text) {
        removeProgress();
        addLoadView();
        if (text != null) {
            mLoadingViewHelper.setText(text);
        }
    }

    /**
     * 清除contentView里面的加载进度
     */
    public final void removeProgress() {
        if (mLoadingViewHelper != null) {
            mContentView.removeView(mLoadingViewHelper.getLoadingView());
            mLoadingViewHelper = null;
        }
    }

    /**
     * 显示没有网络的界面
     */
    public final void showNoNetView() {
        addLoadView();
        mLoadingViewHelper.showNoNetView();
    }

    /**
     * 显示没有网络的界面
     */
    public final void showNoNetView(View.OnClickListener listener, String text) {
        addLoadView();
        mLoadingViewHelper.showNoNetView(listener, text);
    }

    /**
     * 显示空数据的界面
     */
    public final void showEmptyView(String text) {
        addLoadView();
        mLoadingViewHelper.showEmptyView();
        if (!TextUtils.isEmpty(text)) {
            mLoadingViewHelper.setEmptyText(text);
        }
    }

    private void addLoadView() {
        if (mLoadingViewHelper == null) {
            mLoadingViewHelper = new LoadingViewHelper(this);
            mLoadingViewHelper.getLoadingView().setClickable(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            mContentView.addView(mLoadingViewHelper.getLoadingView(), params);
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
