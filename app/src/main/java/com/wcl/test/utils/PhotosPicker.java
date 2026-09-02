package com.wcl.test.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.wcl.test.listener.OnFinishedListener2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 图片选择器（现代实现）
 * 支持 Activity 和 Fragment，自动返回图片绝对路径
 */
public class PhotosPicker {

    private final Context context;
    private final OnFinishedListener2<String> listener;
    private final ActivityResultLauncher<Intent> pickerLauncher;

    private PhotosPicker(@NonNull Context context,
                         @NonNull ActivityResultLauncher<Intent> launcher,
                         @NonNull OnFinishedListener2<String> listener) {
        this.context = context;
        this.pickerLauncher = launcher;
        this.listener = listener;
    }

    /**
     * 创建 Activity 版本的 Picker
     */
    public static PhotosPicker from(@NonNull AppCompatActivity activity, @NonNull OnFinishedListener2<String> listener) {
        Context appContext = activity.getApplicationContext();
        ActivityResultLauncher<Intent> launcher =
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != AppCompatActivity.RESULT_OK) return;
                    Uri uri = result.getData() == null ? null : result.getData().getData();
                    resolveUriAsync(appContext, uri, listener);
                });
        return new PhotosPicker(activity, launcher, listener);
    }

    /**
     * 创建 Fragment 版本的 Picker
     */
    public static PhotosPicker from(@NonNull Fragment fragment, @NonNull OnFinishedListener2<String> listener) {
        Context appContext = fragment.requireContext().getApplicationContext();
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != AppCompatActivity.RESULT_OK) return;
                    Uri uri = result.getData() == null ? null : result.getData().getData();
                    resolveUriAsync(appContext, uri, listener);
                });
        return new PhotosPicker(fragment.requireContext(), launcher, listener);
    }

    /**
     * 启动选择器
     */
    public void start() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        pickerLauncher.launch(intent);
    }

    /**
     * 异步解析 URI 为本地路径
     */
    private static void resolveUriAsync(Context ctx, Uri uri, OnFinishedListener2<String> listener) {
        AppThreadPoolExecutor.getExecutor().execute(() -> {
            String path = UriPathResolver.getPath(ctx, uri);
            AppUtils.getUiHandler().post(() -> {
                if (listener != null) listener.onFinished(path);
            });
        });
    }

    // ---------------- 内部类：路径解析 ----------------

    public static class UriPathResolver {

        /**
         * 获取图片的本地路径（兼容 Android 10+）
         */
        public static String getPath(Context context, Uri uri) {
            if (uri == null) return null;
            if ("file".equalsIgnoreCase(uri.getScheme())) return uri.getPath();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                String path = LegacyPathResolver.getPathFromUri(context, uri);
                if (!TextUtils.isEmpty(path)) return path;
            }
            return copyUriToCache(context, uri);
        }

        private static String copyUriToCache(Context context, Uri uri) {
            File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (cacheDir == null) cacheDir = context.getCacheDir();
            if (!cacheDir.exists() && !cacheDir.mkdirs()) cacheDir = context.getCacheDir();
            File destFile = new File(cacheDir, "IMG_" + UUID.randomUUID() + ".jpg");

            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                try (OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                }
                return destFile.getAbsolutePath();
            } catch (Exception e) {
                if (destFile.exists()) destFile.delete();
                e.printStackTrace();
                return null;
            }
        }
    }

    // ---------------- 旧版本兼容路径解析 ----------------

    private static class LegacyPathResolver {
        public static String getPathFromUri(Context context, Uri uri) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (columnIndex >= 0) return cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
