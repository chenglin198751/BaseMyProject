package helper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import base.BaseFragment;
import cheerly.mybaseproject.R;

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

    public BaseFragment showTabFragment(int index) {
        return showTabFragment(null, index);
    }

    public BaseFragment showTabFragment(Bundle bundle, int index) {
        if (FRAGMENTS == null || FRAGMENTS.length <= 0) {
            throw new IllegalArgumentException("FRAGMENTS.length must >0");
        }
        if (index < 0 || index >= FRAGMENTS.length) {
            throw new IllegalArgumentException("index must >0 And index must < FRAGMENTS.length");
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (mFragArray[index] == null) {
            try {
                mFragArray[index] = (BaseFragment) FRAGMENTS[index].newInstance();
                if (bundle != null){
                    mFragArray[index].setArguments(bundle);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        addFragment(mFragArray[index], ft, index);
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

    private void addFragment(Fragment fragment, FragmentTransaction ft, int fragmentTag) {
        if (!fragment.isAdded() && fragment.getTag() == null) {
            ft.add(R.id.fragment_base_id, fragment, fragmentTag + "");
        }
    }
}
