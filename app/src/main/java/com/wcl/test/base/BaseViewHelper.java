package com.wcl.test.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wcl.test.R;

class BaseViewHelper {
    private final Context mContext;

    private LinearLayout mLoadingView;
    private LinearLayout mEmptyView;
    private LinearLayout mNoNetView;
    private LinearLayout mCurrentView;

    private int mShowGravity = Gravity.CENTER;

    public BaseViewHelper(Context context) {
        this.mContext = context;
    }

    public LinearLayout getView() {
        return mCurrentView;
    }

    /**
     * 设置状态页（Loading / Empty / NoNet 等覆盖层）的显示位置。
     * 该位置会同时作用于所有状态视图，而不是只影响 Loading。
     * 例如可以控制状态页是居中显示，还是贴近顶部显示。
     * 传值：同Gravity.TOP等
     */
    public void setStateViewGravity(int position) {
        mShowGravity = position;
        applyPosition(mLoadingView);
        applyPosition(mEmptyView);
        applyPosition(mNoNetView);
    }

    public void setLoadingText(String text) {
        ensureLoadingView();

        TextView textView = mLoadingView.findViewById(R.id.loading_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }

        showOnly(mLoadingView);
    }

    public void showEmptyText(String text, View.OnClickListener listener) {
        ensureEmptyView();
        mEmptyView.setOnClickListener(listener);

        TextView textView = mEmptyView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        showOnly(mEmptyView);
    }

    public void showNoNetView(String text, View.OnClickListener listener) {
        ensureNoNetView();
        mNoNetView.setOnClickListener(listener);

        TextView textView = mNoNetView.findViewById(R.id.net_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        showOnly(mNoNetView);
    }

    public void startLoadingAnimation() {
        // 保留接口，空实现
    }

    public void hideLoading() {
        hideView(mLoadingView);
    }

    public void hideEmpty() {
        hideView(mEmptyView);
    }

    public void hideNoNet() {
        hideView(mNoNetView);
    }

    public void destroy() {
        removeFromParent(mLoadingView);
        removeFromParent(mEmptyView);
        removeFromParent(mNoNetView);
        if (mEmptyView != null) {
            mEmptyView.setOnClickListener(null);
        }
        if (mNoNetView != null) {
            mNoNetView.setOnClickListener(null);
        }
        mCurrentView = null;
        mLoadingView = null;
        mEmptyView = null;
        mNoNetView = null;
    }

    // ==================== 内部实现 ====================
    private void ensureLoadingView() {
        if (mLoadingView != null) return;
        mLoadingView = (LinearLayout) View.inflate(mContext, R.layout.base_loading_layout, null);
        applyPosition(mLoadingView);
    }

    private void ensureEmptyView() {
        if (mEmptyView != null) return;
        mEmptyView = (LinearLayout) View.inflate(mContext, R.layout.base_empty_layout, null);
        applyPosition(mEmptyView);
    }

    private void ensureNoNetView() {
        if (mNoNetView != null) return;
        mNoNetView = (LinearLayout) View.inflate(mContext, R.layout.base_no_net_layout, null);
        applyPosition(mNoNetView);
    }

    private void showOnly(LinearLayout target) {
        removeFromParent(mLoadingView);
        removeFromParent(mEmptyView);
        removeFromParent(mNoNetView);
        mCurrentView = target;
    }

    private void hideView(LinearLayout target) {
        if (mCurrentView != target) return;
        removeFromParent(target);
        mCurrentView = null;
    }

    private void removeFromParent(View view) {
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private void applyPosition(LinearLayout view) {
        if (view == null) return;

        int gravity;
        if (mShowGravity == Gravity.TOP) {
            gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        } else {
            gravity = Gravity.CENTER;
        }
        view.setGravity(gravity);
    }
}
