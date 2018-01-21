package view;

import android.annotation.SuppressLint;
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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.IOException;

import base.BaseActivity;
import base.MyApp;
import cheerly.mybaseproject.R;
import httpwork.HttpDownloadCallback;
import httpwork.HttpUtils;
import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import utils.GifCacheUtils;
import utils.MyUtils;

/**
 * Created by chenglin on 2017-7-14.
 */
@SuppressLint("AppCompatCustomView")
public class WebImageView extends ImageView {
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

    private RequestCreator getRequestCreator(Object object) {
        RequestCreator requestCreator = null;
        if (object instanceof String) {
            String url = (String) object;
            if (!TextUtils.isEmpty(url)) {
                requestCreator = Picasso.with(MyApp.getApp()).load(url.trim());
            }
        } else if (object instanceof File) {
            File file = (File) object;
            requestCreator = Picasso.with(MyApp.getApp()).load(file);
        } else if (object instanceof Integer) {
            int resourceId = (int) object;
            requestCreator = Picasso.with(MyApp.getApp()).load(resourceId);
        } else if (object instanceof Uri) {
            Uri uri = (Uri) object;
            requestCreator = Picasso.with(MyApp.getApp()).load(uri);
        }
        return requestCreator;
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是dp.
     * 如果 radius <0 ,那么就是纯圆圈的图片;
     * 如果 radius >0 是圆角
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     */
    public void loadRound(Object object, int imageWidth, int imageHeight, int radius) {
        CenterDrawable centerDrawable = new CenterDrawable(R.drawable.image_loadding_icon, radius);
        RequestCreator requestCreator = getRequestCreator(object);

        if (requestCreator != null) {
            requestCreator = requestCreator
                    .placeholder(centerDrawable)
                    .error(centerDrawable);

            if (imageWidth > 0 && imageHeight > 0) {
                requestCreator = requestCreator
                        .resize(imageWidth, imageHeight)
                        .centerCrop();
            }

            if (radius != CenterDrawable.RECTANGLE) {
                requestCreator = requestCreator.transform(radius <= 0 ? mPicassoCircleTransform : new PicassoRoundTransform(radius, radius));
            }
            requestCreator.into(this);
        } else {
            setImageDrawable(centerDrawable);
        }
    }

    /**
     * 加载图片，一定要传入 ImageView 的宽和高，因为这样可以很大的节约内存
     * 支持 gif 格式的图片，但是前提后缀名是.gif 才能解析
     * 如果图片宽度和高度都设置为-1 ，那么就是加载原图。不推荐，因为原图如果太大，很耗费内存。不过某种情况下确实需要加载原图
     */
    public void load(Object object, int imageWidth, int imageHeight) {
        CenterDrawable centerDrawable = new CenterDrawable(R.drawable.image_loadding_icon);
        if (object == null) {
            setImageDrawable(centerDrawable);
            return;
        }
        setTag(R.id.web_image_id, object);

        if (isGif(object)) {
            setImageDrawable(centerDrawable);
            setGifDrawable((String) object);
            return;
        }

        loadRound(object, imageWidth, imageHeight, CenterDrawable.RECTANGLE);
    }

    private boolean isGif(Object object) {
        if (object instanceof String) {
            String url = (String) object;
            if (url.toLowerCase().endsWith(".gif")) {
                return true;
            }
        }
        return false;
    }

    private void setGifDrawable(String url) {
        String key = buildKey(url);
        if (GifCacheUtils.get(key) != null) {
            setImageDrawable(GifCacheUtils.get(key));
        } else {
            downloadGif(url);
        }
    }

    private void downloadGif(final String url) {
        HttpUtils.downloadFile(url, true, new HttpDownloadCallback() {
            @Override
            public void onFailure(IOException e) {
            }

            @Override
            public void onSuccess(final String filePath) {
                if (isFinish()) {
                    return;
                }

                GifCacheUtils.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final GifDrawable gifDrawable = new GifDrawable(filePath);
                            GifCacheUtils.put(buildKey(url), gifDrawable);
                            if (isFinish()) {
                                return;
                            }
                            MyUtils.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isFinish()) {
                                        if (url.equals(getTag(R.id.web_image_id))) {
                                            setImageDrawable(gifDrawable);
                                        }
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent) {

            }
        });
    }

    private boolean isFinish() {
        if (getContext() instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) getContext();
            if (activity != null) {
                return activity.isFinishing();
            }
        }
        return true;
    }


    /**
     * 为了解决多个ImageView 共用一个GifDrawable 时，当此 GifDrawable 被回收时，
     * 别的ImageView 上面的GifDrawable 也会被回收，就会导致别的ImageView GIF 停止不播放。
     * 解决办法：把URL 和View 的hashCode 拼做一个参数当做Key 使用。
     * 如果有更好的解决办法，欢迎讨论 by chegnlin 2018年1月19日20:05:48
     */
    private String buildKey(final String url) {
        return MyUtils.MD5(url + "_" + this.hashCode());
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
