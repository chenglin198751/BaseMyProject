package widget;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import cheerly.mybaseproject.R;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by chenglin on 2017-7-12.
 */

public class CustomRefreshHeader implements PtrUIHandler {
    private Context mContext;
    private View mView;
    private ImageView mLoadingView;

    public CustomRefreshHeader(Context context) {
        mContext = context;
        mView = View.inflate(mContext, R.layout.pull_to_refresh_header, null);
        mLoadingView = (ImageView) mView.findViewById(R.id.image_view);
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {
        stopRotate();
    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {
        startRotate();
    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame, boolean isHeader) {
        stopRotate();
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        float currentPercent = Math.min(1f, ptrIndicator.getCurrentPercent());
        mLoadingView.setRotation(currentPercent * 360f);
    }

    private void startRotate() {
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
