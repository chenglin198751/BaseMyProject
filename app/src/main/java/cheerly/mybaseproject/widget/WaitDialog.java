package cheerly.mybaseproject.widget;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import cheerly.mybaseproject.R;


public class WaitDialog extends Dialog {
    private View mView;
    private ImageView mLoadImg;
    private ValueAnimator mValueAnimator;

    public WaitDialog(Context context) {
        this(context, R.style.dialogNullBg);
        mView = View.inflate(context, R.layout.progress_layout, null);
    }

    protected WaitDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        mLoadImg = mView.findViewById(R.id.image);
    }

    @Override
    public View findViewById(int id) {
        return mView.findViewById(id);
    }


    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mValueAnimator = ValueAnimator.ofFloat(360f);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mLoadImg.setRotation(value);
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(2 * 1000);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mValueAnimator.cancel();
    }
}
