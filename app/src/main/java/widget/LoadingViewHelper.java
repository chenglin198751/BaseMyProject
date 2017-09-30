package widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class LoadingViewHelper {
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

    public void setText(String text) {
        TextView textView = (TextView) mLoadView.findViewById(R.id.text);
        textView.setText(text);
    }

    public void setText(int resId) {
        TextView textView = (TextView) mLoadView.findViewById(R.id.text);
        textView.setText(resId);
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView() {
        NoNetView();
        setBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                intent.setComponent(component);
                intent.setAction(Intent.ACTION_VIEW);
                mContext.startActivity(intent);
            }
        }, "网络设置");
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView(View.OnClickListener listener, String text) {
        NoNetView();
        setBtnClick(listener, text);
    }

    private void NoNetView() {
        LinearLayout load_linear = (LinearLayout) mLoadView.findViewById(R.id.load_linear);
        LinearLayout empty_linear = (LinearLayout) mLoadView.findViewById(R.id.empty_linear);
        load_linear.setVisibility(View.GONE);
        empty_linear.setVisibility(View.VISIBLE);
    }

    private void setBtnClick(View.OnClickListener listener, String text) {
        Button button = (Button) mLoadView.findViewById(R.id.button);
        button.setText(text);
        button.setOnClickListener(listener);
    }

    /**
     * 显示空数据的界面
     */
    public void showEmptyView() {
        LinearLayout load_linear = (LinearLayout) mLoadView.findViewById(R.id.load_linear);
        LinearLayout empty_linear = (LinearLayout) mLoadView.findViewById(R.id.empty_linear);
        load_linear.setVisibility(View.GONE);
        empty_linear.setVisibility(View.VISIBLE);

        TextView empty_text = (TextView) mLoadView.findViewById(R.id.empty_text);
        Button button = (Button) mLoadView.findViewById(R.id.button);
        empty_text.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        empty_text.setText("没有数据哦");
    }

    /**
     * 设置空数据界面时的文字提示
     */
    public void setEmptyText(String text) {
        TextView empty_text = (TextView) mLoadView.findViewById(R.id.empty_text);
        empty_text.setText(text);
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
