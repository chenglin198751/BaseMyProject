package cheerly.mybaseproject.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.helper.MainTitleHelper;
import cheerly.mybaseproject.httpwork.HttpUtils;
import cheerly.mybaseproject.main.MainActivity;
import cheerly.mybaseproject.utils.Constants;
import cheerly.mybaseproject.widget.BaseViewHelper;
import cheerly.mybaseproject.widget.WaitDialog;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public abstract class BaseActivity extends AppCompatActivity implements ImplBaseView, OnBroadcastListener {
    protected final static Gson gson = Constants.gson;
    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper = null;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private View mContentView = null;
    private ViewGroup mNestedParentLayout;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarLightDark(true);

        if (onKeepSingleActivity()) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseAction.Keys.ACTIVITY_NAME, this.getClass().getName());
            BaseAction.sendBroadcast(BaseAction.ACTION_KEEP_SINGLE_ACTIVITY, bundle);
        }
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
     * 已弃用，请使用 {@link #setContentLayout}.
     */
    @Deprecated
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    /**
     * 已弃用，请使用 {@link #setContentLayout}.
     */
    @Deprecated
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    /**
     * 已弃用，请使用 {@link #setContentLayout}.
     */
    @Deprecated
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    /**
     * 已弃用，请使用 {@link #setContentLayout}.
     */
    @Deprecated
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    /**
     * 设置Activity的内容布局，取代setContentView() 方法
     */
    public final void setContentLayout(@LayoutRes int layoutResID) {
        if (mContentView != null && mContentView.getParent() != null) {
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

    /**
     * 子类实现此方法，返回true就是：
     * 设置是否保留此Activity只存在一个。
     * 比如某个场景全部评论列表页被多次打开，那么只保留最后一次被打开的页面。
     */
    protected boolean onKeepSingleActivity() {
        return false;
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }
    }

    /**
     * 内置 post 请求，建议用这个，可以防止内存泄漏
     */
    public void post(String url, Map<String, Object> map, final HttpUtils.HttpCallback httpCallback) {
        HttpUtils.HttpBuilder builder = new HttpUtils.HttpBuilder();
        builder.setCache(false);
        HttpUtils.postWithHeader(getContext(), url, null, map, builder, httpCallback);
    }

    /**
     * 内置 get 请求，建议用这个，可以防止内存泄漏
     */
    public void get(String url, final HttpUtils.HttpCallback httpCallback) {
        HttpUtils.get(getContext(), url, httpCallback);
    }

    @CallSuper
    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        if (BaseAction.ACTION_KEEP_SINGLE_ACTIVITY.equals(action)) {
            //根据开关onKeepSingleActivity()：当前Activity无论打开多少，只保留最后打开的一个
            if (onKeepSingleActivity()) {
                String className = this.getClass().getName();
                if (bundle != null && className.equals(bundle.getString(BaseAction.Keys.ACTIVITY_NAME))) {
                    finish();
                }
            }
        } else if (BaseAction.ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY.equals(action)) {
            //只保留MainActivity不关闭
            if (!this.getClass().getSimpleName().equals(MainActivity.CLASS_NAME)) {
                finish();
            }
        } else {
            //通知Activity里面所有的fragment接收广播
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() > 0) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof BaseFragment) {
                        ((BaseFragment) fragment).onBroadcastReceiver(action, bundle);
                    }
                }
            }
        }
    }

    /**
     * 显示等待的对话框
     */
    @Override
    public final WaitDialog showWaitDialog() {
        if (mWaitDialog == null) {
            mWaitDialog = new WaitDialog(getContext());
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
        if (mBaseViewHelper.getView() == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) mBaseViewHelper.getView().getParent();
        if (parent != null) {
            parent.removeView(mBaseViewHelper.getView());
        }
    }

    /**
     * 显示没有网络的界面
     */
    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        hideNoNetView();
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
        hideEmptyView();
        mBaseViewHelper.showEmptyText(text, listener);
        addLoadView();
    }

    /**
     * 设置空页面或者无网页面要附加的Parent Layout，不设置是整个父布局。
     */
    @Override
    public void setNestedParentLayout(ViewGroup parent) {
        mNestedParentLayout = parent;
    }

    /**
     * 设置状态栏文字和颜色，不兼容Android6.0以下
     */
    public void setStatusBarLightDark(boolean isLightBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int color = getColor(isLightBar ? R.color.white : R.color.black);
            StatusBarCompat.setStatusBarColor(this, color, isLightBar);
        }
    }

    /**
     * 清除空数据的界面
     */
    @Override
    public final void hideEmptyView() {
        clearLoadingView();
    }

    private void addLoadView() {
        mBaseViewHelper.getView().setClickable(true);

        if (mNestedParentLayout != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            mNestedParentLayout.addView(mBaseViewHelper.getView(), params);
        } else {
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
