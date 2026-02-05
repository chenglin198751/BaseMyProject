package com.wcl.test.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.listener.OnCompressBitmapListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitmap 工具类 — 安全、高效、兼容性良好。
 * Created by chenglin, optimized by ChatGPT.
 */
public class BitmapUtils {

    private BitmapUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取图片宽高（不加载像素数据，避免OOM）
     */
    public static int[] getBitmapSize(@NonNull String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        return new int[]{opts.outWidth, opts.outHeight};
    }

    /**
     * 安全创建Bitmap，支持重试。
     */
    public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }

    /**
     * 从View创建Bitmap（安全版）
     */
    public static Bitmap createBitmapFromView(@NonNull View view, float scale) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) return null;

        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }

        view.clearFocus();
        Bitmap bitmap = createBitmapSafely(
                Math.max(1, (int) (view.getWidth() * scale)),
                Math.max(1, (int) (view.getHeight() * scale)),
                Bitmap.Config.ARGB_8888, 1);
        if (bitmap == null) return null;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.scale(scale, scale);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap createBitmapFromView(View view) {
        return createBitmapFromView(view, 1f);
    }

    /**
     * 获取Uri对应的真实路径（兼容）
     */
    public static String getPathByUri(@NonNull Context context, @NonNull Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return copyUriToCache(context, uri);
        }

        return null;
    }

    private static String copyUriToCache(Context context, Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) return null;

            File dir = new File(context.getCacheDir(), "image_pick");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            AppLogUtils.e("UriUtils", e.toString());
        }
        return null;
    }


    /**
     * 保存Bitmap到本地（异步 + 回调）
     */
    public static void saveBitmapAsync(final Bitmap bmp, final OnCompressBitmapListener<String> callback) {
        if (bmp == null) {
            postToUi(() -> callback.onFailed("Bitmap is null"));
            return;
        }

        new Thread(() -> {
            File dir = new File(AppFileUtils.getAppFilesPath());
            if (!dir.exists() && !dir.mkdirs()) {
                postToUi(() -> callback.onFailed("Create directory failed"));
                return;
            }

            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                postToUi(() -> callback.onSucceed(file.getAbsolutePath()));
            } catch (IOException e) {
                postToUi(() -> callback.onFailed("IOException: " + e.getMessage()));
            }
        }).start();
    }

    private static void postToUi(Runnable r) {
        AppUtils.getUiHandler().post(r);
    }

    /**
     * 按原比例压缩图片到指定宽度。
     */
    public static Bitmap resizeBitmap(String path, int targetWidth) {
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) return null;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int srcW = opts.outWidth;
        int srcH = opts.outHeight;
        if (srcW <= 0 || srcH <= 0) return null;

        if (srcW <= targetWidth) {
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, opts);
        }

        int inSampleSize = 1;
        while (srcW / inSampleSize > targetWidth * 2) {
            inSampleSize *= 2;
        }

        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        if (bitmap == null) return null;

        int targetH = Math.round(srcH * (targetWidth / (float) srcW));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetH, true);
        if (scaled != bitmap) bitmap.recycle();
        return scaled;
    }

    /**
     * 截取 ScrollView 完整内容。
     */
    public static Bitmap shotScrollView(@NonNull ScrollView scrollView) {
        int totalHeight = 0;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            totalHeight += scrollView.getChildAt(i).getHeight();
        }
        Bitmap bitmap = createBitmapSafely(scrollView.getWidth(), totalHeight, Bitmap.Config.RGB_565, 1);
        if (bitmap == null) return null;
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    /**
     * 截取 ListView 全部内容。
     */
    public static Bitmap shotListView(@NonNull ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return null;

        int totalHeight = 0;
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            View child = adapter.getView(i, null, listView);
            child.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

            Bitmap bmp = createBitmapSafely(child.getMeasuredWidth(), child.getMeasuredHeight(), Bitmap.Config.ARGB_8888, 1);
            if (bmp == null) continue;
            Canvas canvas = new Canvas(bmp);
            child.draw(canvas);
            bitmaps.add(bmp);
            totalHeight += child.getMeasuredHeight();
        }

        Bitmap big = Bitmap.createBitmap(listView.getWidth(), totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(big);
        int y = 0;
        for (Bitmap bmp : bitmaps) {
            canvas.drawBitmap(bmp, 0, y, null);
            y += bmp.getHeight();
            bmp.recycle();
        }
        return big;
    }

    /**
     * 截取 RecyclerView 全部内容。
     */
    public static Bitmap shotRecyclerView(@NonNull RecyclerView rv) {
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = rv.getAdapter();
        if (adapter == null) return null;

        int count = adapter.getItemCount();
        Paint paint = new Paint();
        int totalHeight = 0;

        List<Bitmap> cache = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, adapter.getItemViewType(i));
            adapter.onBindViewHolder(holder, i);

            holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(rv.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());

            Bitmap bmp = createBitmapSafely(holder.itemView.getWidth(), holder.itemView.getHeight(), Bitmap.Config.ARGB_8888, 1);
            if (bmp == null) continue;
            Canvas canvas = new Canvas(bmp);
            holder.itemView.draw(canvas);
            cache.add(bmp);
            totalHeight += bmp.getHeight();
        }

        Bitmap big = Bitmap.createBitmap(rv.getWidth(), totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(big);
        int y = 0;
        for (Bitmap bmp : cache) {
            canvas.drawBitmap(bmp, 0, y, paint);
            y += bmp.getHeight();
            bmp.recycle();
        }
        return big;
    }

    /**
     * 按纵向拼接多张图片。
     */
    public static Bitmap combineBitmaps(@NonNull Bitmap... bitmaps) {
        if (bitmaps.length == 0) return null;
        int width = 0, height = 0;
        for (Bitmap b : bitmaps) {
            width = Math.max(width, b.getWidth());
        }
        for (Bitmap b : bitmaps) {
            height += b.getHeight() * width / b.getWidth();
        }

        Bitmap combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);
        int offsetY = 0;
        for (Bitmap b : bitmaps) {
            int newH = b.getHeight() * width / b.getWidth();
            Bitmap scaled = Bitmap.createScaledBitmap(b, width, newH, true);
            canvas.drawBitmap(scaled, 0, offsetY, null);
            offsetY += newH;
            if (scaled != b) scaled.recycle();
            b.recycle();
        }
        return combined;
    }

    /**
     * 为未attach的View生成bitmap（不使用drawingCache）
     */
    public static Bitmap createMeasureViewBitmap(@NonNull View view, int width) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = createBitmapSafely(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888, 1);
        if (bitmap == null) return null;
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
