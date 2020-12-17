package cheerly.mybaseproject.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class BaseViewHelper {
    private Context mContext;
    private View mView;
    private View.OnClickListener mTempClickListener;

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

    public void clearLoadingView() {
        mTempClickListener = null;
    }

    /**
     * 设置空页面
     */
    public void showEmptyText(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);
        TextView textView = mView.findViewById(R.id.empty_text);

        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(R.string.empty_tips);
        }

        if (listener != null) {
            mTempClickListener = listener;
        }

        emptyIcon.setImageResource(R.drawable.empty_icon);
    }

    /**
     * 设置无网页面
     */
    public void showNoNetView(String text, View.OnClickListener listener) {
        mView = View.inflate(mContext, R.layout.base_empty_layout, null);
        ImageView emptyIcon = mView.findViewById(R.id.empty_icon);

        TextView textView = mView.findViewById(R.id.empty_text);
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }

        if (listener != null) {
            mTempClickListener = listener;
        }
        emptyIcon.setImageResource(R.drawable.net_error_icon);
    }


}
