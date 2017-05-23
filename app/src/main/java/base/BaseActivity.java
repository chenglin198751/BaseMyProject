package base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import utils.MyUtils;
import cheerly.mybaseproject.R;
import utils.Value;
import widget.LoadingView;

/**
 * Activity的基类
 *
 * @author weiChengLin 2013-06-20
 */
public class BaseActivity extends Activity {
    private View mTitleView;
    private boolean isColseActivity = true;
    public boolean isNet = true;
    private LoadingView mLoadingView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBoradcastReceiver();

        if (!MyUtils.isNet(this)) {
            isNet = false;
        }

        setContentView(R.layout.base_activity_layout);
        mTitleView = findViewById(R.id.main_title);

        View image = mTitleView.findViewById(R.id.back_icon);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isColseActivity) {
                    finish();
                }
            }
        });
    }

    /**
     * 设置Activity的内容布局，取代setContentView（）方法
     */
    public void setView(int layoutResID) {
        LinearLayout content_linear = (LinearLayout) this.findViewById(R.id.content_view);
        content_linear.addView(View.inflate(this, layoutResID, null), new LinearLayout.LayoutParams(-1, -1));
    }

    /**
     * 设置标题
     */
    public void setTitle(String titleStr) {
        TextView titleView = (TextView) mTitleView.findViewById(R.id.title_text);
        titleView.setText(titleStr);
    }

    public String getTitleStr() {
        TextView titleView = (TextView) mTitleView.findViewById(R.id.title_text);
        return titleView.getText().toString();
    }

    /**
     * 设置标题
     */
    public void setTitle(int title_resId) {
        TextView titleView = (TextView) mTitleView.findViewById(R.id.title_text);
        titleView.setText(title_resId);
    }

    /**
     * 隐藏标题
     */
    public void hideTitle() {
        mTitleView.setVisibility(View.GONE);
    }

    /**
     * 设置是否点击左上角的返回按钮关闭当前界面，默认是关闭
     */
    public void setDisCloseActivity() {
        this.isColseActivity = false;
    }

    /**
     * 设置左上角返回图片的点击监听事件
     */
    public void setRuturnListener(View.OnClickListener listener) {
        View image = mTitleView.findViewById(R.id.back_icon);
        image.setOnClickListener(listener);
    }

    /**
     * 设置左上角的图片
     */
    public void setRuturnImage(int res) {
        ImageView image = (ImageView) mTitleView.findViewById(R.id.back_icon);
        image.setImageResource(res);
    }

    /**
     * 为右侧的菜单增加指定的view
     */
    public void addMenuView(View view) {
        View menu_divider = mTitleView.findViewById(R.id.menu_divider);
        menu_divider.setVisibility(View.VISIBLE);
        LinearLayout menLinear = (LinearLayout) mTitleView.findViewById(R.id.menu_linear);
        menLinear.setVisibility(View.VISIBLE);
        menLinear.removeAllViews();
        menLinear.addView(view, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }

    /**
     * 为右侧的第一个菜单图片设置点击监听
     */
    public void addFirstMenu(int resId, View.OnClickListener listener) {
        addFirstMenu(resId);
        View view = mTitleView.findViewById(R.id.menu_Linear_1);
        view.setOnClickListener(listener);
    }

    /**
     * 为右侧的第一个菜单图片设置资源图片
     */
    public void addFirstMenu(int resId) {
        View menu_divider = mTitleView.findViewById(R.id.menu_divider);
        menu_divider.setVisibility(View.VISIBLE);
        LinearLayout menLinear = (LinearLayout) mTitleView.findViewById(R.id.menu_linear);
        menLinear.setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) mTitleView.findViewById(R.id.menu_icon_1);
        imageView.setImageResource(resId);
    }

    /**
     * 为右侧的第二个菜单图片设置点击监听
     */
    public void addSecondMenu(int resId, View.OnClickListener listener) {
        addSecondMenu(resId);
        View view = mTitleView.findViewById(R.id.menu_Linear_2);
        view.setOnClickListener(listener);
    }

    /**
     * 为右侧的第二个菜单图片设置点击监听
     */
    public void addSecondMenu(int resId) {
        View menu_divider = mTitleView.findViewById(R.id.menu_divider);
        menu_divider.setVisibility(View.VISIBLE);
        LinearLayout menLinear = (LinearLayout) mTitleView.findViewById(R.id.menu_linear);
        menLinear.setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) mTitleView.findViewById(R.id.menu_icon_1);
        imageView.setImageResource(resId);
    }

    /**
     * 设置右侧的菜单隐藏还是现实
     */
    public void setMenuVisible(int visible) {
        LinearLayout menLinear = (LinearLayout) mTitleView.findViewById(R.id.menu_linear);
        if (visible == View.VISIBLE) {
            menLinear.setVisibility(visible);
        } else {
            menLinear.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    /**
     * 显示嵌入式进度条
     */
    public void showProgress() {
        removeProgress();
        addLoadView();
    }

    /**
     * 设置进度要要显示的文字提示
     */
    public void setProgressText(String text) {
        mLoadingView.setText(text);
    }

    /**
     * 清除contentView里面的加载进度
     */
    public void removeProgress() {
        if (mLoadingView != null) {
            LinearLayout contentView = (LinearLayout) this.findViewById(R.id.content_view);
            contentView.removeView(mLoadingView.getLoadingView());
            mLoadingView = null;
        }
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView() {
        addLoadView();
        mLoadingView.showNoNetView();
    }

    /**
     * 显示没有网络的界面
     */
    public void showNoNetView(View.OnClickListener listener, String text) {
        addLoadView();
        mLoadingView.showNoNetView(listener, text);
    }

    /**
     * 显示空数据的界面
     */
    public void showEmptyView(String text) {
        addLoadView();
        mLoadingView.showEmptyView();
        if (!TextUtils.isEmpty(text)) {
            mLoadingView.setEmptyText(text);
        }
    }

    private void addLoadView() {
        if (mLoadingView == null) {
            mLoadingView = new LoadingView(this);
            LinearLayout contentView = (LinearLayout) this.findViewById(R.id.content_view);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
            contentView.addView(mLoadingView.getLoadingView(), 0, params);
        }
    }

    private void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Value.ACTION_FINISH_ACTIVITY);
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Value.ACTION_FINISH_ACTIVITY)) {
                boolean isClose = intent.getBooleanExtra("isClose", false);
                if (isClose) {
                    BaseActivity.this.finish();
                }
            }
        }
    };
}
