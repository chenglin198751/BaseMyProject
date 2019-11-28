package cheerly.mybaseproject.widget;

import android.animation.ObjectAnimator;
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
    private ObjectAnimator mValueAnimator;

    public WaitDialog(Context context) {
        this(context, R.style.dialogNullBg);
        mView = View.inflate(context, R.layout.wait_dialog_layout, null);
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

        mValueAnimator = ObjectAnimator.ofFloat(mLoadImg, "Rotation", 0f, 360f);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(2 * 1000);
        mValueAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mValueAnimator.cancel();
    }
}
