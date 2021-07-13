package com.wcl.test.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.ImageLoaderBuilder;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.utils.PhotosSelectedUtil;

public class TestSelectedPhotoActivity extends BaseActivity {

    private TextView tvPhoto;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_selected_photo_layout);

        tvPhoto = findViewById(R.id.tv_photo);
        image = findViewById(R.id.image);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotosSelectedUtil.startPickLocaleImage(getContext());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PhotosSelectedUtil.REQUEST_CODE) {
            String path = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    // 根据返回的URI获取对应的SQLite信息
                    Uri uri = data.getData();
                    path = PhotosSelectedUtil.getPath(this, uri);
                }
            }
            if (path != null) {
                tvPhoto.setText(path);

                ImageLoaderBuilder builder = new ImageLoaderBuilder();
                builder.setUri(path);
                builder.setImageWidth(BaseUtils.dip2px(400));
                builder.setImageHeight(BaseUtils.dip2px(400));
                builder.setScaleType(ImageLoaderBuilder.CENTER_INSIDE);
                SmartImageLoader.load(image,builder);
            } else {
                Toast.makeText(getContext(),"图片选择失败,请检查是否有SD卡权限，并重新选择图片",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
