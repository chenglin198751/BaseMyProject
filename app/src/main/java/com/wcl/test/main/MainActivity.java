package com.wcl.test.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.ActivityMainBinding;
import com.wcl.test.helper.ShowFragmentHelper;
import com.wcl.test.widget.ToastUtils;
import com.wcl.test.widget.banner.AutoGalleryBannerView2;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int TAB_FIRST = 0;
    public static final int TAB_SECOND = 1;
    public static final int TAB_THIRD = 2;
    public static final int TAB_FOURTH = 3;

    private static final int[] TAB_BOTTOM_ID_ARRAY = {
            R.id.tab_first, R.id.tab_second, R.id.tab_third, R.id.tab_fourth
    };
    private static final int[] TAB_BOTTOM_ICON_ARRAY = {
            R.drawable.main_first_icon_selector, R.drawable.main_second_icon_selector,
            R.drawable.main_third_icon_selector, R.drawable.main_fourth_icon_selector
    };
    private static final int[] TAB_BOTTOM_NAME_ARRAY = {
            R.string.host_first_tab, R.string.host_second_tab,
            R.string.host_third_tab, R.string.host_fourth_tab
    };
    @SuppressWarnings("unchecked")
    private final Class<? extends BaseFragment>[] FRAGMENTS = new Class[]{
            MainFirstFragment.class, MainSecondFragment.class, MainThirdFragment.class, MainFourthFragment.class
    };

    private ShowFragmentHelper mFragHelper;
    private ActivityMainBinding mViewBinding;
    private long mLastBackPressTime = 0;
    private static final int BACK_EXIT_INTERVAL = 3000;

    @Override
    protected boolean onDisplayInCutoutMode() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentLayout(mViewBinding.getRoot());
        getTitleHelper().hideTitleBar();

        mFragHelper = new ShowFragmentHelper(getSupportFragmentManager(), FRAGMENTS);
        initBottomTabs();
        showTab(TAB_FIRST);
        setupBackPressHandler();
    }

    private void initBottomTabs() {
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            View tabView = mViewBinding.bottomTab.findViewById(TAB_BOTTOM_ID_ARRAY[index]);
            tabView.setOnClickListener(this);

            ImageView icon = tabView.findViewById(R.id.image_view);
            TextView label = tabView.findViewById(R.id.text_view);

            icon.setImageResource(TAB_BOTTOM_ICON_ARRAY[index]);
            label.setText(TAB_BOTTOM_NAME_ARRAY[index]);
        }
    }

    public void showTab(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= FRAGMENTS.length) return;

        mFragHelper.showTabFragment(R.id.fragment_base_id, selectedIndex);

        for (int i = 0; i < TAB_BOTTOM_ID_ARRAY.length; i++) {
            View tabView = mViewBinding.bottomTab.findViewById(TAB_BOTTOM_ID_ARRAY[i]);
            tabView.setSelected(i == selectedIndex);
        }
    }

    public int getSelectedTab() {
        return mFragHelper.getSelectedTab();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - mLastBackPressTime < BACK_EXIT_INTERVAL) {
                    finish();
                } else {
                    ToastUtils.show(getString(R.string.quit_alert));
                    mLastBackPressTime = currentTime;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < TAB_BOTTOM_ID_ARRAY.length; i++) {
            if (v.getId() == TAB_BOTTOM_ID_ARRAY[i]) {
                showTab(i);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBroadcastReceiver(String myAction, Bundle bundle) {
        super.onBroadcastReceiver(myAction, bundle);
    }

}
