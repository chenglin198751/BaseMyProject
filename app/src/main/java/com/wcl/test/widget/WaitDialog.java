package com.wcl.test.widget;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.wcl.test.R;


public class WaitDialog extends Dialog {
    private static final long ANIM_DURATION_MS = 2000L;

    private ImageView mLoadImg;
    private ObjectAnimator mValueAnimator;

    public WaitDialog(Context context) {
        this(context, R.style.dialogNullBg);
    }

    protected WaitDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wait_dialog_layout);
        mLoadImg = findViewById(R.id.image);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 每次显示时重新创建动画，确保状态干净
        mValueAnimator = ObjectAnimator.ofFloat(mLoadImg, "rotation", 0f, 360f);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mValueAnimator.setDuration(ANIM_DURATION_MS);
        mValueAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 防止极端情况下 mValueAnimator 未初始化导致崩溃
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }
}
