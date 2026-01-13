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
public abstract class BaseFragment extends Fragment implements ImplBaseView, OnEventBusListener {
    protected static final Gson gson = AppConstants.gson;

    private BaseViewHelper baseViewHelper;
    private RelativeLayout rootLayout;
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
        baseViewHelper = new BaseViewHelper(getContext());
    }

    @CallSuper
    @Deprecated
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootLayout = (RelativeLayout) inflater.inflate(R.layout.base_fragment_layout, container, false);
        return rootLayout;
    }

    @CallSuper
    @Deprecated
    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContentLayout() > 0) {
            View content = LayoutInflater.from(getContext()).inflate(getContentLayout(), rootLayout, false);
            rootLayout.addView(content, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else if (getContentView() != null) {
            rootLayout.addView(getContentView(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

    /**
     * 响应 Activity 的 onBackPressed()
     *
     * @return true 可以返回，false 禁止返回
     */
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public final WaitDialog showWaitDialog() {
        return getContext().showWaitDialog();
    }

    @Override
    public final void dismissWaitDialog() {
        getContext().dismissWaitDialog();
    }

    @Override
    public final void showLoading(String text) {
        clearLoadingView();
        baseViewHelper.setLoadingText(TextUtils.isEmpty(text) ? null : text);
        attachHelperView();
    }

    public void setLoadingShowPosition(int position) {
        baseViewHelper.setStateViewGravity(position);
    }

    @Override
    public final void showLoading() {
        clearLoadingView();
    }

    @Override
    public final void showNoNetView(View.OnClickListener listener) {
        hideNoNetView();
        baseViewHelper.showNoNetView(getString(R.string.no_net_tips), listener);
        attachHelperView();
    }

    @Override
    public final void hideNoNetView() {
        clearLoadingView();
    }

    @Override
    public final void showEmptyView(String text, View.OnClickListener listener) {
        hideEmptyView();
        baseViewHelper.showEmptyText(text, listener);
        attachHelperView();
    }

    @Override
    public final void hideEmptyView() {
        clearLoadingView();
    }

    @Override
    public void setNestedParentView(ViewGroup parent) {
        nestedParentView = parent;
    }

    private void attachHelperView() {
        if (getView() == null) return;

        View helperView = baseViewHelper.getView();
        if (helperView.getParent() != null)
            ((ViewGroup) helperView.getParent()).removeView(helperView);
        helperView.setClickable(true);

        if (nestedParentView != null) {
            nestedParentView.addView(helperView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            rootLayout.addView(helperView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void clearLoadingView() {
        if (getView() == null || baseViewHelper.getView() == null) return;

        ViewGroup parent = (ViewGroup) baseViewHelper.getView().getParent();
        if (parent != null) parent.removeView(baseViewHelper.getView());
    }
}
