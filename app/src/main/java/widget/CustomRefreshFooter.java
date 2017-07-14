package widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import cheerly.mybaseproject.R;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by chenglin on 2017-7-12.
 */

public class CustomRefreshFooter implements PtrUIHandler {
    private Context mContext;
    private View mView;
    private ImageView mLoadingView;
    private TextView mLoadingTextView;

    public CustomRefreshFooter(Context context) {
        mContext = context;
        mView = View.inflate(mContext, R.layout.pull_to_refresh_footer, null);
        mLoadingView = (ImageView) mView.findViewById(R.id.image_view);
        mLoadingTextView = (TextView) mView.findViewById(R.id.text_view);
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        stopRotate();
        mLoadingTextView.setText(R.string.cube_ptr_pull_up_to_load);
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {
        stopRotate();
        mLoadingTextView.setText(R.string.cube_ptr_pull_up_to_load);
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        startRotate();
        mLoadingTextView.setText(R.string.cube_ptr_loading);
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame, boolean isHeader) {
        stopRotate();
        mLoadingTextView.setText(R.string.cube_ptr_load_complete);
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
//        float currentPercent = Math.min(1f, ptrIndicator.getCurrentPercent());
        mLoadingView.setRotation(ptrIndicator.getCurrentPercent() * 240f);

        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();
        final int lastPos = ptrIndicator.getLastPosY();

        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                mLoadingTextView.setText(R.string.cube_ptr_pull_up_to_load);
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                mLoadingTextView.setText(R.string.cube_ptr_release_to_refresh);
            }
        }
    }

    private void startRotate() {
        mLoadingView.clearAnimation();
        final RotateAnimation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(800);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        mLoadingView.startAnimation(animation);
    }

    private void stopRotate() {
        mLoadingView.clearAnimation();
    }
}
