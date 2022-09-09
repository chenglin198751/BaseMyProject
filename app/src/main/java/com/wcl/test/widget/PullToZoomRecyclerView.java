package com.wcl.test.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.utils.AppConstants;

public class PullToZoomRecyclerView extends RecyclerView {
    private int mTouchSlop;
    private View zoomView;
    // 记录首次按下位置
    private float mFirstPosition = 0;
    // 是否正在放大
    private Boolean mScaling = false;
    private float mScale, mScaleRatio;

    LinearLayoutManager mLinearLayoutManager;
    private int screenWidth;

    public PullToZoomRecyclerView(Context context) {
        this(context, null);
    }

    public PullToZoomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToZoomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * @param viewScale           缩放View的比例，高/宽
     * @param scaleRatio          下拉阻尼系数，0到1之间取值
     * @param zoomView            要缩放的view
     * @param linearLayoutManager LinearLayoutManager
     */
    public void setZoomView(float viewScale, float scaleRatio, View zoomView, LinearLayoutManager linearLayoutManager) {
        mScale = viewScale;
        mScaleRatio = scaleRatio;
        //获取屏幕宽度
        screenWidth = AppConstants.screenWidth;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) zoomView.getLayoutParams();
        //获取屏幕宽度
        lp.width = screenWidth;
        //设置宽高比为16:9
        lp.height = (int) (screenWidth * mScale);
        //给imageView重新设置宽高属性
        zoomView.setLayoutParams(lp);
        this.zoomView = zoomView;
        mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (zoomView != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) zoomView.getLayoutParams();
            //判断触摸事件
            switch (event.getAction()) {
                //触摸结束
                case MotionEvent.ACTION_UP:
                    mScaling = false;
                    replyImage();
                    break;
                //触摸中
                case MotionEvent.ACTION_MOVE:
                    //判断是否正在放大 mScaling 的默认值为false
                    if (!mScaling) {
                        //当图片也就是第一个item完全可见的时候，记录触摸屏幕的位置
                        if (mLinearLayoutManager.findViewByPosition(mLinearLayoutManager.findFirstVisibleItemPosition()).getTop() == 0) {
                            //记录首次按下位置
                            mFirstPosition = event.getY();
                        } else {
                            break;
                        }
                    }
                    // 滚动距离乘以一个系数
                    int distance = (int) ((event.getY() - mFirstPosition) * mScaleRatio);
                    if (distance < 0) {
                        break;
                    }
                    // 处理放大
                    mScaling = true;

                    lp.width = zoomView.getWidth() + distance;
                    lp.height = (int) ((zoomView.getWidth() + distance) * mScale);
                    // 设置控件水平居中（如果不设置，图片的放大缩小是从图片顶点开始）
                    lp.setMargins(-(lp.width - screenWidth) / 2, 0, 0, 0);
                    zoomView.setLayoutParams(lp);
                    return true; // 返回true表示已经完成触摸事件，不再处理
            }
        }

        return super.onTouchEvent(event);
    }


    /**
     * 图片回弹动画
     */
    private void replyImage() {
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) zoomView.getLayoutParams();
        final float wPresent = zoomView.getLayoutParams().width;// 图片当前宽度
        final float hPresent = zoomView.getLayoutParams().height;// 图片当前高度

        final float width = screenWidth;// 图片原宽度
        final float height = screenWidth * mScale;// 图片原高度

        // 设置动画
        ValueAnimator anim = ObjectAnimator.ofFloat(0.0F, 1.0F).setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                lp.width = (int) (wPresent - (wPresent - width) * cVal);
                lp.height = (int) (hPresent - (hPresent - height) * cVal);
                lp.setMargins(-(lp.width - screenWidth) / 2, 0, 0, 0);
                zoomView.setLayoutParams(lp);
            }
        });
        anim.start();

    }


}