package com.wcl.test.view.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.request.RequestOptions;
import com.wcl.test.GlideApp;

/**
 * 支持根据图片自身宽高比例自动调整尺寸的 ImageView。
 *
 * <p>当宽度或高度其中一个尺寸被父布局确定时，
 * 会根据 Drawable 自身的宽高比例自动计算另一个尺寸。</p>
 *
 * <p>例如图片原始尺寸为 1080 × 520：</p>
 * <ul>
 *     <li>宽度确定为 720px 时，高度自动计算为约 347px</li>
 *     <li>高度确定为 400px 时，宽度自动计算为约 831px</li>
 *     <li>宽高同时确定时，优先遵循父布局指定的尺寸</li>
 * </ul>
 *
 * <p>支持 Glide 等异步加载图片。图片加载完成后会自动重新测量，
 * 使 View 根据实际图片尺寸重新计算宽高。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &lt;com.wcl.test.view.image.RatioImageView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:src="@drawable/example" /&gt;
 * </pre>
 */
public class RatioImageView extends AppCompatImageView {

    public RatioImageView(Context context) {
        super(context);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();

        // 没有图片，交给系统处理
        if (drawable == null || drawable.getIntrinsicWidth() <= 0
                || drawable.getIntrinsicHeight() <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float ratio = (float) drawable.getIntrinsicWidth()
                / drawable.getIntrinsicHeight();

        // 宽高都被确定：尊重父布局
        if (widthMode == MeasureSpec.EXACTLY
                && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 高度确定，宽度自适应
        if (heightMode == MeasureSpec.EXACTLY) {
            int height = Math.min(heightSize, getMaxHeight());
            int width = Math.round((height - getPaddingTop() - getPaddingBottom()) * ratio)
                    + getPaddingLeft() + getPaddingRight();
            width = Math.min(width, getMaxWidth());

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }

            setMeasuredDimension(width, height);
            return;
        }

        // 宽度确定，高度自适应
        // 注意：match_parent 在 wrap_content 容器 / Dialog（默认窗口宽度 wrap_content）里会被降级为 AT_MOST，
        // 此时宽度仍会填满 widthSize，所以 AT_MOST 也要按宽度推导高度，否则比例自适应失效
        boolean widthFixed = widthMode == MeasureSpec.EXACTLY
                || (widthMode == MeasureSpec.AT_MOST
                && getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT);
        if (widthFixed) {
            int width = Math.min(widthSize, getMaxWidth());
            int height = Math.round((width - getPaddingLeft() - getPaddingRight()) / ratio)
                    + getPaddingTop() + getPaddingBottom();
            height = Math.min(height, getMaxHeight());

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }

            setMeasuredDimension(width, height);
            return;
        }

        // 宽高都不是确定的：交给系统处理
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 使用 Glide 加载网络或本地图片
     *
     * <p>与 {@link GlideImageView} 不同的是，此处不做 CenterCrop / 圆角等变换，
     * 保持图片原始宽高比例。图片加载完成后会自动重新测量，
     * 使 View 根据实际图片尺寸重新计算宽高。</p>
     *
     * @param uri 图片 URL、文件路径或资源 ID
     */
    public void loadImage(Object uri) {
        loadImage(uri, new RequestOptions());
    }

    /**
     * 使用 Glide 加载网络或本地图片，支持自定义加载参数
     *
     * @param uri     图片 URL、文件路径或资源 ID
     * @param options 自定义 RequestOptions
     */
    public void loadImage(Object uri, RequestOptions options) {
        GlideApp.with(this)
                .load(uri)
                .apply(options)
                .into(this);
    }
}