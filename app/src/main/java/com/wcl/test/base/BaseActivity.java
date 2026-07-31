package com.wcl.test.base;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.wcl.test.R;
import com.wcl.test.databinding.BaseActivityLayoutBinding;
import com.wcl.test.helper.MainTitleHelper;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.utils.AppUtils;
import com.wcl.test.widget.WaitDialog;

import java.util.List;

/**
 * Activity 基类
 *
 * @author weiChengLin 2013-06-20
 */
public abstract class BaseActivity extends AppCompatActivity implements IBaseView, OnEventBusListener {
    public static final String MAIN_ACTIVITY_NAME = "MainActivity";
    protected static final Gson gson = AppConstants.gson;
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    private BaseActivityLayoutBinding mBinding;
    private MainTitleHelper mTitleHelper;
    private BaseViewHelper mBaseViewHelper;
    private WaitDialog mWaitDialog;
    private RelativeLayout mBaseRootView;
    private ViewGroup mContentView;
    private ViewGroup mNestedParentLayout;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        installFontScaleFactory();
        applyGrayScale();
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        if (onKeepSingleActivity()) {
            EventBus.post(EventAction.System.ACTION_KEEP_SINGLE_ACTIVITY, getClass().getName());
        }
        EventBus.instance().register(this);

        mBinding = BaseActivityLayoutBinding.inflate(getLayoutInflater());
        mBaseRootView = mBinding.getRoot();
        setContentView(mBaseRootView);

        mTitleHelper = new MainTitleHelper(this);
        mBaseViewHelper = new BaseViewHelper(this);

        if (getTitle() != null) {
            mTitleHelper.setTitle(getTitle().toString());
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBaseRootView, (v, insets) -> {
            AppUtils.statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            AppUtils.navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            displayInCutoutMode(onDisplayInCutoutMode());
            return insets;
        });
    }

    // 所有彩色变成黑白色
    private void applyGrayScale() {
        if (AppConstants.Toggle.isGrayscale) {
            Paint paint = new Paint();
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
            getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        }
    }

    // 设置当前页面是否显示在缺口屏内
    public void displayInCutoutMode(boolean isDisplayInCutout) {
        if (isDisplayInCutout) {
            mBaseRootView.setPadding(0, 0, 0, 0);
        } else {
            mBaseRootView.setPadding(0, AppUtils.statusBarHeight, 0, AppUtils.navBarHeight);
        }
    }

    public BaseActivity getContext() {
        return this;
    }

    /**
     * @see #setContentLayout(ViewGroup)
     * @deprecated 请使用 {@link #setContentLayout(ViewGroup)} 代替
     */
    @Deprecated
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    /**
     * @see #setContentLayout(int)
     * @deprecated 请使用 {@link #setContentLayout(int)} 代替
     */
    @Deprecated
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    /**
     * 设置Activity的内容布局，取代系统的 setContentView() 方法
     */
    public final void setContentLayout(@LayoutRes int layoutResID) {
        ViewGroup layoutView = (ViewGroup) View.inflate(this, layoutResID, null);
        setContentLayout(layoutView);
    }

    /**
     * 设置Activity的内容布局，取代系统的 setContentView() 方法
     */
    public final void setContentLayout(final ViewGroup layoutView) {
        if (mContentView != null && mContentView.getParent() != null) {
            mBaseRootView.removeView(mContentView);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
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
                return;
            }
        } else if (EventAction.System.ACTION_KEEP_ONLY_MAIN_ACTIVITY.equals(eventKey)) {
            if (!getClass().getSimpleName().equals(MAIN_ACTIVITY_NAME)) {
                finish();
                return;
            }
        }

        // 通知Activity里面所有的fragment接收广播
        dispatchEventToFragments(eventKey, data);
    }

    /**
     * 将事件分发给当前 Activity 中所有已附加的 BaseFragment
     */
    private void dispatchEventToFragments(String eventKey, Object data) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof BaseFragment && !AppUtils.isFragmentDestroyed(f)) {
                ((BaseFragment) f).onEvent(eventKey, data);
            }
        }
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.instance().unregister(this);

        if (mWaitDialog != null) {
            mWaitDialog.dismiss();
            mWaitDialog = null;
        }
        if (mBaseViewHelper != null) {
            mBaseViewHelper.destroy();
            mBaseViewHelper = null;
        }
    }

    @Override
    public final WaitDialog showWaitDialog() {
        if (mWaitDialog == null) {
            mWaitDialog = new WaitDialog(this);
        }
        if (!mWaitDialog.isShowing() && !isFinishing() && !isDestroyed()) {
            mWaitDialog.show();
        }
        return mWaitDialog;
    }

    @Override
    public final void dismissWaitDialog() {
        if (mWaitDialog != null && mWaitDialog.isShowing() && !isFinishing() && !isDestroyed()) {
            mWaitDialog.dismiss();
        }
    }

    /**
     * 显示嵌入式进度条，带文案
     */
    @Override
    public final void showLoading(String text) {
        mBaseViewHelper.setLoadingText(TextUtils.isEmpty(text) ? null : text);
        attachHelperView();
    }

    /**
     * 显示嵌入式进度条，默认文案
     */
    @Override
    public final void showLoading() {
        showLoading(null);
    }

    /**
     * 清除嵌入式进度条
     */
    @Override
    public void hideLoading() {
        mBaseViewHelper.hideLoading();
    }

    /**
     * 显示无网页面
     */
    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        mBaseViewHelper.showNoNetView(getString(R.string.no_net_tips), listener);
        attachHelperView();
    }

    /**
     * 清除无网页面
     */
    @Override
    public final void hideNoNetView() {
        mBaseViewHelper.hideNoNet();
    }

    /**
     * 显示空数据的界面
     */
    @Override
    public final void showEmptyView(String text, View.OnClickListener listener) {
        mBaseViewHelper.showEmptyText(text, listener);
        attachHelperView();
    }

    /**
     * 清楚空数据的界面
     */
    @Override
    public final void hideEmptyView() {
        mBaseViewHelper.hideEmpty();
    }

    /**
     * 设置空页面或者无网页面要附加的Parent Layout，若不设置则是整个父布局。
     */
    @Override
    public void setNestedParentView(ViewGroup parent) {
        mNestedParentLayout = parent;
    }

    /**
     * 设置状态页（Loading / Empty / NoNet 等覆盖层）的显示位置。
     * 该位置会同时作用于所有状态视图，而不是只影响 Loading。
     * 例如可以控制状态页是居中显示，还是贴近顶部显示。
     * 传值：同Gravity.TOP等
     */
    @Override
    public void setStateViewGravity(int position) {
        mBaseViewHelper.setStateViewGravity(position);
    }

    private void attachHelperView() {
        View view = mBaseViewHelper.getView();
        if (view == null) return;
        if (view.getParent() != null) ((ViewGroup) view.getParent()).removeView(view);
        view.setClickable(true);

        if (mNestedParentLayout != null) {
            mNestedParentLayout.addView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.main_title);
            mBaseRootView.addView(view, params);
        }
        if (view == mBaseViewHelper.getView()) {
            mBaseViewHelper.startLoadingAnimation();
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
