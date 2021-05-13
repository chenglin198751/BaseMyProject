package cheerly.mybaseproject.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class ShimmerTextView extends AppCompatTextView {

    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private Paint mPaint;
    private int mViewWidth = 0;
    private int mTranslate = 0;
    private int mStartColor, mEndColor;

    private boolean mAnimating = true;

    public ShimmerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startAnimating() {
        if (!mAnimating) {
            mAnimating = true;
            invalidate();
        }
    }

    public void stopAnimating() {
        mAnimating = false;
    }

    public void setTextGradientColor(int startColor, int endColor) {
        mStartColor = startColor;
        mEndColor = endColor;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getMeasuredWidth();

        if (mViewWidth > 0) {
            mPaint = getPaint();
            mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,
                    new int[]{mStartColor, mEndColor, mStartColor},
                    new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
            mPaint.setShader(mLinearGradient);
            mGradientMatrix = new Matrix();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimating && mGradientMatrix != null && mViewWidth > 0) {
            mTranslate += mViewWidth * 1f / 10f;
            if (mTranslate > 2 * mViewWidth) {
                mTranslate = -mViewWidth;
            }
            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
            postInvalidateDelayed(50);
        }
    }

}