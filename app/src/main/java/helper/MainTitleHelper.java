package helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import base.BaseActivity;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-5-23.
 */

public class MainTitleHelper {
    private BaseActivity mBaseActivity;
    private View mTitleView;

    public MainTitleHelper(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
        mTitleView = mBaseActivity.findViewById(R.id.main_title);

        View image = mTitleView.findViewById(R.id.back_icon);
        image.setOnClickListener(new View.OnClickListener() {
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
        menLinear.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
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
     * 设置右侧的菜单隐藏还是显示
     */
    public void setMenuVisible(int visible) {
        LinearLayout menLinear = (LinearLayout) mTitleView.findViewById(R.id.menu_linear);
        if (visible == View.VISIBLE) {
            menLinear.setVisibility(visible);
        } else {
            menLinear.setVisibility(View.INVISIBLE);
        }
    }
}
