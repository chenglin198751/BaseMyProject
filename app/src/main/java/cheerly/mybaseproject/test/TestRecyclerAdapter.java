package cheerly.mybaseproject.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.utils.BaseUtils;
import cheerly.mybaseproject.utils.SmartImageLoader;

public class TestRecyclerAdapter extends BaseRecyclerViewAdapter<String> {

    private Context mContext;

    public TestRecyclerAdapter(Context context) {
        this.mContext = context;
    }


    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_2, parent, false);
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
        private String url = "http://5b0988e595225.cdn.sohucs.com/images/20170922/c7e95cf930a64a27b616e8c77525645b.jpeg";
        ImageView imageView;
        TextView title;

        public ListHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
        }

        @Override
        public void onBind(int position) {
            SmartImageLoader.getInstance().loadRound(imageView, url, BaseUtils.dip2px(50f), BaseUtils.dip2px(50f), 15);
            title.setText("标题 " + getData().get(position));
        }

    }
}