package com.wcl.test.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wcl.test.R;
import com.wcl.test.utils.AppUtils;

class BaseViewHelper {
    private final Context mContext;
    private View mRootView;

    private View mLoadingView;
    private View mEmptyView;
    private View mNoNetView;

    private int mShowGravity = Gravity.CENTER;

    private View.OnClickListener mTempClickListener;

    private final View.OnClickListener mInternalClickListener = v -> {
        if (mTempClickListener != null) {
            mTempClickListener.onClick(v);
        }
    };

    public BaseViewHelper(Context context) {
        this.mContext = context;
        initRootView();
    }

    private void initRootView() {
        mRootView = View.inflate(mContext, R.layout.base_loading_layout, null);
        mRootView.setOnClickListener(mInternalClickListener);
    }

    public View getView() {
        return mRootView;
    }

    /**
     * 设置状态页（Loading / Empty / NoNet 等覆盖层）的显示位置。
     * 该位置会同时作用于所有状态视图，而不是只影响 Loading。
     * 例如可以控制状态页是居中显示，还是贴近顶部显示。
     */
    public void setStateViewGravity(int position) {
        mShowGravity = position;
        applyPosition(mLoadingView);
        applyPosition(mEmptyView);
        applyPosition(mNoNetView);
    }

    public void setLoadingText(String text) {
        ensureLoadingView();

        TextView textView = mLoadingView.findViewById(R.id.text);
        if (text != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }

        showOnly(mLoadingView);
        mTempClickListener = null;
    }

    public void showEmptyText(String text, View.OnClickListener listener) {
        ensureEmptyView();

        TextView textView = mEmptyView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        mTempClickListener = listener;
        showOnly(mEmptyView);
    }

    public void showNoNetView(String text, View.OnClickListener listener) {
        ensureNoNetView();

        TextView textView = mNoNetView.findViewById(R.id.net_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        mTempClickListener = listener;
        showOnly(mNoNetView);
    }

    // ==================== 内部实现 ====================
    private void ensureLoadingView() {
        if (mLoadingView != null) return;
        mLoadingView = View.inflate(mContext, R.layout.base_loading_layout, null);
        applyPosition(mLoadingView);
    }

    private void ensureEmptyView() {
        if (mEmptyView != null) return;
        mEmptyView = View.inflate(mContext, R.layout.base_empty_layout, null);
        applyPosition(mEmptyView);
    }

    private void ensureNoNetView() {
        if (mNoNetView != null) return;
        mNoNetView = View.inflate(mContext, R.layout.base_no_net_layout, null);
        applyPosition(mNoNetView);
    }

    private void showOnly(View target) {
        LinearLayout container = getRootContainer();

        container.removeAllViews();
        container.addView(target, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
    }

    private LinearLayout getRootContainer() {
        return (LinearLayout) mRootView;
    }

    private void applyPosition(View view) {
        if (view == null) return;

        int gravity;
        int topPadding;

        if (mShowGravity == Gravity.TOP) {
            gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            topPadding = AppUtils.dp2px(30f);
        } else {
            gravity = Gravity.CENTER;
            topPadding = 0;
        }

        int rootId;
        if (view == mLoadingView) {
            rootId = R.id.loading_linear;
        } else if (view == mEmptyView) {
            rootId = R.id.empty_linear;
        } else {
            rootId = R.id.no_net_linear;
        }
        LinearLayout root = view.findViewById(rootId);

        if (root != null) {
            root.setGravity(gravity);
            root.setPadding(0, topPadding, 0, 0);
        }
    }
}
