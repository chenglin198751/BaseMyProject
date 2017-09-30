package base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import cheerly.mybaseproject.R;
import widget.LoadingViewHelper;

/**
 * Created by chenglin on 2017-9-14.
 */

public class BaseFragment extends Fragment {
    private LoadingViewHelper mLoadingViewHelper = null;
    private RelativeLayout mContentView;

    public Activity getContext() {
        return getActivity();
    }

    public void onMyBroadcastReceive(String action, Bundle bundle) {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContentView = (RelativeLayout) view.findViewById(R.id.base_frag_id);
    }

    /**
     * 显示嵌入式进度条
     */
    public void showProgress(String text) {
        hideProgress();
        addLoadView();
        if (text != null) {
            mLoadingViewHelper.setText(text);
        }
    }

    /**
     * 清除contentView里面的加载进度
     */
    public void hideProgress() {
        if (mLoadingViewHelper != null && getView() != null) {
            mContentView.removeView(mLoadingViewHelper.getLoadingView());
            mLoadingViewHelper = null;
        }
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView() {
        addLoadView();
        mLoadingViewHelper.showNoNetView();
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView(View.OnClickListener listener, String text) {
        addLoadView();
        mLoadingViewHelper.showNoNetView(listener, text);
    }

    /**
     * 显示空数据的界面
     */
    public void showEmptyView(String text) {
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
