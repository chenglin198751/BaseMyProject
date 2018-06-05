package view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cheerly.mybaseproject.R;
import utils.BaseUtils;

public class RippleView extends RelativeLayout {

    //涟漪的颜色
    private int rippleColor = getResources().getColor(R.color.colorAccent);
    //最里面涟漪的实心圆
    private float rippleStrokeWidth = 10;
    //涟漪的半径
    private float rippleRadius = BaseUtils.dip2px(30f);
    //自定义的动画开始与结束接口
    private AnimationListener mAnimationListener;
    //画笔
    private Paint paint;
    //动画标志
    private boolean animationRunning = false;
    //动画集合
    private AnimatorSet animatorSet;
    //自定义view集合
    private ArrayList<CircleView> rippleViewList = new ArrayList<>();
    //每次动画的时间
    private static final int RIPPLE_DURATION_TIME = 5000;
    //涟漪条目
    private static final int RIPPLE_AMOUNT = 4;
    //每条涟漪依次出现的延迟
    private int rippleDelay;
    //涟漪从出现到消失的动画次数，ValueAnimator.INFINITE为无限次
    private int mRepeatCount = 4;


    public RippleView(Context context) {
        super(context);
    }


    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private void init() {
        //画每个圆的时间间隔为一个圆的动画时间除以总共出现圆的个数，达到每个圆出现的时间间隔一致
        rippleDelay = RIPPLE_DURATION_TIME / RIPPLE_AMOUNT;
        //初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        //布局 管理器，让圆剧中显示
        LayoutParams rippleParams = new LayoutParams((int) (2 * (rippleRadius + rippleStrokeWidth)), (int) (2 * (rippleRadius + rippleStrokeWidth)));
        rippleParams.addRule(CENTER_IN_PARENT, TRUE);

        animatorSet = new AnimatorSet();
        animatorSet.setDuration(RIPPLE_DURATION_TIME);
        //加速插值器
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        //圆圈的集合
        ArrayList<Animator> animatorList = new ArrayList<>();
        //缩放、渐变动画
        for (int i = 0; i < RIPPLE_AMOUNT; i++) {

            CircleView rippleView = new CircleView(getContext());
            addView(rippleView, rippleParams);
            rippleViewList.add(rippleView);

            //伸缩动画
            float rippleScale = 6.0f;
            final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale);
            scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
            scaleXAnimator.setStartDelay(i * rippleDelay);
            animatorList.add(scaleXAnimator);

            final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale);
            scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);//ObjectAnimator.RESTART
            scaleYAnimator.setStartDelay(i * rippleDelay);
            animatorList.add(scaleYAnimator);

            //透明度动画
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setStartDelay(i * rippleDelay);
            animatorList.add(alphaAnimator);

        }
        //开始动画
        animatorSet.playTogether(animatorList);
        //动画的监听
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mAnimationListener != null) {
                    mAnimationListener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mAnimationListener != null) {
                    mAnimationListener.onEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    //画一个圆
    private class CircleView extends View {

        CircleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //圆的半径，也就是它的父布局宽或高（取最小）的一半
            int radius = (Math.min(getWidth(), getHeight())) / 2;
            /**
             * 参数解析：
             * 圆心的x坐标。
             * 圆心的y坐标。
             * 圆的半径。
             * 绘制时所使用的画笔。
             */
            canvas.drawCircle(radius, radius, radius - rippleStrokeWidth, paint);

        }
    }

    /**
     * 对外的开始动画
     */
    public void startRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            for (CircleView rippleView : rippleViewList) {
                rippleView.setVisibility(VISIBLE);
            }
            animatorSet.start();
            animationRunning = true;
        }
    }

    /**
     * 对面外的结束动画
     */
    public void stopRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            for (CircleView rippleView : rippleViewList) {
                rippleView.setVisibility(INVISIBLE);
            }
            animatorSet.end();
            animationRunning = false;
        }
    }

    /**
     * 设置涟漪的颜色
     */
    public RippleView setRippleColor(@ColorInt int color){
        rippleColor = color;
        return this;
    }

    public RippleView build(){
        init();
        return this;
    }

    private boolean isRippleAnimationRunning() {
        return animationRunning;
    }

    /**
     * 对外的接口
     */
    public interface AnimationListener {
        void onStart();

        void onEnd();
    }

    public void setAnimationListener(AnimationListener listener) {
        this.mAnimationListener = listener;
    }

}