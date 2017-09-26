package pullrefresh;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-7-12.
 */

public class CustomRefreshHeader implements RefreshHeader {
    private Context mContext;
    private View mView;
    private ImageView mLoadingView;
    private TextView mLoadingTextView;

    public CustomRefreshHeader(Context context) {
        mContext = context;
        mView = View.inflate(mContext, R.layout.pull_to_refresh_header, null);
        mLoadingView = (ImageView) mView.findViewById(R.id.image_view);
        mLoadingTextView = (TextView) mView.findViewById(R.id.text_view);
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(@ColorInt int... colors) {

    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {
        startRotate();
    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        stopRotate();
        mLoadingTextView.setText(R.string.cube_ptr_refresh_complete);
        return 300;
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
        mLoadingView.setRotation(percent * 360f);
    }

    @Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {

    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        switch (newState) {
            case None:
                break;
            case PullDownToRefresh:
                mLoadingTextView.setText(R.string.cube_ptr_pull_down_to_refresh);
                break;
            case Refreshing:
                mLoadingTextView.setText(R.string.cube_ptr_refreshing);
                break;
            case ReleaseToRefresh:
                mLoadingTextView.setText(R.string.cube_ptr_release_to_refresh);
                break;
        }
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
