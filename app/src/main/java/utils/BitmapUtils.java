package utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import listener.SaveBitmapCallback;

/**
 * Created by chenglin on 2017-5-24.
 */

public class BitmapUtils {

    /**
     * 根据传入的图片path压缩图片到指定大小
     */
    public static void compressBitmap(Context context, final String path, final int targetWidth, Target target) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        int[] size = getBitmapSize(path);
        int targetHeight = targetWidth * size[1] / size[0];
        Picasso.with(context).load(new File(path)).resize(targetWidth, targetHeight).into(target);
    }

    /**
     * 根据传入的URI压缩图片到指定大小
     */
    public static void compressBitmap(Context context, final Uri uri, final int targetWidth, Target target) {
        String path = getPathByUri(context, uri);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        int[] size = getBitmapSize(path);
        int targetHeight = targetWidth * size[1] / size[0];
        Picasso.with(context).load(uri).resize(targetWidth, targetHeight).into(target);
    }

    /**
     * 返回图片宽高数组，0是宽，1是高
     */
    public static int[] getBitmapSize(final String path) {
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

    public static void saveBitmap(final Bitmap bmp, final SaveBitmapCallback callback) {
        if (callback != null) {
            callback.onPrepare();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(SDCardUtils.SDCARD_PATH, fileName);
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
}
