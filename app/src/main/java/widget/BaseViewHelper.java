package widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class BaseViewHelper {
    private Context mContext;
    private View mView;
    private View mShadowView;

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
        mView = View.inflate(mContext, R.layout.base_loading_layout, null);
        TextView textView = mView.findViewById(R.id.text);
        textView.setText(text);
    }

    /**
     * 设置空页面
     */
    public void showEmptyText(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);
        Button button = mView.findViewById(R.id.button);
        TextView textView = mView.findViewById(R.id.empty_text);

        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        if (listener != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(listener);
        } else {
            button.setVisibility(View.GONE);
        }

        button.setText("点击刷新");
        emptyIcon.setImageResource(R.drawable.empty_icon);
    }

    /**
     * 设置无网页面
     */
    public void showNoNetView(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);

        Button button = mView.findViewById(R.id.button);
        TextView textView = mView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        if (listener != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(listener);
        } else {
            button.setVisibility(View.GONE);
        }

        button.setText("点击重试");
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
