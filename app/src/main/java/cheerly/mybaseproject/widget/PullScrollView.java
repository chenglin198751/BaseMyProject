package cheerly.mybaseproject.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ScrollView;


public class PullScrollView extends ScrollView {
    private View mVideoParentView;
    private SurfaceView mTopVideo;
    private int mVideoOriginalHeight;
    private OnScrolledListener<Boolean> mOnScrolledListener;
    private OnScrollingListener<Integer> mOnScrollingListener;
    private int mVideoHeight, mVideoWidth;

    public interface OnScrolledListener<T> {
        void onScrolled(T t);
    }

    public interface OnScrollingListener<T> {
        void onScrolling(T t);
    }

    public PullScrollView(Context context) {
        this(context, null);
    }

    public PullScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mVideoOriginalHeight = 100;
    }

    public void setOnScrolledListener(OnScrolledListener listener) {
        mOnScrolledListener = listener;
    }

    public void setOnScrollingListener(OnScrollingListener<Integer> listener) {
        mOnScrollingListener = listener;
    }

    public void setParallaxHeader(View headerView) {
        this.mVideoParentView = headerView;
    }

    public void setVideoWidthHeight(int width, int height) {
        mVideoHeight = height;
        mVideoWidth = width;
    }

    /**
     * 重写overScrollBy，能获取ListView下拉的距离
     *
     * @param deltaX:横向的变化量
     * @param deltaY：纵向的变化量
     * @param scrollX：横向X的偏移量
     * @param scrollY：纵向Y的偏移量
     * @param scrollRangeX：横向X偏移范围
     * @param scrollRangeY：纵向Y的偏移范围
     * @param maxOverScrollX：横向X最大的偏移量
     * @param maxOverScrollY：纵向Y最大的偏移量
     * @param isTouchEvent：是否是触摸产生的滑动超出
     * @return
     */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX,
                                   int scrollRangeY, int maxOverScrollX,
                                   int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY < 0) {
            mVideoParentView.getLayoutParams().height = (int) (mVideoParentView.getHeight() - deltaY * 0.5f);
            if (mVideoParentView.getLayoutParams().height >= mVideoHeight) {
                mTopVideo.getLayoutParams().width = (int) (mTopVideo.getWidth() - deltaY * 2f);
                mTopVideo.getLayoutParams().height = mTopVideo.getLayoutParams().width * 9 / 16;
            }
            mVideoParentView.requestLayout();
            mTopVideo.requestLayout();
            if (mOnScrolledListener != null) {
                mOnScrolledListener.onScrolled(true);
            }
        } else {
            if (mVideoParentView.getLayoutParams().height >= mVideoOriginalHeight) {
                mVideoParentView.getLayoutParams().height = (int) (mVideoParentView.getHeight() - deltaY * 0.5f);
                mVideoParentView.requestLayout();
            }

            if (mTopVideo.getLayoutParams().width > mVideoWidth) {
                mTopVideo.getLayoutParams().width = (int) (mTopVideo.getWidth() - deltaY * 2f);
                mTopVideo.getLayoutParams().height = mTopVideo.getLayoutParams().width * 9 / 16;
                mTopVideo.requestLayout();
            }
        }

        if (mOnScrollingListener != null) {
            mOnScrollingListener.onScrolling(scrollY);
        }

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mVideoParentView.getHeight() > mVideoOriginalHeight) {
                    setAnimation();
                }

                if (mOnScrolledListener != null) {
                    mOnScrolledListener.onScrolled(false);
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void setAnimation() {
        int nowHeight = mVideoParentView.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(nowHeight, mVideoOriginalHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mVideoParentView.getLayoutParams().height = value;
                mVideoParentView.requestLayout();
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();

        int nowWidth = mTopVideo.getWidth();
        ValueAnimator valueAnimator2 = ValueAnimator.ofInt(nowWidth, mVideoWidth);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mTopVideo.getLayoutParams().width = value;
                mTopVideo.getLayoutParams().height = mTopVideo.getLayoutParams().width * 9 / 16;

                mTopVideo.requestLayout();
            }
        });
        valueAnimator2.setDuration(300);
        valueAnimator2.start();
    }
}
