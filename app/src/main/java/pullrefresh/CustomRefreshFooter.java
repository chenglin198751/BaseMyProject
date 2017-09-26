package pullrefresh;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-7-12.
 */

public class CustomRefreshFooter implements RefreshFooter {
    private Context mContext;
    private View mView;
    private ImageView mLoadingView;
    private TextView mLoadingTextView;
    protected boolean mLoadmoreFinished = false;

    public CustomRefreshFooter(Context context) {
        mContext = context;
        mView = View.inflate(mContext, R.layout.pull_to_refresh_footer, null);
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
        if (!mLoadmoreFinished) {
            stopRotate();
            mLoadingTextView.setText(R.string.cube_ptr_load_complete);
        }
        return 300;
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }


    @Override
    public void onPullingUp(float percent, int offset, int footerHeight, int extendHeight) {
        mLoadingView.setRotation(percent * 360f);
    }

    @Override
    public void onPullReleasing(float percent, int offset, int footerHeight, int extendHeight) {

    }

    /**
     * 设置数据全部加载完成，将不能再次触发加载功能
     */
    @Override
    public boolean setLoadmoreFinished(boolean finished) {
        if (mLoadmoreFinished != finished) {
            mLoadmoreFinished = finished;
            if (finished) {
                stopRotate();
                mLoadingView.setVisibility(View.GONE);
                mLoadingTextView.setText(R.string.cube_ptr_all_load_complete);
            } else {
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText(R.string.cube_ptr_pull_up_to_load);
            }
        }
        return true;
    }


    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        if (!mLoadmoreFinished) {
            switch (newState) {
                case None:
                    break;
                case PullToUpLoad:
                    mLoadingTextView.setText(R.string.cube_ptr_pull_up_to_load);
                    break;
                case Loading:
                    mLoadingTextView.setText(R.string.cube_ptr_loading);
                    break;
                case ReleaseToLoad:
                    mLoadingTextView.setText(R.string.cube_ptr_release_to_refresh);
                    break;
                case Refreshing:
                    mLoadingTextView.setText(R.string.cube_ptr_loading);
                    break;
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
