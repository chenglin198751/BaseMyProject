package cheerly.mybaseproject.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class BaseViewHelper {
    private Context mContext;
    private View mView;
    private View mShadowView;
    private ObjectAnimator mValueAnimator;
    private View mLoadImg;
    private View.OnClickListener mTempClickListener;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startLoadingAnimation();
            if (mTempClickListener != null) {
                mTempClickListener.onClick(v);
            }
        }
    };

    public BaseViewHelper(Context context) {
        mContext = context;
    }

    /**
     * 得到显示中的View
     */
    public View getView() {
        return mView;
    }

    /**
     * 设置加载中的View 的文字
     */
    public void setLoadingText(String text) {
        if (mView == null){
            mView = View.inflate(mContext, R.layout.base_loading_layout, null);
        }
        mLoadImg = mView.findViewById(R.id.image);
        TextView textView = mView.findViewById(R.id.text);
        textView.setText(text);
    }

    public void startLoadingAnimation() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            return;
        }

        mValueAnimator = ObjectAnimator.ofFloat(mLoadImg, "Rotation", 0f, 360f);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mValueAnimator.setDuration(2 * 1000);
        mValueAnimator.start();
    }

    public void stopLoadingAnimation() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }

    public void clearLoadingView() {
        stopLoadingAnimation();
        mTempClickListener = null;
    }

    /**
     * 设置空页面
     */
    public void showEmptyText(String text, View.OnClickListener listener) {
        if (mView == null){
            mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        }
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);
        View btnRefresh = mView.findViewById(R.id.btn_refresh);
        TextView textView = mView.findViewById(R.id.empty_text);
        mLoadImg = btnRefresh;

        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        if (listener != null) {
            mTempClickListener = listener;
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setOnClickListener(mClickListener);
        } else {
            btnRefresh.setVisibility(View.GONE);
        }

        emptyIcon.setImageResource(R.drawable.empty_icon);
    }

    /**
     * 设置无网页面
     */
    public void showNoNetView(String text, View.OnClickListener listener) {
        if (mView == null){
            mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        }
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);

        View btnRefresh = mView.findViewById(R.id.btn_refresh);
        mLoadImg = btnRefresh;
        TextView textView = mView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        if (listener != null) {
            mTempClickListener = listener;
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setOnClickListener(mClickListener);
        } else {
            btnRefresh.setVisibility(View.GONE);
        }

        emptyIcon.setImageResource(R.drawable.net_error_icon);
    }

    /**
     * 增加加载框的阴影效果
     */
    public void addShadowView(RelativeLayout attachView) {
        removeShadowView(attachView);
        if (mShadowView == null) {
            RelativeLayout.LayoutParams shadowParams = new RelativeLayout.LayoutParams(-1, -1);
            mShadowView = new View(mContext);
            mShadowView.setBackgroundColor(attachView.getResources().getColor(R.color.shadow_bg));
            attachView.addView(mShadowView, shadowParams);
        }
    }

    /**
     * 移除加载框的阴影效果
     */
    public void removeShadowView(RelativeLayout attachView) {
        if (mShadowView != null) {
            attachView.removeView(mShadowView);
            mShadowView = null;
        }
    }
}
