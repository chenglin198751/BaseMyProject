package main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import base.BaseActivity;
import cheerly.mybaseproject.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int TAB_FIRST = 0;
    public static final int TAB_SECOND = 1;
    public static final int TAB_THIRD = 2;
    public static final int TAB_FOURTH = 3;

    private static final int[] TAB_BOTTOM_ID_ARRAY = {R.id.tab_first, R.id.tab_second, R.id.tab_third, R.id.tab_fourth};
    private static final int[] TAB_BOTTOM_ICON_ARRAY = {R.drawable.main_first_icon_selector, R.drawable.main_second_icon_selector,
            R.drawable.main_third_icon_selector, R.drawable.main_fourth_icon_selector};
    private static final int[] TAB_BOTTOM_NAME_ARRAY = {R.string.host_first_tab, R.string.host_second_tab,
            R.string.host_third_tab, R.string.host_fourth_tab};
    private static final int TAB_SIZE = 4;
    private Fragment[] mFragArray = new Fragment[TAB_SIZE];

    private int mCurrentTab;
    private MainFirstFragment mMainFirstFragment;
    private MainSecondFragment mMainSecondFragment;
    private MainThirdFragment mMainThirdFragment;
    private MainFourthFragment mMainFourthFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        getTitleHelper().hideTitle();

        showTabFragment(TAB_FIRST);
        initView();
    }

    private void initView() {
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            findViewById(TAB_BOTTOM_ID_ARRAY[index]).setOnClickListener(this);
            ImageView imageView = (ImageView) findViewById(TAB_BOTTOM_ID_ARRAY[index]).findViewById(R.id.image_view);
            TextView textView = (TextView) findViewById(TAB_BOTTOM_ID_ARRAY[index]).findViewById(R.id.text_view);

            imageView.setImageResource(TAB_BOTTOM_ICON_ARRAY[index]);
            textView.setText(TAB_BOTTOM_NAME_ARRAY[index]);
        }
    }

    /**
     * 用来让某个tab显示
     */
    public void showTab(int currentIndex) {
        showTabFragment(currentIndex);
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            if (currentIndex == index) {
                findViewById(TAB_BOTTOM_ID_ARRAY[index]).setSelected(true);
            } else {
                findViewById(TAB_BOTTOM_ID_ARRAY[index]).setSelected(false);
            }
        }
    }

    private void showTabFragment(int index) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (index == TAB_FIRST) {
            if (mMainFirstFragment == null) {
                mMainFirstFragment = new MainFirstFragment();
                mFragArray[index] = mMainFirstFragment;
            }
            addFragment(ft, mMainFirstFragment, index);
        } else if (index == TAB_SECOND) {
            if (mMainSecondFragment == null) {
                mMainSecondFragment = new MainSecondFragment();
                mFragArray[index] = mMainSecondFragment;
            }
            addFragment(ft, mMainSecondFragment, index);
        } else if (index == TAB_THIRD) {
            if (mMainThirdFragment == null) {
                mMainThirdFragment = new MainThirdFragment();
                mFragArray[index] = mMainThirdFragment;
            }
            addFragment(ft, mMainThirdFragment, index);
        } else if (index == TAB_FOURTH) {
            if (mMainFourthFragment == null) {
                mMainFourthFragment = new MainFourthFragment();
                mFragArray[index] = mMainFourthFragment;
            }
            addFragment(ft, mMainFourthFragment, index);
        }

        showFragment(ft, index);
        ft.commitAllowingStateLoss();
        mCurrentTab = index;
    }

    public int getCurrentTabIndex() {
        return mCurrentTab;
    }

    private void showFragment(FragmentTransaction ft, final int index) {
        for (int i = 0; i < mFragArray.length; i++) {
            if (i == index) {
                if (mFragArray[i] != null) {
                    ft.show(mFragArray[i]);
                }
            } else {
                if (mFragArray[i] != null) {
                    ft.hide(mFragArray[i]);
                }
            }
        }
    }

    private void addFragment(FragmentTransaction ft, Fragment fragment, int fragmentTag) {
        if (!fragment.isAdded() && fragment.getTag() == null) {
            ft.add(R.id.fragment_base_id, fragment, fragmentTag + "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMyBroadcastReceive(String myAction, Bundle bundle) {
        super.onMyBroadcastReceive(myAction, bundle);
        if (myAction.equals("1")) {
            Log.v("tag_2", bundle.getString("key"));
        } else if (myAction.equals("2")) {
            Log.v("tag_2", bundle.getString("key"));
        }
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
