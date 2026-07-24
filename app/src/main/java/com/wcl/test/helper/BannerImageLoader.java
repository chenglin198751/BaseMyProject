package com.wcl.test.helper;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.view.image.GlideImageView;
import com.wcl.test.banner.adapter.BannerAdapter;

import java.util.List;

/**
 * Created by chenglin on 2017-9-27.
 */
public class BannerImageLoader extends BannerAdapter<String, BannerImageLoader.BannerViewHolder> {

    public BannerImageLoader(List<String> mDatas) {
        super(mDatas);
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        GlideImageView imageView = new GlideImageView(parent.getContext());
        imageView.setCornerRadius(8);
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setAspectRatio(2);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String url, int position, int size) {
        holder.imageView.loadImage(url);
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        GlideImageView imageView;

        public BannerViewHolder(@NonNull GlideImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}

