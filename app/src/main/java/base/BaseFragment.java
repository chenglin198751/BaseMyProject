package base;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import cheerly.mybaseproject.R;
import httpwork.HttpBuilder;
import httpwork.HttpCallback;
import httpwork.HttpUtils;
import utils.Constants;
import utils.MyAction;
import widget.BaseViewHelper;
import widget.WaitDialog;

/**
 * Created by chenglin on 2017-9-14.
 */

public abstract class BaseFragment extends Fragment implements ImplBaseView {
    protected final static Gson gson = Constants.gson;
    private BaseViewHelper mBaseViewHelper = null;
    private RelativeLayout mContentView;
    private boolean isAddedView = false;

    public BaseActivity getContext() {
        return (BaseActivity) getActivity();
    }

    /**
     * post 请求，建议用这个
     */
    public void post(String url, Map<String, Object> map, final HttpCallback httpCallback) {
        HttpBuilder builder = new HttpBuilder();
        builder.setCache(false);
        HttpUtils.postWithHeader(getContext(), url, null, map, builder, httpCallback);
    }

    public final void sendMyBroadcast(String action, Bundle bundle) {
        MyAction.sendMyBroadcast(action, bundle);
    }

    @CallSuper
    public void onMyBroadcastReceiver(String action, Bundle bundle) {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null && fragments.size() > 0) {
            for (Fragment childFragment : fragments) {
                if (childFragment instanceof BaseFragment) {
                    ((BaseFragment) childFragment).onMyBroadcastReceiver(action, bundle);
                }
            }
        }
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
        mBaseViewHelper = new BaseViewHelper(getContext());
        if (getContentLayout() > 0) {
            mContentView.addView(View.inflate(getContext(), getContentLayout(), null), new RelativeLayout.LayoutParams(-1, -1));
        }
        onViewCreated(savedInstanceState, view);
    }

    protected abstract int getContentLayout();

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
    public final WaitDialog showWaitDialog(String text) {
        return getContext().showWaitDialog(text);
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
        if (text != null) {
            mBaseViewHelper.setLoadingText(text);
        } else {
            mBaseViewHelper.setLoadingText(getString(R.string.data_loading));
        }
        addLoadView();
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
        if (isAddedView) {
            mContentView.removeView(mBaseViewHelper.getView());
            isAddedView = false;
        }
    }

    /**
     * 显示没有网络的界面
     */
    @Override
    public final void showNoNetView(View.OnClickListener listener) {
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

    private void addLoadView() {
        if (getView() == null) {
            return;
        }
        if (!isAddedView) {
            mBaseViewHelper.getView().setClickable(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
            mContentView.addView(mBaseViewHelper.getView(), params);
            isAddedView = true;
        }
    }
}
