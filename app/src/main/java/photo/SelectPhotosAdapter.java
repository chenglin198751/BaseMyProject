package photo;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import base.MyApplication;
import cheerly.mybaseproject.R;
import utils.Constants;
import utils.MyUtils;
import utils.SDCardUtils;
import view.WebImageView;
import widget.MyToast;

/**
 * weichenglin create in 16/4/11
 */
public class SelectPhotosAdapter extends RecyclerView.Adapter<SelectPhotosAdapter.ViewHolder> {
    public static final int REQUEST_TAKE_PICTURE = 2;
    private static final int TAKE_PHOTO = 0;
    private static final int ALBUM_LIST = 1;
    private SelectPhotosActivity mActivity;
    private List<PhotoAlbum> mPhotoAlbumList = new ArrayList<PhotoAlbum>();
    private PhotoAlbum mSelectedPhotoItem;
    private String mPicPath;
    ArrayList<String> mSelectedList = new ArrayList<String>();
    private View.OnClickListener imgClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag(R.id.publish_picture_id) != null) {
                int position = (int) view.getTag(R.id.publish_picture_id);
//                if (mActivity.isMultipleSelect){
//                    String path = mPhotoAlbumList.get(position).getPath();
//                    if (!mSelectedList.contains(path)){
//                        if (mActivity.mMaxCount > 0 && mSelectedList.size() == mActivity.mMaxCount ){
//                            String maxLimit = mActivity.getString(R.string.publish_max_selected_photo_limit);
//                            maxLimit = maxLimit.replace("X",mActivity.mMaxCount + "");
//                            MyToast.show(maxLimit);
//                            return;
//                        }
//                        mSelectedList.add(path);
//                    }else {
//                        mSelectedList.remove(path);
//                    }
//                    notifyDataSetChanged();
//                }else

                if (mSelectedPhotoItem == null || mSelectedPhotoItem != mPhotoAlbumList.get(position)) {
                    mSelectedPhotoItem = mPhotoAlbumList.get(position);
                    notifyDataSetChanged();
                }
            }
        }
    };

    public SelectPhotosAdapter(SelectPhotosActivity context) {
        mActivity = context;
    }

    public void setData(List<PhotoAlbum> list) {
        mPhotoAlbumList = list;
        notifyDataSetChanged();
    }

    public void setSelectedPhotoItem(PhotoAlbum item) {
        mSelectedPhotoItem = item;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TAKE_PHOTO;
        } else {
            return ALBUM_LIST;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = null;
        if (viewType == TAKE_PHOTO) {
            itemView = View.inflate(mActivity, R.layout.publish_select_photo_take_photo_item, null);
        } else {
            itemView = View.inflate(mActivity, R.layout.publish_select_photo_item, null);
        }

        ViewHolder viewHolder = new ViewHolder(itemView);
        viewHolder.setViewType(viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        int viewType = getItemViewType(position);
        if (viewType == TAKE_PHOTO) {
            viewHolder.takePhoto.setTag(R.id.take_photo, position);
            viewHolder.takePhoto.setOnClickListener(imgClickListener);
        } else if (viewType == ALBUM_LIST) {
            PhotoAlbum photoItem = mPhotoAlbumList.get(position);

            viewHolder.photoImg.setTag(R.id.publish_picture_id, position);
            viewHolder.photoImg.setOnClickListener(imgClickListener);

            int width = MyUtils.dip2px(100f);
            Picasso.with(MyApplication.getApp())
                    .load(new File(photoItem.getPath()))
                    .resize(width, width)
                    .centerCrop()
                    .into(viewHolder.photoImg);

//            if (mActivity.isMultipleSelect){
//                if (mSelectedList.contains(photoItem.getPath())){
//                    viewHolder.checkBox.setVisibility(View.VISIBLE);
//                }else {
//                    viewHolder.checkBox.setVisibility(View.GONE);
//                }
//            }else

            if (mSelectedPhotoItem != null && photoItem == mSelectedPhotoItem) {
                viewHolder.checkBox.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkBox.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoAlbumList.size();
    }

    public void goCameraPick() {
//        try {
//            mPicPath = getImagePath();
//            Intent intent = new Intent(mActivity, CameraNewActivity.class);
//            intent.putExtra("path", mPicPath);
//            intent.putExtra("isFromPhoto", true);
//            intent.putExtra("isNotCropAndFilter", true);
//            mActivity.startActivityForResult(intent, REQUEST_TAKE_PICTURE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private String getImagePath() {
        String path = SDCardUtils.SDCARD_PATH;
        return path + System.currentTimeMillis() + ".jpeg";
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (mActivity.RESULT_OK == resultCode && REQUEST_TAKE_PICTURE == requestCode) {
//            PublishPhotosPreviewAct.startForTakePhotoPreview(mActivity, mPicPath);
//        } else if (mActivity.RESULT_OK == resultCode && PublishPhotosPreviewAct.OPEN_TAKE_PHOTO_PREVIEW == requestCode) {
//            mPicPath = data.getStringExtra("photo_path");
//            if (mActivity.isMultipleSelect){
//                mSelectedList.add(mPicPath);
//                mActivity.mHelper.returnMultipleSelect();
//            }else {
//                mSelectedPhotoItem.setPath(mPicPath);
//                mActivity.setShowSelectedPhoto(mSelectedPhotoItem);
//                mActivity.setSelectedPhotoAlbumIsNull();
//                notifyDataSetChanged();
//            }
//        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        int mItemSize = -1;
        WebImageView photoImg;
        ImageView takePhoto;
        ImageView checkBox;
        private int mViewType;

        public ViewHolder(View v) {
            super(v);
        }

        public void setViewType(int viewType) {
            mViewType = viewType;
            initViewHolder();
        }

        private void initViewHolder() {
            if (mViewType == TAKE_PHOTO) {
                takePhoto = (ImageView) itemView.findViewById(R.id.take_photo);

                takePhoto.getLayoutParams().width = getItemSize();
                takePhoto.getLayoutParams().height = getItemSize();
            } else if (mViewType == ALBUM_LIST) {
                photoImg = (WebImageView) itemView.findViewById(R.id.photo_img);
                checkBox = (ImageView) itemView.findViewById(R.id.check_box);

                photoImg.getLayoutParams().width = getItemSize();
                photoImg.getLayoutParams().height = getItemSize();

                checkBox.getLayoutParams().width = getItemSize();
                checkBox.getLayoutParams().height = getItemSize();
            }
        }

        private int getItemSize() {
            if (mItemSize < 0) {
                mItemSize = Constants.screenWidth / SelectPhotosActivity.GRID_COLUMN;
            }

            return mItemSize;
        }

    }

}