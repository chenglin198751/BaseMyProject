package com.wcl.test.helper;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.view.image.GlideBorderImageView;
import com.youth.banner.adapter.BannerAdapter;

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
        GlideBorderImageView imageView = new GlideBorderImageView(parent.getContext());
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
        GlideBorderImageView imageView;

        public BannerViewHolder(@NonNull GlideBorderImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}

