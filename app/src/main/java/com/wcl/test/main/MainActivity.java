package com.wcl.test.main;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.ActivityMainBinding;
import com.wcl.test.helper.ShowFragmentHelper;
import com.wcl.test.widget.ToastUtils;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final String CLASS_NAME = "MainActivity";
    private final int TIME_LONG = 3 * 1000;
    private long mLastTime = 0;
    public static final int TAB_FIRST = 0;
    public static final int TAB_SECOND = 1;
    public static final int TAB_THIRD = 2;
    public static final int TAB_FOURTH = 3;
    private static final int[] TAB_BOTTOM_ID_ARRAY = {R.id.tab_first, R.id.tab_second, R.id.tab_third, R.id.tab_fourth};
    private static final int[] TAB_BOTTOM_ICON_ARRAY = {R.drawable.main_first_icon_selector, R.drawable.main_second_icon_selector,
            R.drawable.main_third_icon_selector, R.drawable.main_fourth_icon_selector};
    private static final int[] TAB_BOTTOM_NAME_ARRAY = {R.string.host_first_tab, R.string.host_second_tab,
            R.string.host_third_tab, R.string.host_fourth_tab};
    private final Class[] FRAGMENTS = {MainFirstFragment.class, MainSecondFragment.class, MainThirdFragment.class, MainFourthFragment.class};
    private ShowFragmentHelper mFragHelper;
    private ActivityMainBinding mViewBinding;

    private MainFirstFragment mMainFirstFragment;
    private MainSecondFragment mMainSecondFragment;
    private MainThirdFragment mMainThirdFragment;
    private MainFourthFragment mMainFourthFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());

        //由于Manifest的style v27中设置了使用缺口屏，所以这里恢复为默认不使用缺口屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            getWindow().setAttributes(layoutParams);
        }


        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentLayout(R.layout.activity_main);
        getTitleHelper().hideTitleBar();
        mFragHelper = new ShowFragmentHelper(getSupportFragmentManager(), FRAGMENTS);

        initView();
        showTab(TAB_FIRST);
    }

    private void initView() {
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            mViewBinding.bottomTab.findViewById(TAB_BOTTOM_ID_ARRAY[index]).setOnClickListener(this);
            ImageView imageView =  findViewById(TAB_BOTTOM_ID_ARRAY[index]).findViewById(R.id.image_view);
            TextView textView = findViewById(TAB_BOTTOM_ID_ARRAY[index]).findViewById(R.id.text_view);

            imageView.setImageResource(TAB_BOTTOM_ICON_ARRAY[index]);
            textView.setText(TAB_BOTTOM_NAME_ARRAY[index]);
        }
    }

    /**
     * 用来让某个tab显示
     */
    public void showTab(int selectedIndex) {
        BaseFragment baseFragment = mFragHelper.showTabFragment(R.id.fragment_base_id, selectedIndex);
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            if (selectedIndex == index) {
                findViewById(TAB_BOTTOM_ID_ARRAY[index]).setSelected(true);
            } else {
                findViewById(TAB_BOTTOM_ID_ARRAY[index]).setSelected(false);
            }
        }

        if (selectedIndex == TAB_FIRST) {
            mMainFirstFragment = (MainFirstFragment) baseFragment;
        } else if (selectedIndex == TAB_SECOND) {
            mMainSecondFragment = (MainSecondFragment) baseFragment;
        } else if (selectedIndex == TAB_THIRD) {
            mMainThirdFragment = (MainThirdFragment) baseFragment;
        } else if (selectedIndex == TAB_FOURTH) {
            mMainFourthFragment = (MainFourthFragment) baseFragment;
        }
    }


    public int getSelectedTab() {
        return mFragHelper.mSelectedTab;
    }

    //解决fragment回收后重建空指针的问题
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        long exitTime = System.currentTimeMillis();
        if (exitTime - mLastTime < TIME_LONG) {
            super.onBackPressed();
        } else {
            ToastUtils.show(getString(R.string.quit_alert));
            mLastTime = exitTime;
        }
    }

    @Override
    public void onBroadcastReceiver(String myAction, Bundle bundle) {
        super.onBroadcastReceiver(myAction, bundle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_first:
                showTab(TAB_FIRST);
                break;
            case R.id.tab_second:
                showTab(TAB_SECOND);
                break;
            case R.id.tab_third:
                showTab(TAB_THIRD);
                break;
            case R.id.tab_fourth:
                showTab(TAB_FOURTH);
                break;
        }
    }
}
