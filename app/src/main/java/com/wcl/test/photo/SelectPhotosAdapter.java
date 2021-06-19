package com.wcl.test.photo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.wcl.test.R;
import com.wcl.test.base.BaseAction;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.Constants;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.utils.SDCardUtils;
import com.wcl.test.widget.ToastUtils;

/**
 * weichenglin create in 16/4/11
 */
public class SelectPhotosAdapter extends RecyclerView.Adapter<SelectPhotosAdapter.ViewHolder> {
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
            if (view.getTag(R.id.take_photo) != null) {
                mPicPath = openCamera();
            } else if (view.getTag(R.id.publish_picture_id) != null) {
                int position = (int) view.getTag(R.id.publish_picture_id);
                if (!mActivity.isSingleType) {
                    String path = mPhotoAlbumList.get(position).getPath();
                    if (!mSelectedList.contains(path)) {
                        if (mActivity.mCount > 0 && mSelectedList.size() == mActivity.mCount) {
                            String maxLimit = mActivity.getString(R.string.publish_max_selected_photo_limit);
                            maxLimit = maxLimit.replace("X", mActivity.mCount + "");
                            ToastUtils.show(maxLimit);
                            return;
                        }
                        mSelectedList.add(path);
                    } else {
                        mSelectedList.remove(path);
                    }
//                    notifyDataSetChanged();
                    notifyItemChanged(position);
                } else if (mSelectedPhotoItem == null || mSelectedPhotoItem != mPhotoAlbumList.get(position)) {
                    mSelectedPhotoItem = mPhotoAlbumList.get(position);
//                    notifyDataSetChanged();
                    notifyItemChanged(position);
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

    public ArrayList<String> getSelectedPhotoList() {
        if (mActivity.isSingleType) {
            if (mSelectedPhotoItem != null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(mSelectedPhotoItem.getPath());
                return list;
            } else {
                return null;
            }
        } else {
            return mSelectedList;
        }
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
            itemView = LayoutInflater.from(mActivity).inflate(R.layout.publish_select_photo_take_photo_item, viewGroup, false);
        } else {
            itemView = LayoutInflater.from(mActivity).inflate(R.layout.publish_select_photo_item, viewGroup, false);
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

            int width = BaseUtils.dip2px(100f);
            SmartImageLoader.load(viewHolder.photoImg, new File(photoItem.getPath()), width, width, 0);

            if (!mActivity.isSingleType) {
                if (mSelectedList.contains(photoItem.getPath())) {
                    viewHolder.checkBox.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.checkBox.setVisibility(View.GONE);
                }
            } else {
                if (mSelectedPhotoItem != null && photoItem == mSelectedPhotoItem) {
                    viewHolder.checkBox.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.checkBox.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoAlbumList.size();
    }

    public String openCamera() {
        String photoPath = getImagePath();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoPath)));
        mActivity.startActivityForResult(cameraIntent, TAKE_PHOTO);
        return photoPath;
    }

    private String getImagePath() {
        String path = SDCardUtils.getDataPath();
        return path + System.currentTimeMillis() + ".jpg";
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK == resultCode && TAKE_PHOTO == requestCode) {
            if (!TextUtils.isEmpty(mPicPath)) {
                ArrayList<String> list = new ArrayList<>();
                list.add(mPicPath);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.KEY_PHOTO_LIST, list);
                BaseAction.sendBroadcast(Constants.ACTION_GET_PHOTO_LIST, bundle);
                mActivity.finish();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        int mItemSize = -1;
        ImageView photoImg;
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
                takePhoto = itemView.findViewById(R.id.take_photo);

                takePhoto.getLayoutParams().width = getItemSize();
                takePhoto.getLayoutParams().height = getItemSize();
            } else if (mViewType == ALBUM_LIST) {
                photoImg = itemView.findViewById(R.id.photo_img);
                checkBox = itemView.findViewById(R.id.check_box);

                photoImg.getLayoutParams().width = getItemSize();
                photoImg.getLayoutParams().height = getItemSize();

                checkBox.getLayoutParams().width = getItemSize();
                checkBox.getLayoutParams().height = getItemSize();
            }
        }

        private int getItemSize() {
            if (mItemSize < 0) {
                mItemSize = Constants.getScreenWidth() / SelectPhotosActivity.GRID_COLUMN;
            }

            return mItemSize;
        }

    }

}