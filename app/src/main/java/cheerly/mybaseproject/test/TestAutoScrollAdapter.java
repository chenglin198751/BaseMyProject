package cheerly.mybaseproject.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cheerly.mybaseproject.R;

/**
 * Created by weichenglin  on 2018/6/22
 */
public class TestAutoScrollAdapter extends RecyclerView.Adapter<TestAutoScrollAdapter.BaseViewHolder> {
    private final Context mContext;
    private final List<String> mData;

    public TestAutoScrollAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mData = list;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.test_viewholder_auto, parent, false);
        BaseViewHolder holder = new BaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        String data = mData.get(position % mData.size());
        holder.setText(data);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }


    class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        private void setText(String text) {
            TextView tvContent = itemView.findViewById(R.id.tv_content);
            tvContent.setText(text);
        }
    }
}
