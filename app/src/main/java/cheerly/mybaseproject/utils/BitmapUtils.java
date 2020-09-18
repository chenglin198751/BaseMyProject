package cheerly.mybaseproject.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.listener.OnCompressBitmapListener;

/**
 * Created by chenglin on 2017-5-24.
 */

public class BitmapUtils {
    /**
     * 返回图片宽高数组，第0个是宽，第1个是高
     */
    public static int[] getBitmapWidthHeight(final String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        int[] size = new int[2];
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;
    }

    //节省每次创建时产生的开销，但要注意多线程操作synchronized
    private static final Canvas sCanvas = new Canvas();

    /**
     * 从一个view创建Bitmap。
     * 注意点：绘制之前要清掉 View 的焦点，因为焦点可能会改变一个 View 的 UI 状态。
     * 来源：https://github.com/tyrantgit/ExplosionField
     *
     * @param view  传入一个 View，会获取这个 View 的内容创建 Bitmap。
     * @param scale 缩放比例，对创建的 Bitmap 进行缩放，数值支持从 0 到 1。
     */
    public static Bitmap createBitmapFromView(View view, float scale) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.clearFocus();
        Bitmap bitmap = createBitmapSafely((int) (view.getWidth() * scale),
                (int) (view.getHeight() * scale), Bitmap.Config.ARGB_8888, 1);
        if (bitmap != null) {
            synchronized (sCanvas) {
                Canvas canvas = sCanvas;
                canvas.setBitmap(bitmap);
                canvas.save();
                canvas.drawColor(Color.WHITE); // 防止 View 上面有些区域空白导致最终 Bitmap 上有些区域变黑
                canvas.scale(scale, scale);
                view.draw(canvas);
                canvas.restore();
                canvas.setBitmap(null);
            }
        }
        return bitmap;
    }

    public static Bitmap createBitmapFromView(View view) {
        return createBitmapFromView(view, 1f);
    }

    /**
     * 从一个view创建Bitmap。把view的区域截掉leftCrop/topCrop/rightCrop/bottomCrop
     */
    public static Bitmap createBitmapFromView(View view, int leftCrop, int topCrop, int rightCrop, int bottomCrop) {
        Bitmap originBitmap = createBitmapFromView(view);
        if (originBitmap == null) {
            return null;
        }
        Bitmap cutBitmap = createBitmapSafely(view.getWidth() - rightCrop - leftCrop, view.getHeight() - topCrop - bottomCrop, Bitmap.Config.ARGB_8888, 1);
        if (cutBitmap == null) {
            return null;
        }
        Canvas canvas = new Canvas(cutBitmap);
        Rect src = new Rect(leftCrop, topCrop, view.getWidth() - rightCrop, view.getHeight() - bottomCrop);
        Rect dest = new Rect(0, 0, view.getWidth() - rightCrop - leftCrop, view.getHeight() - topCrop - bottomCrop);
        canvas.drawColor(Color.WHITE); // 防止 View 上面有些区域空白导致最终 Bitmap 上有些区域变黑
        canvas.drawBitmap(originBitmap, src, dest, null);
        originBitmap.recycle();
        return cutBitmap;
    }

    /**
     * 安全的创建bitmap。
     * 如果新建 Bitmap 时产生了 OOM，可以主动进行一次 GC - System.gc()，然后再次尝试创建。
     *
     * @param width      Bitmap 宽度。
     * @param height     Bitmap 高度。
     * @param config     传入一个 Bitmap.Config。
     * @param retryCount 创建 Bitmap 时产生 OOM 后，主动重试的次数。
     * @return 返回创建的 Bitmap。
     */
    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }

    public static String getPathByUri(Context context, Uri uri) {
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        if (activity == null) {
            return null;
        }

        String path = null;
        if (uri == null) {
            return path;
        }
        if ("content".equals(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } else if ("file".equals(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }

    /**
     * 此方法仅仅用于计算createScaledBitmap() 方法获取Bitmap 时的缩放比例。
     * 目的是为了防止OOM
     */
    private static int getOptionSize(final float size) {
        int optionSize = 1;
        if (size >= 2f && size < 4f) {
            optionSize = 2;
        } else if (size >= 4f && size < 8f) {
            optionSize = 4;
        } else if (size >= 8f && size < 16) {
            optionSize = 8;
        } else if (size >= 16f) {
            optionSize = 16;
        }
        return optionSize;
    }

    /**
     * 保存图片到sdcard
     */
    public static void saveBitmap(final Bitmap bmp, final OnCompressBitmapListener callback) {
        if (callback != null) {
            callback.onPrepare();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(SDCardUtils.getDataPath(SDCardUtils.TYPE_CACHE), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    if (callback != null) {
                        callback.onError();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError();
                    }
                }
                String path = file.getAbsolutePath();

                if (callback != null) {
                    callback.onSucceed(path);
                }

            }
        }).start();

    }


    /**
     * 按原图比例缩放裁剪图片
     *
     * @param activity   Activity
     * @param imageUri   图片的Uri
     * @param imageWidth 想要被缩放到的target图片宽度，我会根据此宽度和原图的比例，去计算target图片高度
     * @param callback   回调监听
     */
    public static void createScaledBitmap(final Activity activity, final Uri imageUri, int imageWidth, final OnCompressBitmapListener callback) {
        final String imagePath = getPathByUri(activity, imageUri);
        createScaledBitmap(activity, imagePath, imageWidth, callback);
    }

    /**
     * 按原图比例缩放裁剪图片
     *
     * @param activity   Activity
     * @param imagePath  图片的路径
     * @param imageWidth 想要被缩放到的target图片宽度，我会根据此宽度和原图的比例，去计算target图片高度
     * @param callback   回调监听
     */
    public static void createScaledBitmap(final Activity activity, final String imagePath, final int imageWidth, final OnCompressBitmapListener callback) {
        if (callback == null) {
            throw new NullPointerException("OnCompressBitmapListener must not null");
        }
        callback.onPrepare();

        if (TextUtils.isEmpty(imagePath) || !(new File(imagePath).exists())) {
            callback.onError();
            return;
        }

        //原图宽度大于传入的宽度才压缩，否则不压缩，直接返回原图路径
        final int[] size = getBitmapWidthHeight(imagePath);
        if (size[0] <= imageWidth) {
            callback.onSucceed(imagePath);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                int imageHeight = (int) (size[1] * 1f * imageWidth * 1f / size[0] * 1f);
                float scale = size[0] * 1f / imageWidth * 1f;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = getOptionSize(scale);
                Bitmap targetBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath, options), imageWidth, imageHeight, true);

                saveBitmap(targetBitmap, new OnCompressBitmapListener() {
                    @Override
                    public void onPrepare() {

                    }

                    @Override
                    public void onSucceed(final Object path) {
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSucceed(path);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError() {
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError();
                                }
                            });
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * 返回bitmap 所占内存的大小
     */
    public static int getBitmapBytes(Bitmap bitmap) {
        return bitmap.getByteCount();
    }
}
