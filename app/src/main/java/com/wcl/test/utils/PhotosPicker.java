package com.wcl.test.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.wcl.test.listener.OnFinishedListener2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PhotosPicker {
    private final static int REQUEST_CODE = 10098;
    private OnFinishedListener2<String> mListener = null;
    private Context mContext;

    public PhotosPicker(Activity activity, OnFinishedListener2<String> listener) {
        mContext = activity;
        mListener = listener;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public PhotosPicker(Fragment fragment, OnFinishedListener2<String> listener) {
        mContext = fragment.getContext();
        mListener = listener;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 根据uri获取Bitmap
        // Bitmap bitmap = BitmapFactory.decodeStream(MainApp.getApp().getContentResolver().openInputStream(uri));

        if (mListener != null && requestCode == REQUEST_CODE) {
            Uri uri = data.getData();
            String path = ImagePathUtil.getImageAbsolutePath(mContext, uri);
            mListener.onFinished(path);
        }
    }


    public static class ImagePathUtil {

        public static String getImageAbsolutePath(Context context, Uri uri) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return getPathForAndroidQ(context, uri);
            } else {
                return getPathFromUri(context, uri);
            }
        }

        private static String getPathFromUri(Context context, Uri uri) {
            String[] projection = {MediaStore.Images.Media.DATA};
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(columnIndex);
                cursor.close();
                return path;
            }
            return null;
        }

        private static String getPathForAndroidQ(Context context, Uri uri) {
            File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(cacheDir, System.currentTimeMillis() + ".jpg");
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return file.getAbsolutePath();
            } catch (IOException e) {
                Toast.makeText(context, "获取路径失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
    }
}
