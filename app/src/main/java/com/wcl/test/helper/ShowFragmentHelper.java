package com.wcl.test.helper;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wcl.test.base.BaseFragment;

/**
 * Created by chenglin on 2017-11-28.
 */

public class ShowFragmentHelper {
    private BaseFragment[] mFragArray;
    private FragmentManager mFragmentManager;
    private Class[] FRAGMENTS;
    public int mSelectedTab;

    /**
     * fragmentManager 必须特别注意：
     * 如果是在Activity里需要传入的是getSupportFragmentManager();
     * 如果是在嵌套的fragment里面需要调用getChildFragmentManager();
     * 否则会出问题。
     */
    public ShowFragmentHelper(FragmentManager fragmentManager, Class[] fragments) {
        if (fragments == null || fragments.length <= 0) {
            throw new IllegalArgumentException("FRAGMENTS.length must >0");
        }

        this.mFragmentManager = fragmentManager;
        this.FRAGMENTS = fragments;
        mFragArray = new BaseFragment[fragments.length];
    }

    private ShowFragmentHelper() {
    }

    public BaseFragment showTabFragment(@IdRes int viewId, int index) {
        return showTabFragment(viewId, null, index);
    }

    public BaseFragment showTabFragment(@IdRes int viewId, Bundle bundle, int index) {
        if (FRAGMENTS == null || FRAGMENTS.length <= 0) {
            throw new IllegalArgumentException("FRAGMENTS.length must >0");
        }
        if (index < 0 || index >= FRAGMENTS.length) {
            throw new IllegalArgumentException("index must >0 And index must < FRAGMENTS.length");
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (mFragArray[index] == null) {
            try {
                //非常重要，勿改：此处这么写是为了解决当Activity被回收后，重新onCreate()时会创建新的fragment的问题。
                //具体分析见：https://mp.weixin.qq.com/s/mwTtxk4YfYWCG4m6n_ropw  --by chenglin 2018年7月2日
                BaseFragment tempFragment = (BaseFragment) mFragmentManager.findFragmentByTag(FRAGMENTS[index].getName());
                if (tempFragment != null) {
                    mFragArray[index] = tempFragment;
                } else {
                    mFragArray[index] = (BaseFragment) FRAGMENTS[index].newInstance();
                }

                if (bundle != null) {
                    mFragArray[index].setArguments(bundle);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        addFragment(viewId, mFragArray[index], ft);
        showFragment(ft, index);
        ft.commitAllowingStateLoss();
        mSelectedTab = index;
        return mFragArray[index];
    }

    private void showFragment(FragmentTransaction ft, final int index) {
        for (int i = 0; i < mFragArray.length; i++) {
            if (mFragArray[i] != null) {
                if (i == index) {
                    ft.show(mFragArray[i]);
                } else {
                    ft.hide(mFragArray[i]);
                }
            }
        }
    }

    private void addFragment(@IdRes int viewId, Fragment fragment, FragmentTransaction ft) {
        if (!fragment.isAdded() && fragment.getTag() == null) {
            ft.add(viewId, fragment, fragment.getClass().getName());
        }
    }
}
