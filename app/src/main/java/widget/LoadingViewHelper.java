package widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class LoadingViewHelper {
    public static final int VIEW_EMPTY = 1;
    public static final int VIEW_NO_NET = 2;
    private Context mContext;
    private View mLoadView;
    private View mShadowView;

    public LoadingViewHelper(Context context) {
        mContext = context;
        mLoadView = View.inflate(mContext, R.layout.loading, null);
    }

    /**
     * 得到显示加载中的View
     */
    public View getLoadingView() {
        return mLoadView;
    }

    /**
     * 设置加载中的View 的文字
     */
    public void setLoadingText(String text) {
        LinearLayout loadLinear = mLoadView.findViewById(R.id.load_linear);
        LinearLayout emptyLinear = mLoadView.findViewById(R.id.empty_linear);
        loadLinear.setVisibility(View.VISIBLE);
        emptyLinear.setVisibility(View.GONE);

        TextView textView = mLoadView.findViewById(R.id.text);
        textView.setText(text);
    }

    /**
     * 设置空页面或者无网页面
     */
    public void showEmptyText(int type, String text, View.OnClickListener listener) {
        LinearLayout loadLinear = mLoadView.findViewById(R.id.load_linear);
        LinearLayout emptyLinear = mLoadView.findViewById(R.id.empty_linear);
        ImageView emptyIcon = mLoadView.findViewById(R.id.empty_icon);
        loadLinear.setVisibility(View.GONE);
        emptyLinear.setVisibility(View.VISIBLE);

        Button button = mLoadView.findViewById(R.id.button);
        TextView textView = mLoadView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        if (listener != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(listener);
        } else {
            button.setVisibility(View.GONE);
        }

        if (type == VIEW_EMPTY) {
            button.setText("点击刷新");
            emptyIcon.setImageResource(R.drawable.empty_icon);
        } else if (type == VIEW_NO_NET) {
            button.setText("点击重试");
            emptyIcon.setImageResource(R.drawable.net_error_icon);
        }
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
