package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;

import base.MyApplication;
import utils.MyUtils;

/**
 * Created by chenglin on 2017-7-14.
 */

public class WebImageView extends AppCompatImageView {
    private final static PicassoCircleTransform mPicassoCircleTransform = new PicassoCircleTransform();

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 加载原图，不推荐，因为原图如果太大，很耗费内存。不过某种情况下确实需要加载原图
     */
    @Deprecated
    public void load(Object object) {
        load(object, -1, -1);
    }

    /**
     * 加载图片，一定要传入 ImageView 的宽和高，因为这样可以很大的节约内存
     */
    public void load(Object object, int imageWidth, int imageHeight) {
        if (object == null) {
            return;
        }
        RequestCreator requestCreator = null;

        if (object instanceof String) {
            String url = (String) object;
            if (!TextUtils.isEmpty(url)) {
                requestCreator = Picasso.with(MyApplication.getApp()).load(url.trim());
            }
        } else if (object instanceof File) {
            File file = (File) object;
            requestCreator = Picasso.with(MyApplication.getApp()).load(file);
        } else if (object instanceof Integer) {
            int resourceId = (int) object;
            requestCreator = Picasso.with(MyApplication.getApp()).load(resourceId);
        } else if (object instanceof Uri) {
            Uri uri = (Uri) object;
            requestCreator = Picasso.with(MyApplication.getApp()).load(uri);
        }

        if (requestCreator != null) {
            if (imageWidth > 0 && imageHeight > 0) {
                requestCreator.resize(imageWidth, imageHeight).centerCrop().into(this);
            } else {
                requestCreator.into(this);
            }
        }
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius单位是dp. 如果 radius <=0 ,那么就是圆形的图片，否则是圆角
     */
    public void loadRound(String url, int imageWidth, int imageHeight, int radius) {
        if (!TextUtils.isEmpty(url)) {
            Picasso.with(MyApplication.getApp())
                    .load(url.trim())
                    .resize(imageWidth, imageHeight)
                    .centerCrop()
                    .transform(radius <= 0 ? mPicassoCircleTransform : new PicassoRoundTransform(radius, radius))
                    .into(this);
        }
    }

    /**
     * picasso 的圆角图片转换器
     */
    private static final class PicassoRoundTransform implements Transformation {
        private int mRadiusX = 0, mRadiusY = 0;

        public PicassoRoundTransform(int radiusX, int radiusY) {
            mRadiusX = MyUtils.dip2px(radiusX);
            mRadiusY = MyUtils.dip2px(radiusY);
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int widthLight = source.getWidth();
            int heightLight = source.getHeight();

            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(output);
            Paint paintColor = new Paint();
            paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);
            paintColor.setAntiAlias(true);
            RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));
            canvas.drawRoundRect(rectF, mRadiusX, mRadiusY, paintColor);

            Paint paintImage = new Paint();
            paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(source, 0, 0, paintImage);
            source.recycle();
            return output;
        }

        @Override
        public String key() {
            return "roundcorner";
        }


    }

    /**
     * picasso 的圆形图片转换器
     */
    private static final class PicassoCircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
