package base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import cheerly.mybaseproject.R;
import utils.Constants;
import widget.LoadingViewHelper;

/**
 * Created by chenglin on 2017-9-14.
 */

public abstract class BaseFragment extends Fragment {
    protected final static Gson gson = Constants.gson;
    private LoadingViewHelper mLoadingViewHelper = null;
    private RelativeLayout mContentView;
    private int mFragLayoutId = 0;

    public BaseActivity getContext() {
        return (BaseActivity) getActivity();
    }

    public final void sendMyBroadcast(String action, Bundle bundle) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).sendMyBroadcast(action, bundle);
        }
    }

    public void onMyBroadcastReceiver(String action, Bundle bundle) {
    }

    @Deprecated
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_fragment_layout, container, false);
    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContentView = (RelativeLayout) view.findViewById(R.id.base_frag_id);
        if (mFragLayoutId > 0) {
            mContentView.addView(View.inflate(getContext(), mFragLayoutId, null), new RelativeLayout.LayoutParams(-1, -1));
        }
        onViewCreated(savedInstanceState, view);
    }

    protected void setContentLayout(@LayoutRes int layoutId) {
        mFragLayoutId = layoutId;
    }

    /**
     * 所有的业务逻辑在这里写
     */
    protected abstract void onViewCreated(Bundle savedInstanceState, View view);

    /**
     * 显示嵌入式进度条
     */
    public final void showProgress(String text) {
        hideProgress();
        addLoadView();
        if (text != null) {
            mLoadingViewHelper.setText(text);
        }
    }

    /**
     * 清除contentView里面的加载进度
     */
    public final void hideProgress() {
        if (mLoadingViewHelper != null && getView() != null) {
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
        if (mLoadingViewHelper == null && getView() != null) {
            mLoadingViewHelper = new LoadingViewHelper(getActivity());
            mLoadingViewHelper.getLoadingView().setClickable(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            mContentView.addView(mLoadingViewHelper.getLoadingView(), params);
        }
    }
}
