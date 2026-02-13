package com.wcl.test.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.wcl.test.R;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.widget.WaitDialog;

/**
 * BaseFragment 基类
 */
public abstract class BaseFragment extends Fragment implements IBaseView, OnEventBusListener {
    protected static final Gson gson = AppConstants.gson;

    private BaseViewHelper mBaseViewHelper;
    private RelativeLayout mContentView;
    private ViewGroup nestedParentView;

    @NonNull
    public BaseActivity getContext() {
        if (!(getActivity() instanceof BaseActivity)) {
            throw new IllegalStateException("Activity must be a BaseActivity");
        }
        return (BaseActivity) getActivity();
    }

    @CallSuper
    @Override
    public void onEvent(String eventKey, Object data) {
        for (Fragment childFragment : getChildFragmentManager().getFragments()) {
            if (childFragment instanceof BaseFragment && childFragment.isAdded()) {
                ((BaseFragment) childFragment).onEvent(eventKey, data);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @CallSuper
    @Deprecated
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = (RelativeLayout) inflater.inflate(R.layout.base_fragment_layout, container, false);
        mBaseViewHelper = new BaseViewHelper(getContext(), container);
        return mContentView;
    }

    @CallSuper
    @Deprecated
    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContentLayout() > 0) {
            View content = LayoutInflater.from(getContext()).inflate(getContentLayout(), mContentView, false);
            mContentView.addView(content, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else if (getContentView() != null) {
            mContentView.addView(getContentView(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        onViewCreated(savedInstanceState, view);
    }

    protected abstract int getContentLayout();

    protected View getContentView() {
        return null;
    }

    /**
     * 所有业务逻辑在这里处理
     */
    protected abstract void onViewCreated(Bundle savedInstanceState, View view);


    @Override
    public final WaitDialog showWaitDialog() {
        return getContext().showWaitDialog();
    }

    @Override
    public final void dismissWaitDialog() {
        getContext().dismissWaitDialog();
    }

    /**
     * 显示嵌入式进度条，带文案
     */
    @Override
    public final void showLoading(String text) {
        detachHelperView();
        mBaseViewHelper.setLoadingText(TextUtils.isEmpty(text) ? null : text);
        attachHelperView();
    }

    /**
     * 显示嵌入式进度条，默认文案
     */
    @Override
    public final void showLoading() {
        showLoading("");
    }

    /**
     * 清除嵌入式进度条
     */
    @Override
    public void hideLoading() {
        detachHelperView();
    }

    /**
     * 显示无网页面
     */
    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        hideNoNetView();
        mBaseViewHelper.showNoNetView(getString(R.string.no_net_tips), listener);
        attachHelperView();
    }

    /**
     * 清除无网页面
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
        hideEmptyView();
        mBaseViewHelper.showEmptyText(text, listener);
        attachHelperView();
    }

    /**
     * 清楚空数据的界面
     */
    @Override
    public final void hideEmptyView() {
        detachHelperView();
    }

    /**
     * 设置空页面或者无网页面要附加的Parent Layout，若不设置则是整个父布局。
     */
    @Override
    public void setNestedParentView(ViewGroup parent) {
        nestedParentView = parent;
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

    /**
     * 当使用ShowFragmentHelper使用BaseFragment时，显示时触发此事件
     */
    public void onSelected(int index) {

    }

    private void attachHelperView() {
        if (getView() == null) return;

        View helperView = mBaseViewHelper.getView();
        if (helperView.getParent() != null)
            ((ViewGroup) helperView.getParent()).removeView(helperView);
        helperView.setClickable(true);

        if (nestedParentView != null) {
            nestedParentView.addView(helperView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            mContentView.addView(helperView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void detachHelperView() {
        View view = mBaseViewHelper.getView();
        if (getView() == null || view == null || view.getParent() == null) {
            return;
        }

        mBaseViewHelper.destroy();
        ((ViewGroup) view.getParent()).removeView(view);
    }
}
