package cheerly.mybaseproject.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;


public class BaseViewHelper {
    private Context mContext;
    private View mView;
    private LinearLayout mLoadingLinear;
    private LinearLayout mEmptyLinear;
    private LinearLayout mNoNetLinear;
    private View.OnClickListener mTempClickListener;
    public static final int TOP_STYLE = 1;
    public static final int CENTER_STYLE = 2;
    private int mStyle = CENTER_STYLE;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mTempClickListener != null) {
                mTempClickListener.onClick(v);
            }
        }
    };

    public BaseViewHelper(Context context) {
        mContext = context;
    }

    public void setShowStyle(int style) {
        mStyle = style;
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
        mLoadingLinear = mView.findViewById(R.id.loading_linear);
        TextView textView = mView.findViewById(R.id.text);
        textView.setText(text);

        if (mStyle == TOP_STYLE) {
            mLoadingLinear.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mLoadingLinear.setPadding(0, BaseUtils.dip2px(30f), 0, 0);
        } else {
            mLoadingLinear.setGravity(Gravity.CENTER);
            mLoadingLinear.setPadding(0, 0, 0, 0);
        }
    }

    public void clearLoadingView() {
        mTempClickListener = null;
    }

    /**
     * 设置空页面
     */
    public void showEmptyText(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        mEmptyLinear = mView.findViewById(R.id.empty_linear);
        TextView textView = mEmptyLinear.findViewById(R.id.empty_text);

        if (mStyle == TOP_STYLE) {
            mEmptyLinear.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mEmptyLinear.setPadding(0, BaseUtils.dip2px(30f), 0, 0);
        } else {
            mEmptyLinear.setGravity(Gravity.CENTER);
            mEmptyLinear.setPadding(0, 0, 0, 0);
        }

        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        if (listener != null) {
            mTempClickListener = listener;
        }
    }

    /**
     * 设置无网页面
     */
    public void showNoNetView(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_no_net_layout, null);
        mNoNetLinear = mView.findViewById(R.id.no_net_linear);

        if (mStyle == TOP_STYLE) {
            mNoNetLinear.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mNoNetLinear.setPadding(0, BaseUtils.dip2px(30f), 0, 0);
        } else {
            mNoNetLinear.setGravity(Gravity.CENTER);
            mNoNetLinear.setPadding(0, 0, 0, 0);
        }

        TextView textView = mView.findViewById(R.id.net_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        if (listener != null) {
            mTempClickListener = listener;
        }
    }


}
