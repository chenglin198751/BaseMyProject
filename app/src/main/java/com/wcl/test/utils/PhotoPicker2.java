package com.wcl.test.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

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
import java.util.Locale;
import java.util.UUID;

/**
 * 完整版图片选择/拍照/裁剪工具
 * 统一封装 ActivityResult API，支持 Activity/Fragment。
 */
public class PhotoPicker2 {

    private static final String FILE_PROVIDER_SUFFIX = ".custom.file_provider";
    private static final String CROP_ACTION = "com.android.camera.action.CROP";
    private static final int CROP_OUTPUT_SIZE = 800;

    public enum Mode {
        PICK_SINGLE,  // 选单张
        PICK_MULTI,   // 选多张
        CAPTURE,      // 拍照
        CROP          // 裁剪（需传入源URI）
    }

    private final Context context;
    private final ActivityResultLauncher<Intent> launcher;
    private final PendingOperation pendingOperation;

    private PhotoPicker2(@NonNull Context ctx,
                         @NonNull ActivityResultLauncher<Intent> launcher,
                         @NonNull PendingOperation pendingOperation) {
        this.context = ctx.getApplicationContext();
        this.launcher = launcher;
        this.pendingOperation = pendingOperation;
    }

    /**
     * 保存拍照/裁剪临时文件，结果回调时优先使用 EXTRA_OUTPUT 对应的文件。
     */
    private static final class PendingOperation {
        private File outputFile;

        synchronized void setOutputFile(File file) {
            outputFile = file;
        }

        synchronized File takeOutputFile() {
            File file = outputFile;
            outputFile = null;
            return file;
        }

        synchronized void cancel() {
            File file = outputFile;
            outputFile = null;
            deleteQuietly(file);
        }
    }

    // ---------- 创建入口 ----------

    public static PhotoPicker2 from(@NonNull AppCompatActivity activity,
                                    @NonNull OnFinishedListener2<List<String>> listener) {
        Context appContext = activity.getApplicationContext();
        PendingOperation pendingOperation = new PendingOperation();
        ActivityResultLauncher<Intent> launcher =
                activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    File outputFile = pendingOperation.takeOutputFile();
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        deleteQuietly(outputFile);
                        return;
                    }
                    handleResult(appContext, result.getData(), outputFile, listener);
                });
        return new PhotoPicker2(appContext, launcher, pendingOperation);
    }

    public static PhotoPicker2 from(@NonNull Fragment fragment,
                                    @NonNull OnFinishedListener2<List<String>> listener) {
        Context appContext = fragment.requireContext().getApplicationContext();
        PendingOperation pendingOperation = new PendingOperation();
        ActivityResultLauncher<Intent> launcher =
                fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    File outputFile = pendingOperation.takeOutputFile();
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        deleteQuietly(outputFile);
                        return;
                    }
                    handleResult(appContext, result.getData(), outputFile, listener);
                });
        return new PhotoPicker2(appContext, launcher, pendingOperation);
    }

    // ---------- 图片选择 ----------

    public void pickSingle() {
        pendingOperation.cancel();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        launcher.launch(intent);
    }

    public void pickMultiple() {
        pendingOperation.cancel();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        launcher.launch(Intent.createChooser(intent, "选择图片"));
    }

    // ---------- 拍照 ----------

    public void capture(@NonNull Activity activity) {
        pendingOperation.cancel();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            throw new ActivityNotFoundException("No camera application is available");
        }

        File photoFile = createTempImageFile(context);
        pendingOperation.setOutputFile(photoFile);
        try {
            Uri cameraUri = getProviderUri(photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            addUriGrants(intent, cameraUri);
            launcher.launch(intent);
        } catch (RuntimeException e) {
            pendingOperation.cancel();
            throw e;
        }
    }

    // ---------- 裁剪 ----------

    public void crop(@NonNull Uri srcUri, int aspectX, int aspectY) {
        if (aspectX <= 0 || aspectY <= 0) {
            throw new IllegalArgumentException("aspectX and aspectY must be greater than zero");
        }
        if ("file".equalsIgnoreCase(srcUri.getScheme())) {
            throw new IllegalArgumentException("crop() requires a content URI, not a file URI");
        }

        pendingOperation.cancel();
        Intent intent = new Intent(CROP_ACTION);
        intent.setDataAndType(srcUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        int outputX;
        int outputY;
        if (aspectX >= aspectY) {
            outputX = CROP_OUTPUT_SIZE;
            outputY = Math.max(1, Math.round(CROP_OUTPUT_SIZE * (float) aspectY / aspectX));
        } else {
            outputX = Math.max(1, Math.round(CROP_OUTPUT_SIZE * (float) aspectX / aspectY));
            outputY = CROP_OUTPUT_SIZE;
        }
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);

        File outputFile = createTempImageFile(context);
        pendingOperation.setOutputFile(outputFile);
        try {
            Uri outputUri = getProviderUri(outputFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra("return-data", false);
            addUriGrants(intent, srcUri, outputUri);
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                throw new ActivityNotFoundException("No image crop application is available");
            }
            launcher.launch(intent);
        } catch (RuntimeException e) {
            pendingOperation.cancel();
            throw e;
        }
    }

    // ---------- 结果处理 ----------

    private static void handleResult(Context ctx, Intent data, File outputFile,
                                     OnFinishedListener2<List<String>> listener) {
        AppThreadPoolExecutor.getExecutor().execute(() -> {
            List<String> resultPaths = new ArrayList<>();
            boolean outputResolved = addFilePath(resultPaths, outputFile);
            if (!outputResolved) {
                deleteQuietly(outputFile);
                if (data != null) {
                    android.content.ClipData clipData = data.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        int count = clipData.getItemCount();
                        for (int i = 0; i < count; i++) {
                            addResolvedPath(resultPaths, ctx, clipData.getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        addResolvedPath(resultPaths, ctx, data.getData());
                    }
                }
            }

            AppUtils.getUiHandler().post(() -> {
                if (listener != null) {
                    listener.onFinished(resultPaths);
                }
            });
        });
    }

    private static boolean addFilePath(List<String> paths, File file) {
        if (file == null || !file.isFile() || file.length() <= 0 || !file.canRead()) {
            return false;
        }
        String path = file.getAbsolutePath();
        if (!paths.contains(path)) {
            paths.add(path);
            return true;
        }
        return false;
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
        return FileProvider.getUriForFile(context,
                context.getPackageName() + FILE_PROVIDER_SUFFIX, file);
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
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            dir = context.getCacheDir();
        }
        if (dir == null || (!dir.exists() && !dir.mkdirs())) {
            throw new IllegalStateException("Unable to create image directory");
        }
        return new File(dir, "IMG_" + UUID.randomUUID() + ".jpg");
    }

    private static void deleteQuietly(File file) {
        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    // ---------- 内部类：路径解析 ----------

    public static class UriPathResolver {
        public static String getPath(Context context, Uri uri) {
            if (context == null || uri == null) return null;

            String scheme = uri.getScheme();
            if ("file".equalsIgnoreCase(scheme)) {
                return getReadablePath(uri.getPath());
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                String path = LegacyResolver.getPathFromUri(context, uri);
                if (!TextUtils.isEmpty(path) && isReadableFile(path)) {
                    return path;
                }
            }
            return copyUriToCache(context, uri);
        }

        private static String copyUriToCache(Context context, Uri uri) {
            File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (cacheDir == null) cacheDir = context.getCacheDir();
            if (cacheDir == null) return null;
            if (!cacheDir.exists() && !cacheDir.mkdirs()) cacheDir = context.getCacheDir();
            if (cacheDir == null || (!cacheDir.exists() && !cacheDir.mkdirs())) return null;

            String extension = getExtension(context, uri);
            File destFile = new File(cacheDir, "IMG_" + UUID.randomUUID() + "." + extension);
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) {
                    deleteQuietly(destFile);
                    return null;
                }
                try (OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                }
                if (!destFile.isFile() || destFile.length() <= 0) {
                    deleteQuietly(destFile);
                    return null;
                }
                return destFile.getAbsolutePath();
            } catch (Exception e) {
                deleteQuietly(destFile);
                e.printStackTrace();
                return null;
            }
        }

        private static String getExtension(Context context, Uri uri) {
            String extension = null;
            try {
                String mimeType = context.getContentResolver().getType(uri);
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            } catch (Exception ignored) {
                // 某些 ContentProvider 不支持查询 MIME 类型，继续从 URI 推断。
            }
            if (TextUtils.isEmpty(extension)) {
                String path = uri.getPath();
                if (!TextUtils.isEmpty(path)) {
                    int dot = path.lastIndexOf('.');
                    if (dot >= 0 && dot < path.length() - 1) {
                        String candidate = path.substring(dot + 1).toLowerCase(Locale.US);
                        if (isSafeExtension(candidate)) extension = candidate;
                    }
                }
            }
            return TextUtils.isEmpty(extension) ? "bin" : extension.toLowerCase(Locale.US);
        }

        private static boolean isSafeExtension(String extension) {
            if (TextUtils.isEmpty(extension) || extension.length() > 10) return false;
            for (int i = 0; i < extension.length(); i++) {
                char c = extension.charAt(i);
                if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9')) return false;
            }
            return true;
        }

        private static String getReadablePath(String path) {
            return isReadableFile(path) ? path : null;
        }

        private static boolean isReadableFile(String path) {
            if (TextUtils.isEmpty(path)) return false;
            File file = new File(path);
            return file.isFile() && file.canRead();
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
