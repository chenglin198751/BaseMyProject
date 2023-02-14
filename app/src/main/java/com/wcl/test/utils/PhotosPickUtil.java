package com.wcl.test.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.wcl.test.base.MainApp;
import com.wcl.test.listener.OnCompressBitmapListener;
import com.wcl.test.listener.OnFinishedListener;

public class PhotosPickUtil {
    private final static int REQUEST_CODE = 10098;
    private static OnFinishedListener<Bitmap, String> mListener = null;

    public static void pickPhoto(Activity activity, OnFinishedListener<Bitmap, String> listener) {
        mListener = listener;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static void pickPhoto(Fragment fragment, OnFinishedListener<Bitmap, String> listener) {
        mListener = listener;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }


    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mListener != null && requestCode == REQUEST_CODE) {
            Uri uri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(MainApp.getApp().getContentResolver().openInputStream(uri));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                Bitmap bitmap2 = bitmap;
                BitmapUtils.saveBitmap(bitmap, new OnCompressBitmapListener<String>() {
                    @Override
                    public void onSucceed(String path) {
                        AppLogUtils.v("tag_2", "bitmap path=" + path);
                        mListener.onFinished(bitmap2, path);
                    }
                });
            } else {
                mListener.onFinished(null, null);
            }
        }
    }
}
