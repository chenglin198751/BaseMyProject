package com.wcl.test.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.wcl.test.R;
import com.wcl.test.helper.MainTitleHelper;
import com.wcl.test.main.MainActivity;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.widget.BaseViewHelper;
import com.wcl.test.widget.WaitDialog;

import java.util.List;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public abstract class BaseActivity extends AppCompatActivity implements ImplBaseView, OnBroadcastListener {
    private BroadcastReceiver mBroadcastReceiver = null;
    protected final static Gson gson = AppConstants.gson;
    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper = null;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private View mContentView = null;
    private ViewGroup mNestedParentLayout;
    public WindowInsetsControllerCompat windowInsetsController;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppConstants.Toggle.isGrayscale) {
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0f);
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        }

        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        setLightStatusBarColor(true);
        //设置是否沉浸状态栏，false 表示沉浸，true表示不沉浸
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        if (onKeepSingleActivity()) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseAction.Keys.ACTIVITY_NAME, this.getClass().getName());
            BaseAction.sendBroadcast(BaseAction.System.ACTION_KEEP_SINGLE_ACTIVITY, bundle);
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
     * 设置Activity的内容布局，取代系统的 setContentView() 方法
     */
    public final void setContentLayout(@LayoutRes int layoutResID) {
        View layoutView = View.inflate(this, layoutResID, null);
        setContentLayout(layoutView);
    }

    /**
     * 设置Activity的内容布局，取代系统的 setContentView() 方法
     */
    public final void setContentLayout(final View layoutView) {
        if (mContentView != null && mContentView.getParent() != null) {
            mBaseRootView.removeView(mContentView);
            mContentView = null;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
        params.addRule(RelativeLayout.BELOW, R.id.main_title);
        mContentView = layoutView;
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
        unregisterBroadcastReceiver();
    }


    @CallSuper
    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        if (BaseAction.System.ACTION_KEEP_SINGLE_ACTIVITY.equals(action)) {
            //根据开关onKeepSingleActivity()：当前Activity无论打开多少，只保留最后打开的一个
            if (onKeepSingleActivity()) {
                String className = this.getClass().getName();
                if (bundle != null && className.equals(bundle.getString(BaseAction.Keys.ACTIVITY_NAME))) {
                    finish();
                }
            }
        } else if (BaseAction.System.ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY.equals(action)) {
            //只保留MainActivity不关闭
            if (!this.getClass().getSimpleName().equals(MainActivity.CLASS_NAME)) {
                finish();
            }
        } else if (BaseAction.System.ACTION_BASE_BROADCAST.equals(action)) {
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
        if (!TextUtils.isEmpty(text)) {
            mBaseViewHelper.setLoadingText(text);
        } else {
            mBaseViewHelper.setLoadingText(null);
        }
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
     * 设置状态栏底色和文字颜色，true为黑色，false为白色
     */
    protected void setLightStatusBarColor(boolean isLightBar) {
        //设置状态栏底色为白色
        getWindow().setStatusBarColor(Color.WHITE);
        // 控制状态栏字体颜色，true为黑色，false为白色
        windowInsetsController.setAppearanceLightStatusBars(isLightBar);
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
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(BaseAction.System.ACTION_BASE_BROADCAST)) {
                        String myAction = intent.getStringExtra("action");
                        onBroadcastReceiver(myAction, intent.getBundleExtra("bundle"));
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BaseAction.System.ACTION_BASE_BROADCAST);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        }
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

}
