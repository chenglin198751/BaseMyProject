package helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import base.BaseActivity;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-5-23.
 */

public class MainTitleHelper {
    private BaseActivity mBaseActivity;
    private View mTitleView;
    private RelativeLayout mMenuView;
    private TextView mTitleTv;

    public MainTitleHelper(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
        mTitleView = mBaseActivity.findViewById(R.id.main_title);
        mMenuView = mTitleView.findViewById(R.id.menu_linear);
        mTitleTv = mTitleView.findViewById(R.id.title_text);

        mTitleView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaseActivity.finish();
            }
        });
    }

    /**
     * 设置标题
     */
    public void setTitle(String titleStr) {
        mTitleTv.setText(titleStr);
    }

    public String getTitle() {
        return mTitleTv.getText().toString();
    }

    /**
     * 设置标题
     */
    public void setTitle(int title_resId) {
        mTitleTv.setText(title_resId);
    }

    /**
     * 隐藏标题栏
     */
    public void hideTitleBar() {
        mTitleView.setVisibility(View.GONE);
    }

    /**
     * 设置左上角返回图片的点击监听事件
     */
    public void setReturnListener(View.OnClickListener listener) {
        mTitleView.findViewById(R.id.back_btn).setOnClickListener(listener);
    }


    /**
     * 为右侧的菜单增加指定的view
     */
    public void addMenuView(View view) {
        mMenuView.setVisibility(View.VISIBLE);
        mMenuView.removeAllViews();
        mMenuView.addView(view, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * 为右侧的第一个菜单图片设置点击监听
     */
    public void addMenuIcon(int resId, View.OnClickListener listener) {
        TextView menuTv = mTitleView.findViewById(R.id.menu_text);
        ImageView menuIcon = mTitleView.findViewById(R.id.menu_icon);

        mMenuView.setVisibility(View.VISIBLE);
        menuTv.setVisibility(View.GONE);
        menuIcon.setVisibility(View.VISIBLE);

        menuIcon.setImageResource(resId);
        menuIcon.setOnClickListener(listener);
    }

    /**
     * 为右侧的第一个菜单文字设置点击监听
     */
    public void addMenuText(String text, View.OnClickListener listener) {
        TextView menuTv = mTitleView.findViewById(R.id.menu_text);
        ImageView menuIcon = mTitleView.findViewById(R.id.menu_icon);

        mMenuView.setVisibility(View.VISIBLE);
        menuTv.setVisibility(View.VISIBLE);
        menuIcon.setVisibility(View.GONE);

        menuTv.setText(text);
        menuTv.setOnClickListener(listener);
    }

    /**
     * 设置右侧的菜单隐藏还是显示
     */
    public void setMenuVisible(int visible) {
        mMenuView.setVisibility(visible);
    }
}
