package com.wcl.test.base;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wcl.test.R;

class BaseViewHelper {
    private final Context mContext;
    private final ViewGroup mParentView;

    private LinearLayout mLoadingView;
    private LinearLayout mEmptyView;
    private LinearLayout mNoNetView;

    private int mShowGravity = Gravity.CENTER;
    private ObjectAnimator rotateAnimator;

    public BaseViewHelper(Context context, ViewGroup parentView) {
        this.mContext = context;
        this.mParentView = parentView;
    }

    public LinearLayout getView() {
        if (mLoadingView != null) {
            return mLoadingView;
        } else if (mEmptyView != null) {
            return mEmptyView;
        } else if (mNoNetView != null) {
            return mNoNetView;
        }
        return null;
    }

    /**
     * 设置状态页（Loading / Empty / NoNet 等覆盖层）的显示位置。
     * 该位置会同时作用于所有状态视图，而不是只影响 Loading。
     * 例如可以控制状态页是居中显示，还是贴近顶部显示。
     * 传值：同Gravity.TOP等
     */
    public void setStateViewGravity(int position) {
        mShowGravity = position;
        applyPosition(getView());
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

        // 旋转动画
        ImageView image = mLoadingView.findViewById(R.id.loading_icon);
        rotateAnimator = ObjectAnimator.ofFloat(image, View.ROTATION, 0f, 360f);
        rotateAnimator.setDuration(800);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.start();

        showOnly(mLoadingView);
    }

    public void showEmptyText(String text, View.OnClickListener listener) {
        ensureEmptyView(listener);

        TextView textView = mEmptyView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        showOnly(mEmptyView);
    }

    public void showNoNetView(String text, View.OnClickListener listener) {
        ensureNoNetView(listener);

        TextView textView = mNoNetView.findViewById(R.id.net_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        showOnly(mNoNetView);
    }

    public void destroy() {
        if (rotateAnimator != null) {
            rotateAnimator.cancel();
            rotateAnimator = null;
        }
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

    private void ensureEmptyView(View.OnClickListener listener) {
        if (mEmptyView != null) return;
        mEmptyView = (LinearLayout) View.inflate(mContext, R.layout.base_empty_layout, null);
        mEmptyView.setOnClickListener(listener);
        applyPosition(mEmptyView);
    }

    private void ensureNoNetView(View.OnClickListener listener) {
        if (mNoNetView != null) return;
        mNoNetView = (LinearLayout) View.inflate(mContext, R.layout.base_no_net_layout, null);
        mNoNetView.setOnClickListener(listener);
        applyPosition(mNoNetView);
    }

    private void showOnly(View target) {
        mParentView.addView(target, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
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
