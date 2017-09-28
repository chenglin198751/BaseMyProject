package base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import cheerly.mybaseproject.R;
import widget.LoadingView;

/**
 * Created by chenglin on 2017-9-14.
 */

public class BaseFragment extends Fragment {
    private LoadingView mLoadingView = null;

    public Activity getContext() {
        return getActivity();
    }

    public void onMyBroadcastReceive(String action, Bundle bundle) {
    }

    /**
     * 显示嵌入式进度条
     */
    public void showProgress(String text) {
        removeProgress();
        addLoadView();
        if (text != null) {
            mLoadingView.setText(text);
        }
    }

    /**
     * 清除contentView里面的加载进度
     */
    public void removeProgress() {
        if (mLoadingView != null && getView() != null) {
            RelativeLayout contentView = (RelativeLayout) getView().findViewById(R.id.root_view);
            contentView.removeView(mLoadingView.getLoadingView());
            mLoadingView = null;
        }
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView() {
        addLoadView();
        mLoadingView.showNoNetView();
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView(View.OnClickListener listener, String text) {
        addLoadView();
        mLoadingView.showNoNetView(listener, text);
    }

    /**
     * 显示空数据的界面
     */
    public void showEmptyView(String text) {
        addLoadView();
        mLoadingView.showEmptyView();
        if (!TextUtils.isEmpty(text)) {
            mLoadingView.setEmptyText(text);
        }
    }

    private void addLoadView() {
        if (mLoadingView == null && getView() != null) {
            mLoadingView = new LoadingView(getActivity());
            mLoadingView.getLoadingView().setBackgroundColor(getResources().getColor(R.color.view_bg));
            mLoadingView.getLoadingView().setClickable(true);
            RelativeLayout contentView = (RelativeLayout) getView().findViewById(R.id.root_view);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            contentView.addView(mLoadingView.getLoadingView(), params);
        }
    }
}
