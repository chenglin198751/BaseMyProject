package com.wcl.test.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

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
import java.util.UUID;

/**
 * 完整版图片选择/拍照/裁剪工具
 * 统一封装 ActivityResult API，支持 Activity/Fragment。
 */
public class PhotoPicker2 {

    private static final String FILE_PROVIDER_SUFFIX = ".custom.file_provider";

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
    private Uri cropOutputUri; // 裁剪输出文件URI
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
        Context appContext = activity.getApplicationContext();
        PhotoPicker2[] pickerHolder = new PhotoPicker2[1];
        ActivityResultLauncher<Intent> launcher =
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    PhotoPicker2 picker = pickerHolder[0];
                    Uri outputUri = picker == null ? null : picker.getOutputUri();
                    handleResult(appContext, result.getData(), outputUri, listener);
                });
        pickerHolder[0] = new PhotoPicker2(activity, launcher, listener);
        return pickerHolder[0];
    }

    public static PhotoPicker2 from(@NonNull Fragment fragment, @NonNull OnFinishedListener2<List<String>> listener) {
        Context appContext = fragment.requireContext().getApplicationContext();
        PhotoPicker2[] pickerHolder = new PhotoPicker2[1];
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    PhotoPicker2 picker = pickerHolder[0];
                    Uri outputUri = picker == null ? null : picker.getOutputUri();
                    handleResult(appContext, result.getData(), outputUri, listener);
                });
        pickerHolder[0] = new PhotoPicker2(fragment.requireContext(), launcher, listener);
        return pickerHolder[0];
    }

    // ---------- 图片选择 ----------

    public void pickSingle() {
        tempCameraUri = null;
        cropOutputUri = null;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        launcher.launch(intent);
    }

    public void pickMultiple() {
        tempCameraUri = null;
        cropOutputUri = null;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        launcher.launch(Intent.createChooser(intent, "选择图片"));
    }

    // ---------- 拍照 ----------

    public void capture(Activity activity) {
        cropOutputUri = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createTempImageFile(context);
        tempCameraUri = getProviderUri(photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
        addUriGrants(intent, tempCameraUri);
        launcher.launch(intent);
    }

    // ---------- 裁剪 ----------

    public void crop(@NonNull Uri srcUri, int aspectX, int aspectY) {
        tempCameraUri = null;
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
        cropOutputUri = getProviderUri(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropOutputUri);
        intent.putExtra("return-data", false);
        addUriGrants(intent, srcUri, cropOutputUri);

        launcher.launch(intent);
    }

    // ---------- 结果处理 ----------

    private Uri getOutputUri() {
        return cropOutputUri != null ? cropOutputUri : tempCameraUri;
    }

    private static void handleResult(Context ctx, Intent data, Uri outputUri,
                                     OnFinishedListener2<List<String>> listener) {
        AppThreadPoolExecutor.getExecutor().execute(() -> {
            List<String> resultPaths = new ArrayList<>();
            boolean outputResolved = outputUri != null && addResolvedPath(resultPaths, ctx, outputUri);
            if (!outputResolved && data != null && data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    addResolvedPath(resultPaths, ctx, data.getClipData().getItemAt(i).getUri());
                }
            } else if (!outputResolved && data != null && data.getData() != null) {
                addResolvedPath(resultPaths, ctx, data.getData());
            }
            AppUtils.getUiHandler().post(() -> {
                if (listener != null) listener.onFinished(resultPaths);
            });
        });
    }

    private static boolean addResolvedPath(List<String> paths, Context context, Uri uri) {
        String path = UriPathResolver.getPath(context, uri);
        if (!TextUtils.isEmpty(path) && !paths.contains(path)) {
            paths.add(path);
            return true;
        }
        return false;
    }

    private Uri getProviderUri(File file) {
        Context appContext = context.getApplicationContext();
        return FileProvider.getUriForFile(appContext,
                appContext.getPackageName() + FILE_PROVIDER_SUFFIX, file);
    }

    private static void addUriGrants(Intent intent, Uri... uris) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ClipData clipData = null;
        for (Uri uri : uris) {
            if (uri == null) continue;
            if (clipData == null) {
                clipData = ClipData.newRawUri("image", uri);
            } else {
                clipData.addItem(new ClipData.Item(uri));
            }
        }
        if (clipData != null) intent.setClipData(clipData);
    }

    // ---------- 辅助方法 ----------

    private static File createTempImageFile(Context context) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) dir = context.getCacheDir();
        if (!dir.exists() && !dir.mkdirs()) {
            dir = context.getCacheDir();
        }
        return new File(dir, "IMG_" + UUID.randomUUID() + ".jpg");
    }

    // ---------- 内部类：路径解析 ----------

    public static class UriPathResolver {
        public static String getPath(Context context, Uri uri) {
            if (uri == null) return null;
            if ("file".equalsIgnoreCase(uri.getScheme())) return uri.getPath();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                String path = LegacyResolver.getPathFromUri(context, uri);
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

        private static class LegacyResolver {
            public static String getPathFromUri(Context context, Uri uri) {
                String[] projection = {MediaStore.Images.Media.DATA};
                try (android.database.Cursor cursor = context.getContentResolver()
                        .query(uri, projection, null, null, null)) {
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
}
