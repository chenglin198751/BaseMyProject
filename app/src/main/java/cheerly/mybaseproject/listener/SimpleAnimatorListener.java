package cheerly.mybaseproject.listener;

import android.animation.Animator;

/**
 * Created by chenglin on 2017-12-25.
 * 参看： AnimatorListenerAdapter 官方实现的abstract 监听
 */
public abstract class SimpleAnimatorListener implements Animator.AnimatorListener {
    public abstract void onAnimationEnd();

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        onAnimationEnd();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
