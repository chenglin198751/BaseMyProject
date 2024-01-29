package com.wcl.test.base;

import android.content.Context;
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
import com.wcl.test.widget.BaseViewHelper;
import com.wcl.test.widget.WaitDialog;

import java.util.List;

/**
 * Created by chenglin on 2017-9-14.
 */

public abstract class BaseFragment extends Fragment implements ImplBaseView, OnBroadcastListener {
    protected final static Gson gson = AppConstants.gson;
    private BaseViewHelper mBaseViewHelper = null;
    private RelativeLayout mContentView;
    private ViewGroup mNestedParentLayout;

    @NonNull
    public BaseActivity getContext() {
        if (getActivity() == null) {
            throw new NullPointerException("getActivity() is NullPointerException");
        }
        return (BaseActivity) getActivity();
    }

    @CallSuper
    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments.size() > 0) {
            for (Fragment childFragment : fragments) {
                if (childFragment instanceof BaseFragment) {
                    ((BaseFragment) childFragment).onBroadcastReceiver(action, bundle);
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseViewHelper = new BaseViewHelper(getContext());
    }

    @CallSuper
    @Deprecated
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = (RelativeLayout) inflater.inflate(R.layout.base_fragment_layout, container, false);
        return mContentView;
    }

    @CallSuper
    @Deprecated
    @Override
    public final void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContentLayout() > 0) {
            mContentView.addView(View.inflate(getContext(), getContentLayout(), null), new RelativeLayout.LayoutParams(-1, -1));
        } else if (getContentView() != null) {
            mContentView.addView(getContentView(), new RelativeLayout.LayoutParams(-1, -1));
        }

        onViewCreated(savedInstanceState, view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected abstract int getContentLayout();

    protected View getContentView() {
        return null;
    }

    /**
     * 所有的业务逻辑在这里写
     */
    protected abstract void onViewCreated(Bundle savedInstanceState, View view);

    /**
     * 响应Activity 的 onBackPressed() 方法，需要手动实现。
     *
     * @return true 是可以返回，否则不能返回
     */
    public boolean onBackPressed() {
        return true;
    }

    /**
     * 显示等待的对话框
     */
    @Override
    public final WaitDialog showWaitDialog() {
        return getContext().showWaitDialog();
    }

    @Override
    public void dismissWaitDialog() {
        getContext().dismissWaitDialog();
    }

    /**
     * 显示嵌入式进度条
     */
    @Override
    public final void showProgress(String text) {
        clearLoadingView();
        if (!TextUtils.isEmpty(text)) {
            mBaseViewHelper.setLoadingText(text);
        } else {
            mBaseViewHelper.setLoadingText(null);
        }

        addLoadView();
    }

    public void setShowStyle(int style) {
        mBaseViewHelper.setShowStyle(style);
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
        if (getView() == null) {
            return;
        }

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
     * 清除空数据的界面
     */
    @Override
    public final void hideEmptyView() {
        clearLoadingView();
    }

    /**
     * 设置空页面或者无网页面要附加的Parent Layout，不设置是整个父布局。
     */
    @Override
    public void setNestedParentLayout(ViewGroup parent) {
        mNestedParentLayout = parent;
    }

    private void addLoadView() {
        if (getView() == null) {
            return;
        }
        mBaseViewHelper.getView().setClickable(true);

        if (mNestedParentLayout != null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
            mNestedParentLayout.addView(mBaseViewHelper.getView(), params);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            mContentView.addView(mBaseViewHelper.getView(), params);
        }
    }
}
