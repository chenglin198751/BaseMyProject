package base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cheerly.mybaseproject.R;
import helper.MainTitleHelper;
import httpwork.HttpUtils;
import utils.Constants;
import widget.BaseViewHelper;
import widget.WaitDialog;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public abstract class BaseActivity extends AppCompatActivity implements ImplBaseView {

    protected final static Gson gson = Constants.gson;
    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper = null;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private HashMap<String, Object> mTagMap;
    private boolean isAddedView = false;
    private View mContentView = null;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();

        setContentView(R.layout.base_activity_layout);
        mBaseRootView = findViewById(R.id.base_root);
        mTitleHelper = new MainTitleHelper(this);
        mBaseViewHelper = new BaseViewHelper(this);

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
    public final void setContentLayout(@LayoutRes int layoutResID) {
        if (mContentView != null) {
            mBaseRootView.removeView(mContentView);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
        params.addRule(RelativeLayout.BELOW, R.id.main_title);
        mContentView = View.inflate(this, layoutResID, null);
        mBaseRootView.addView(mContentView, params);
    }

    public final MainTitleHelper getTitleHelper() {
        return mTitleHelper;
    }

    @Override
    public void setTitle(@StringRes int titleId) {
        mTitleHelper.setTitle(titleId);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitleHelper.setTitle(title.toString());
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
        if (mTagMap != null) {
            mTagMap.clear();
        }
    }

    /**
     * post 请求，建议用这个
     */
    public void post(String url, Map<String, Object> map, final HttpUtils.HttpCallback httpCallback) {
        HttpUtils.HttpBuilder builder = new HttpUtils.HttpBuilder();
        builder.setCache(false);
        HttpUtils.postWithHeader(getContext(), url, null, map, builder, httpCallback);
    }

    @CallSuper
    public void onBroadcastReceiver(String action, Bundle bundle) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment) fragment).onBroadcastReceiver(action, bundle);
                }
            }
        }
    }


    public void addTag(String key, Object object) {
        if (mTagMap == null) {
            mTagMap = new HashMap<>();
        }
        mTagMap.put(key, object);
    }

    public HashMap<String, Object> getTagMap() {
        return mTagMap;
    }

    /**
     * 显示等待的对话框
     */
    @Override
    public final WaitDialog showWaitDialog(String text) {
        if (mWaitDialog == null) {
            mWaitDialog = new WaitDialog(getContext());
        }

        TextView textView = (TextView) mWaitDialog.findViewById(R.id.text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.data_loading);
        }
        if (!mWaitDialog.isShowing() && !isFinishing()) {
            mWaitDialog.show();
        }
        return mWaitDialog;
    }

    /**
     * 取消等待的对话框
     */
    @Override
    public final void dismissWaitDialog() {
        if (mWaitDialog != null && !isFinishing()) {
            mWaitDialog.dismiss();
        }
    }

    /**
     * 显示嵌入式进度条
     */
    @Override
    public final void showProgress(String text) {
        hideProgress();
        mBaseViewHelper.setLoadingText(text);
        addLoadView();
        mBaseViewHelper.addShadowView(mBaseRootView);
    }

    /**
     * 清除嵌入式进度条
     */
    @Override
    public final void hideProgress() {
        clearLoadingView();
    }

    /**
     * 清除contentView里面的加载进度
     */
    private void clearLoadingView() {
        if (isAddedView) {
            mBaseRootView.removeView(mBaseViewHelper.getView());
            mBaseViewHelper.removeShadowView(mBaseRootView);
            isAddedView = false;
        }
    }

    /**
     * 显示没有网络的界面
     */
    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        mBaseViewHelper.showNoNetView(getString(R.string.no_net_tips), listener);
        addLoadView();
    }

    /**
     * 清除没有网络的界面
     */
    @Override
    public final void hideNoNetView() {
        clearLoadingView();
    }

    /**
     * 显示空数据的界面
     */
    @Override
    public final void showEmptyView(String text, View.OnClickListener listener) {
        mBaseViewHelper.showEmptyText(text, listener);
        addLoadView();
    }

    /**
     * 清除空数据的界面
     */
    @Override
    public final void hideEmptyView() {
        clearLoadingView();
    }

    private void addLoadView() {
        if (!isAddedView) {
            isAddedView = true;
            mBaseViewHelper.getView().setClickable(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            params.addRule(RelativeLayout.BELOW, R.id.main_title);
            mBaseRootView.addView(mBaseViewHelper.getView(), params);
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseAction.ACTION_BASE_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BaseAction.ACTION_BASE_BROADCAST)) {
                String myAction = intent.getStringExtra("action");
                onBroadcastReceiver(myAction, intent.getBundleExtra("bundle"));
            }
        }
    };
}
