package com.wcl.test.helper;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wcl.test.base.BaseFragment;

/**
 * Helper class to manage showing and switching tab fragments.
 * Created by chenglin on 2017-11-28.
 */
public class ShowFragmentHelper {
    private final BaseFragment[] mFragArray;
    private final FragmentManager mFragmentManager;
    private final Class<? extends BaseFragment>[] mFragmentClasses;
    private int mSelectedTab = -1;

    /**
     * Constructor.
     *
     * @param fragmentManager Use getSupportFragmentManager() in Activity,
     *                        getChildFragmentManager() in Fragment.
     * @param fragmentClasses Array of fragment classes to manage.
     */
    public ShowFragmentHelper(FragmentManager fragmentManager, Class<? extends BaseFragment>[] fragmentClasses) {
        if (fragmentClasses == null || fragmentClasses.length == 0) {
            throw new IllegalArgumentException("Fragment classes must not be null or empty");
        }

        this.mFragmentManager = fragmentManager;
        this.mFragmentClasses = fragmentClasses;
        this.mFragArray = new BaseFragment[fragmentClasses.length];

        // Attempt to restore fragments from FragmentManager after configuration change
        for (int i = 0; i < fragmentClasses.length; i++) {
            Fragment existing = fragmentManager.findFragmentByTag(fragmentClasses[i].getName());
            if (existing instanceof BaseFragment) {
                mFragArray[i] = (BaseFragment) existing;
            }
        }
    }

    public BaseFragment showTabFragment(@IdRes int viewId, int index) {
        return showTabFragment(viewId, null, index);
    }

    public BaseFragment showTabFragment(@IdRes int viewId, Bundle args, int index) {
        validateIndex(index);

        FragmentTransaction ft = mFragmentManager.beginTransaction();

        BaseFragment fragment = mFragArray[index];
        if (fragment == null) {
            fragment = createFragment(index, args);
            mFragArray[index] = fragment;
            addFragment(viewId, fragment, ft);
        } else if (args != null && fragment.getArguments() == null) {
            fragment.setArguments(args);
        }

        showOnlyFragment(ft, index);
        ft.commitAllowingStateLoss();

        mSelectedTab = index;
        return fragment;
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= mFragmentClasses.length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for fragment array");
        }
    }

    private BaseFragment createFragment(int index, Bundle args) {
        try {
            BaseFragment fragment = mFragmentClasses[index].newInstance();
            if (args != null) {
                fragment.setArguments(args);
            }
            return fragment;
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate fragment: " + mFragmentClasses[index].getName(), e);
        }
    }

    private void addFragment(@IdRes int viewId, Fragment fragment, FragmentTransaction ft) {
        if (!fragment.isAdded()) {
            ft.add(viewId, fragment, fragment.getClass().getName());
        }
    }

    private void showOnlyFragment(FragmentTransaction ft, int showIndex) {
        for (int i = 0; i < mFragArray.length; i++) {
            Fragment frag = mFragArray[i];
            if (frag != null) {
                if (i == showIndex) {
                    ft.show(frag);
                } else {
                    ft.hide(frag);
                }
            }
        }
    }

    public int getSelectedTab() {
        return mSelectedTab;
    }
}