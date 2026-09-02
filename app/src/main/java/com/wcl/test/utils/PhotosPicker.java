package com.wcl.test.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.wcl.test.listener.OnFinishedListener2;

/**
 * 图片选择器（现代实现）
 * 支持 Activity 和 Fragment，自动返回图片绝对路径
 */
public class PhotosPicker {

    private final OnFinishedListener2<String> listener;
    private final ActivityResultLauncher<Intent> pickerLauncher;

    private PhotosPicker(@NonNull ActivityResultLauncher<Intent> launcher,
                         @NonNull OnFinishedListener2<String> listener) {
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
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    resolveUriAsync(appContext, getResultUri(result), listener);
                });
        return new PhotosPicker(launcher, listener);
    }

    /**
     * 创建 Fragment 版本的 Picker
     */
    public static PhotosPicker from(@NonNull Fragment fragment, @NonNull OnFinishedListener2<String> listener) {
        Context appContext = fragment.requireContext().getApplicationContext();
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    resolveUriAsync(appContext, getResultUri(result), listener);
                });
        return new PhotosPicker(launcher, listener);
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
            String path = PhotoPicker2.UriPathResolver.getPath(ctx, uri);
            AppUtils.getUiHandler().post(() -> {
                if (listener != null) listener.onFinished(path);
            });
        });
    }

    private static Uri getResultUri(ActivityResult result) {
        if (result == null || result.getData() == null) return null;
        Intent data = result.getData();
        if (data.getData() != null) return data.getData();
        ClipData clipData = data.getClipData();
        if (clipData != null && clipData.getItemCount() > 0) {
            return clipData.getItemAt(0).getUri();
        }
        return null;
    }

    // ---------------- 内部类：路径解析 ----------------

    public static class UriPathResolver {

        /**
         * 获取图片的本地路径（兼容 Android 10+）
         */
        public static String getPath(Context context, Uri uri) {
            return PhotoPicker2.UriPathResolver.getPath(context, uri);
        }
    }

    // ---------------- 旧版本兼容路径解析 ----------------
}
