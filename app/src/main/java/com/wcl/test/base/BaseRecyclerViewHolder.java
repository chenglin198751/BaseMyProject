package com.wcl.test.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {
    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind(int position);
}