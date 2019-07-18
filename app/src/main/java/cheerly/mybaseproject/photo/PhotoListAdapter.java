package cheerly.mybaseproject.photo;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;
import cheerly.mybaseproject.utils.SmartImageLoader;

/**
 * @title: PhotoListAdappter.java
 * @description: 相册列表Adapter
 * @company: 美丽说（北京）网络科技有限公司
 * @author: tinglongyu
 * @version: 6.5.0
 * @created：2015年6月12日
 */
public class PhotoListAdapter extends BaseAdapter {
    protected Context mContext;
    private List<PhotoAlbum> mPhotoAlbums;

    public PhotoListAdapter(Context context, List<PhotoAlbum> mPhotoAlbums) {
        this.mPhotoAlbums = mPhotoAlbums;
        mContext = context;
    }

    @Override
    public int getCount() {
        int mCount = (mPhotoAlbums == null) ? 0 : mPhotoAlbums.size();
        return mCount;
    }

    @Override
    public PhotoAlbum getItem(int position) {
        return (mPhotoAlbums == null) ? null : mPhotoAlbums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.publish_photo_list_item, null);
            ViewHolder holder = new ViewHolder();
            holder.mImageView = convertView.findViewById(R.id.photo_img_view);
            holder.mAlbumName = convertView.findViewById(R.id.photo_name);
            holder.mAlbumNum = convertView.findViewById(R.id.photo_num);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        PhotoAlbum photoItem = mPhotoAlbums.get(position);

        if (photoItem != null && !TextUtils.isEmpty(photoItem.getPath())) {
            int width = BaseUtils.dip2px(100);
            SmartImageLoader.getInstance().load(holder.mImageView, new File(photoItem.getPath()), width, width);

        }
        holder.mAlbumName.setText(photoItem.getName());
        holder.mAlbumNum.setText(photoItem.getCount() + "张");
        return convertView;
    }

    class ViewHolder {
        ImageView mImageView;
        TextView mAlbumName;
        TextView mAlbumNum;
    }
}
