package helper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import base.BaseActivity;
import base.BaseFragment;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-11-28.
 */

public class ShowFragmentHelper {
    private BaseFragment[] mFragArray;
    private FragmentTransaction mFragmentTransaction;
    private BaseActivity mActivity;
    private Class[] FRAGMENTS;
    public int mSelectedTab;

    public ShowFragmentHelper(BaseActivity activity, Class[] fragments) {
        if (fragments == null || fragments.length <= 0) {
            throw new IllegalArgumentException("FRAGMENTS.length must >0");
        }

        this.mActivity = activity;
        this.FRAGMENTS = fragments;
        mFragArray = new BaseFragment[fragments.length];
    }

    private ShowFragmentHelper() {
    }

    public BaseFragment showTabFragment(int index) {
        if (FRAGMENTS == null || FRAGMENTS.length <= 0) {
            throw new IllegalArgumentException("FRAGMENTS.length must >0");
        }
        if (index < 0 || index >= FRAGMENTS.length) {
            throw new IllegalArgumentException("index must >0 And index must < FRAGMENTS.length");
        }

        mFragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (mFragArray[index] == null) {
            try {
                mFragArray[index] = (BaseFragment) FRAGMENTS[index].newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        addFragment(mFragArray[index], index);
        showFragment(index);
        mFragmentTransaction.commitAllowingStateLoss();
        mSelectedTab = index;
        return mFragArray[index];
    }

    private void showFragment(final int index) {
        for (int i = 0; i < mFragArray.length; i++) {
            if (mFragArray[i] != null) {
                if (i == index) {
                    mFragmentTransaction.show(mFragArray[i]);
                } else {
                    mFragmentTransaction.hide(mFragArray[i]);
                }
            }
        }
    }

    private void addFragment(Fragment fragment, int fragmentTag) {
        if (!fragment.isAdded() && fragment.getTag() == null) {
            mFragmentTransaction.add(R.id.fragment_base_id, fragment, fragmentTag + "");
        }
    }
}
