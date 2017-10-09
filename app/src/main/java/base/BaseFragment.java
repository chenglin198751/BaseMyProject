package base;

import android.app.Activity;
import android.os.Bundle;
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

public class BaseFragment extends Fragment {
    protected final static Gson gson = Constants.gson;
    private LoadingViewHelper mLoadingViewHelper = null;
    private RelativeLayout mContentView;
    private int mFragLayoutId = 0;

    public Activity getContext() {
        return getActivity();
    }

    public void onMyBroadcastReceive(String action, Bundle bundle) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContentView = (RelativeLayout) view.findViewById(R.id.base_frag_id);
        if (mFragLayoutId > 0) {
            mContentView.addView(View.inflate(getContext(), mFragLayoutId, null), new RelativeLayout.LayoutParams(-1, -1));
        }
    }

    public void setContentLayout(int layoutId) {
        mFragLayoutId = layoutId;
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
