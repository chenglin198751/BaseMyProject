package com.wcl.test.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.wcl.test.listener.OnFinishedListener2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 完整版图片选择/拍照/裁剪工具
 * 统一封装 ActivityResult API，支持 Activity/Fragment。
 */
public class PhotoPicker2 {

    public enum Mode {
        PICK_SINGLE,  // 选单张
        PICK_MULTI,   // 选多张
        CAPTURE,      // 拍照
        CROP          // 裁剪（需传入源URI）
    }

    private final Context context;
    private final ActivityResultLauncher<Intent> launcher;
    private final OnFinishedListener2<List<String>> listener;

    private Uri tempCameraUri; // 拍照临时文件
    private Uri cropSrcUri;    // 待裁剪的图片URI
    private int cropWidth = 1, cropHeight = 1; // 裁剪比例

    private PhotoPicker2(@NonNull Context ctx,
                         @NonNull ActivityResultLauncher<Intent> launcher,
                         @NonNull OnFinishedListener2<List<String>> listener) {
        this.context = ctx;
        this.launcher = launcher;
        this.listener = listener;
    }

    // ---------- 创建入口 ----------

    public static PhotoPicker2 from(@NonNull AppCompatActivity activity, @NonNull OnFinishedListener2<List<String>> listener) {
        ActivityResultLauncher<Intent> launcher =
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                    handleResult(activity.getApplicationContext(), result.getData(), listener);
                });
        return new PhotoPicker2(activity, launcher, listener);
    }

    public static PhotoPicker2 from(@NonNull Fragment fragment, @NonNull OnFinishedListener2<List<String>> listener) {
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                    handleResult(fragment.requireContext().getApplicationContext(), result.getData(), listener);
                });
        return new PhotoPicker2(fragment.requireContext(), launcher, listener);
    }

    // ---------- 图片选择 ----------

    public void pickSingle() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    public void pickMultiple() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launcher.launch(Intent.createChooser(intent, "选择图片"));
    }

    // ---------- 拍照 ----------

    public void capture(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createTempImageFile(context);
        tempCameraUri = FileProvider.getUriForFile(context, activity.getPackageName() + ".fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
        launcher.launch(intent);
    }

    // ---------- 裁剪 ----------

    public void crop(@NonNull Uri srcUri, int aspectX, int aspectY) {
        cropSrcUri = srcUri;
        cropWidth = aspectX;
        cropHeight = aspectY;

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("scale", true);

        File outputFile = createTempImageFile(context);
        Uri outputUri = Uri.fromFile(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("return-data", false);

        launcher.launch(intent);
    }

    // ---------- 结果处理 ----------

    private static void handleResult(Context ctx, Intent data, OnFinishedListener2<List<String>> listener) {
        new Thread(() -> {
            List<String> resultPaths = new ArrayList<>();

            if (data.getData() != null) {
                // 单选
                Uri uri = data.getData();
                String path = UriPathResolver.getPath(ctx, uri);
                if (path != null) resultPaths.add(path);
            } else if (data.getClipData() != null) {
                // 多选
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    String path = UriPathResolver.getPath(ctx, uri);
                    if (path != null) resultPaths.add(path);
                }
            }

            if (listener != null) listener.onFinished(resultPaths);
        }).start();
    }

    // ---------- 辅助方法 ----------

    private static File createTempImageFile(Context context) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) dir = context.getCacheDir();
        return new File(dir, "IMG_" + System.currentTimeMillis() + ".jpg");
    }

    // ---------- 内部类：路径解析 ----------

    public static class UriPathResolver {
        public static String getPath(Context context, Uri uri) {
            if (uri == null) return null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return copyUriToCache(context, uri);
            } else {
                return LegacyResolver.getPathFromUri(context, uri);
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

        private static class LegacyResolver {
            public static String getPathFromUri(Context context, Uri uri) {
                String[] projection = {MediaStore.Images.Media.DATA};
                try (android.database.Cursor cursor = context.getContentResolver()
                        .query(uri, projection, null, null, null)) {
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
}
