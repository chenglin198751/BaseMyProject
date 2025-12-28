package com.wcl.test.base;

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
public abstract class BaseActivity extends AppCompatActivity implements ImplBaseView, OnEventBusListener {
    public static final String CLASS_NAME = "MainActivity";
    protected static final Gson gson = AppConstants.gson;

    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private View mContentView;
    private ViewGroup mNestedParentLayout;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        installFontScaleFactory();
        super.onCreate(savedInstanceState);
        applyGrayScaleIfNeeded();

        if (onKeepSingleActivity()) {
            EventBus.post(EventAction.System.ACTION_KEEP_SINGLE_ACTIVITY, getClass().getName());
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
            setPaddingStatusBar();
        }
    }

    // 显示状态栏：默认View显示在缺口屏内
    public void setPaddingStatusBar() {
        ViewCompat.setOnApplyWindowInsetsListener(mBaseRootView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(0, statusBarHeight, 0, navBarHeight);
            return insets;
        });
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
        return false;
    }

    @CallSuper
    @Override
    public void onEvent(String eventKey, Object data) {
        if (EventAction.System.ACTION_KEEP_SINGLE_ACTIVITY.equals(eventKey) && onKeepSingleActivity()) {
            if (getClass().getName().equals(data)) {
                finish();
            }
        } else if (EventAction.System.ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY.equals(eventKey)) {
            if (!getClass().getSimpleName().equals(CLASS_NAME)) {
                finish();
            }
        } else {
            //通知Activity里面所有的fragment接收广播
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseFragment && fragment.isAdded()) {
                    ((BaseFragment) fragment).onEvent(eventKey, data);
                }
            }
        }
    }

    private void registerBroadcastReceiver() {
        EventBus.instance().register(this);
    }

    private void unregisterBroadcastReceiver() {
        EventBus.instance().unregister(this);
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

    /**
     * 使用自定义的LayoutInflater.Factory2，
     * 解决比如字号在小米手机上总是小一号的问题
     */
    private void installFontScaleFactory() {
//        if (AppBaseUtils.isXiaomiDevice()) {
//            LayoutInflater inflater = getLayoutInflater();
//            if (inflater.getFactory2() != null && inflater.getFactory2() instanceof FontScaleFactory) {
//                return;
//            }
//            LayoutInflater.Factory2 existingFactory2 = inflater.getFactory2();
//            float deltaPx = AppBaseUtils.dip2px(1f);
//            FontScaleFactory fsFactory = new FontScaleFactory(inflater, existingFactory2, getDelegate(), deltaPx);
//            try {
//                inflater.setFactory2(fsFactory);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}
