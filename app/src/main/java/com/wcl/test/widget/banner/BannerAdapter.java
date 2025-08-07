package com.wcl.test.widget.banner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wcl.test.GlideApp;
import com.wcl.test.R;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.view.round.RoundedImageView;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<String> imageList;

    public BannerAdapter(List<String> imageList) {
        this.imageList = imageList;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView imageView;

        public BannerViewHolder(View view) {
            super(view);
            this.view = view;
            this.imageView = view.findViewById(R.id.image_view);
        }
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int realPosition = position % imageList.size();
        String imageUrl = imageList.get(realPosition);
        GlideApp.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}
