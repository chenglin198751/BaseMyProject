package com.wcl.test.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wcl.test.R;
import com.wcl.test.base.BaseRecyclerViewAdapter;
import com.wcl.test.base.BaseRecyclerViewHolder;
import com.wcl.test.view.image.GlideImageView;

public class TestRecyclerAdapter extends BaseRecyclerViewAdapter<String> {

    private Context mContext;

    public TestRecyclerAdapter(Context context) {
        this.mContext = context;
    }


    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_4, parent, false);
        return new ListHolder(view);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        if (holder instanceof ListHolder) {
            ListHolder listHolder = (ListHolder) holder;
            listHolder.onBind(position);
        }

    }

    private class ListHolder extends BaseRecyclerViewHolder {
        GlideImageView imageView;
        TextView title;

        public ListHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
        }

        @Override
        public void onBind(int position) {
            imageView.loadImage(TestUrls.ImgUrls.get(position));
            title.setText("标题 " + getData().get(position));
        }

    }
}