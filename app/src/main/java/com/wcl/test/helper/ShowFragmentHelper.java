package com.wcl.test.helper;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wcl.test.base.BaseFragment;

/**
 * 用于管理显示和切换 tab Fragment 的辅助类。
 * Created by chenglin on 2017-11-28.
 */
public class ShowFragmentHelper {
    private final BaseFragment[] mFragArray;
    private final FragmentManager mFragmentManager;
    private final Class<? extends BaseFragment>[] mFragmentClasses;
    private int mSelectedIndex = -1;

    /**
     * 构造函数
     *
     * @param fragmentManager 在 Activity 里请使用 getSupportFragmentManager()，
     *                        在 Fragment 里请使用 getChildFragmentManager()。
     * @param fragmentClasses 需要管理的 Fragment 类数组。
     */
    public ShowFragmentHelper(FragmentManager fragmentManager, Class<? extends BaseFragment>[] fragmentClasses) {
        if (fragmentClasses == null || fragmentClasses.length == 0) {
            throw new IllegalArgumentException("Fragment classes must not be null or empty");
        }

        this.mFragmentManager = fragmentManager;
        this.mFragmentClasses = fragmentClasses;
        this.mFragArray = new BaseFragment[fragmentClasses.length];

        // 尝试从 FragmentManager 恢复已存在的 fragment 实例（可能被销毁过）
        for (int i = 0; i < fragmentClasses.length; i++) {
            Fragment existing = fragmentManager.findFragmentByTag(fragmentClasses[i].getName());
            if (existing instanceof BaseFragment) {
                mFragArray[i] = (BaseFragment) existing;
            }
        }
    }

    public BaseFragment showFragment(@IdRes int viewId, int index) {
        return showFragment(viewId, null, index);
    }

    public BaseFragment showFragment(@IdRes int viewId, Bundle args, int index) {
        validateIndex(index);

        FragmentTransaction ft = mFragmentManager.beginTransaction();

        BaseFragment fragment = mFragArray[index];
        if (fragment == null) {
            fragment = createFragment(index, args);
            mFragArray[index] = fragment;
            addFragment(viewId, fragment, ft);
        }

        showOnlyFragment(ft, index);
        ft.commitAllowingStateLoss();

        if (mSelectedIndex != index) {
            fragment.onSelected();
        }

        mSelectedIndex = index;
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

    public int getShowingIndex() {
        return mSelectedIndex;
    }
}