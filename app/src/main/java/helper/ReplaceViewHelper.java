package helper;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by chenglin on 2017-5-24.
 */

public class ReplaceViewHelper {
    private Activity mActivity;
    private View mInsertView;
    private View mReplaceView = null;

    public ReplaceViewHelper(Activity activity, int insertViewId) {
        mActivity = activity;
        mInsertView = mActivity.findViewById(insertViewId);
    }

    private ReplaceViewHelper() {
    }

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     */
    public final ReplaceViewHelper toReplaceView(final View replaceView) {
        if (mInsertView == null) {
            return this;
        } else if (!(mInsertView.getParent() instanceof ViewGroup)) {
            return this;
        }

        ViewGroup parentViewGroup = (ViewGroup) mInsertView.getParent();
        int index = parentViewGroup.indexOfChild(mInsertView);
        if (mReplaceView != null) {
            parentViewGroup.removeView(mReplaceView);
        }
        mReplaceView = replaceView;
        mReplaceView.setLayoutParams(mInsertView.getLayoutParams());

        parentViewGroup.addView(mReplaceView, index);

        //RelativeLayout时别的View可能会依赖这个View的位置，所以不能GONE
        if (parentViewGroup instanceof RelativeLayout) {
            mInsertView.setVisibility(View.INVISIBLE);
        } else {
            mInsertView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     */
    public final ReplaceViewHelper toReplaceView(int resLayoutId) {
        View view = View.inflate(mActivity, resLayoutId, null);
        toReplaceView(view);
        return this;
    }

    /**
     * 移除你替换进来的View
     */
    public final ReplaceViewHelper removeReplaceView() {
        if (mReplaceView != null && mInsertView != null) {
            if (mInsertView.getParent() instanceof ViewGroup) {
                ViewGroup parentViewGroup = (ViewGroup) mInsertView.getParent();
                parentViewGroup.removeView(mReplaceView);
                mReplaceView = null;
                mInsertView.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }
}
