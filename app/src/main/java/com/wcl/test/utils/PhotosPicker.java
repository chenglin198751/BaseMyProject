package com.wcl.test.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

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
        ActivityResultLauncher<Intent> launcher =
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        resolveUriAsync(activity.getApplicationContext(), uri, listener);
                    }
                });
        return new PhotosPicker(activity, launcher, listener);
    }

    /**
     * 创建 Fragment 版本的 Picker
     */
    public static PhotosPicker from(@NonNull Fragment fragment, @NonNull OnFinishedListener2<String> listener) {
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        resolveUriAsync(fragment.requireContext().getApplicationContext(), uri, listener);
                    }
                });
        return new PhotosPicker(fragment.requireContext(), launcher, listener);
    }

    /**
     * 启动选择器
     */
    public void start() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickerLauncher.launch(intent);
    }

    /**
     * 异步解析 URI 为本地路径
     */
    private static void resolveUriAsync(Context ctx, Uri uri, OnFinishedListener2<String> listener) {
        new Thread(() -> {
            String path = UriPathResolver.getPath(ctx, uri);
            if (listener != null) {
                listener.onFinished(path);
            }
        }).start();
    }

    // ---------------- 内部类：路径解析 ----------------

    public static class UriPathResolver {

        /**
         * 获取图片的本地路径（兼容 Android 10+）
         */
        public static String getPath(Context context, Uri uri) {
            if (uri == null) return null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return copyUriToCache(context, uri);
            } else {
                return LegacyPathResolver.getPathFromUri(context, uri);
            }
        }

        private static String copyUriToCache(Context context, Uri uri) {
            File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (cacheDir == null) cacheDir = context.getCacheDir();
            File destFile = new File(cacheDir, "IMG_" + System.currentTimeMillis() + ".jpg");

            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                return destFile.getAbsolutePath();
            } catch (Exception e) {
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
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
