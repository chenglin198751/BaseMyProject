package com.wcl.test.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder {
    public BaseRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void onBind(@NonNull T t, int position);
}
