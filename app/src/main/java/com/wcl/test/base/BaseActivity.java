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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.wcl.test.R;
import com.wcl.test.helper.MainTitleHelper;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.widget.BaseViewHelper;
import com.wcl.test.widget.WaitDialog;

import java.util.List;

/**
 * Activity 基类
 *
 * @author weiChengLin 2013-06-20
 */
public abstract class BaseActivity extends AppCompatActivity implements ImplBaseView, OnBroadcastListener {
    public static final String CLASS_NAME = "MainActivity";
    protected static final Gson gson = AppConstants.gson;

    private BroadcastReceiver mBroadcastReceiver;
    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private View mContentView;
    private ViewGroup mNestedParentLayout;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyGrayScaleIfNeeded();

        if (onKeepSingleActivity()) {
            Bundle bundle = new Bundle();
            bundle.putString("activity_name", getClass().getName());
            EventBus.sendBroadcast(EventBus.System.ACTION_KEEP_SINGLE_ACTIVITY, bundle);
        }
        registerBroadcastReceiver();

        setContentView(R.layout.base_activity_layout);
        mBaseRootView = findViewById(R.id.base_root);
        mTitleHelper = new MainTitleHelper(this);
        mBaseViewHelper = new BaseViewHelper(this);

        if (getTitle() != null) {
            mTitleHelper.setTitle(getTitle().toString());
        }

        setupSystemBars();
        setupInsetsIfNeeded();
    }

    private void applyGrayScaleIfNeeded() {
        if (AppConstants.Toggle.isGrayscale) {
            Paint paint = new Paint();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
            getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        }
    }

    private void setupSystemBars() {
        // 边到边（edge-to-edge），false沉浸式，true不沉浸
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (!AppBaseUtils.isEdgeToEdge()) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 状态栏黑色文字
        View decorView = getWindow().getDecorView();
        new WindowInsetsControllerCompat(getWindow(), decorView)
                .setAppearanceLightStatusBars(true);
    }

    // 是否显示在顶部挖口屏内
    private void setupInsetsIfNeeded() {
        if (!onDisplayInCutoutMode()) {
            ViewCompat.setOnApplyWindowInsetsListener(mBaseRootView, (v, insets) -> {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                v.setPadding(0, statusBarHeight, 0, navBarHeight);
                return insets;
            });
        }
    }

    public BaseActivity getContext() {
        return this;
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
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        mTitleHelper.setTitle(title != null ? title.toString() : "");
    }

    /**
     * 子类实现此方法，返回true就是：
     * 设置是否保留此Activity只存在一个。
     * 比如某个场景全部评论列表页被多次打开，那么只保留最后一次被打开的页面。
     */
    protected boolean onKeepSingleActivity() {
        return false;
    }

    /**
     * 是否显示在顶部挖口屏内
     */
    protected boolean onDisplayInCutoutMode() {
        return true;
    }

    @CallSuper
    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        if (EventBus.System.ACTION_KEEP_SINGLE_ACTIVITY.equals(action) && onKeepSingleActivity()) {
            String className = getClass().getName();
            if (bundle != null && className.equals(bundle.getString("activity_name"))) {
                finish();
            }
        } else if (EventBus.System.ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY.equals(action)) {
            if (!getClass().getSimpleName().equals(CLASS_NAME)) {
                finish();
            }
        } else {
            //通知Activity里面所有的fragment接收广播
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseFragment && fragment.isAdded()) {
                    ((BaseFragment) fragment).onBroadcastReceiver(action, bundle);
                }
            }
        }
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver != null) return;
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (EventBus.ACTION_BASE_BROADCAST.equals(intent.getAction())) {
                    String childAction = intent.getStringExtra("action");
                    onBroadcastReceiver(childAction, intent.getBundleExtra("bundle"));
                }
            }
        };
        IntentFilter filter = new IntentFilter(EventBus.ACTION_BASE_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    @Override
    public final WaitDialog showWaitDialog() {
        if (mWaitDialog == null) {
            mWaitDialog = new WaitDialog(this);
        }
        if (!mWaitDialog.isShowing() && !isFinishing()) {
            mWaitDialog.show();
        }
        return mWaitDialog;
    }

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
        mBaseViewHelper.setLoadingText(TextUtils.isEmpty(text) ? null : text);
        attachHelperView();
    }

    /**
     * 清除嵌入式进度条
     */
    @Override
    public final void hideProgress() {
        detachHelperView();
    }

    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        mBaseViewHelper.showNoNetView(getString(R.string.no_net_tips), listener);
        attachHelperView();
    }

    /**
     * 清除没有网络的界面
     */
    @Override
    public final void hideNoNetView() {
        detachHelperView();
    }

    /**
     * 显示空数据的界面
     */
    @Override
    public final void showEmptyView(String text, View.OnClickListener listener) {
        mBaseViewHelper.showEmptyText(text, listener);
        attachHelperView();
    }

    @Override
    public final void hideEmptyView() {
        detachHelperView();
    }

    /**
     * 设置空页面或者无网页面要附加的Parent Layout，不设置是整个父布局。
     */
    @Override
    public void setNestedParentLayout(ViewGroup parent) {
        mNestedParentLayout = parent;
    }

    private void attachHelperView() {
        View view = mBaseViewHelper.getView();
        if (view.getParent() != null) ((ViewGroup) view.getParent()).removeView(view);
        view.setClickable(true);

        if (mNestedParentLayout != null) {
            mNestedParentLayout.addView(view,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.main_title);
            mBaseRootView.addView(view, params);
        }
    }

    private void detachHelperView() {
        View view = mBaseViewHelper.getView();
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
}
