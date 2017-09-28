package photo;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cheerly.mybaseproject.R;
import preferences.PreferencesData;
import utils.Constants;
import utils.MyUtils;
import utils.SDCardUtils;
import widget.MyToast;

/**
 * weichenglin create in 16/4/11
 */
public class SelectPhotosHelper {
    public static final int FINISH_CODE = 123;
    private SelectPhotosActivity mActivity;
    private List<PhotoAlbum> mPhotoAlbumList;
    private PhotoAlbum mCurrentAlbum = null;
    private ListView mPhotoListView;
    private View mPhotoListLayout;
    private View mPhotoListBg;
    private TextView mTitleTextView;
    private PhotoListAdapter mAlbumAdapter;
    private boolean isOpenedList = false;    // 相册选择下拉是否显示中
    private boolean isAnimationing = false;    // 动画是否在运行中

    private AdapterView.OnItemClickListener listviewItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isOpenedList && !isAnimationing) {
                startHideAnimation();
            }

            PhotoAlbum photoAlbum = mAlbumAdapter.getItem(position);

            PreferencesData.setLastAlbumId(photoAlbum.getDirId() + "");
            final PhotoAlbum currentAlbum = LoadImageManager.existDir(mActivity, photoAlbum.getDirId() + "");
            mTitleTextView.setText(photoAlbum.getName());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String mDirID = "";
                    if (currentAlbum != null) {
                        mDirID = currentAlbum.getDirId() + "";
                    }
                    mPhotoAlbumList = LoadImageManager.getPicsForAlbum(mActivity, mDirID, -1);

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PhotoAlbum takePhotoItem = new PhotoAlbum();
                            mPhotoAlbumList.add(0, takePhotoItem);
                            mActivity.mAdapter.setSelectedPhotoItem(currentAlbum);
                            mActivity.mAdapter.setData(mPhotoAlbumList);
                        }
                    });
                }
            }).start();
        }
    };

    public SelectPhotosHelper(SelectPhotosActivity activity) {
        mActivity = activity;
        mPhotoListLayout = mActivity.findViewById(R.id.photo_list_layout);
        mPhotoListView = (ListView) mActivity.findViewById(R.id.album_list);
        mPhotoListBg = mActivity.findViewById(R.id.photo_list_bg);
        mTitleTextView = (TextView) mActivity.findViewById(R.id.title);

//        mPhotoListView.setListViewHeight(maxListHeight);
        mTitleTextView.setText("");

        mPhotoListLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isOpenedList && !isAnimationing) {
                    startHideAnimation();
                }
                return true;
            }
        });
    }

    public void initImageData() {
        mActivity.showProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取所有的相册集合
                final List<PhotoAlbum> photoList = LoadImageManager.getAlbums(mActivity);
                String dirId = PreferencesData.getLastAlbumId();
                if (!TextUtils.isEmpty(dirId)) {  // 获取用户上一次的相册
                    mCurrentAlbum = LoadImageManager.existDir(mActivity, dirId);
                }

                if (mCurrentAlbum == null) { // 获取推荐的相册
                    mCurrentAlbum = LoadImageManager.existDir(mActivity, LoadImageManager.getDefaultDirID(photoList));
                }

                String currentDirID = "-1";
                final String currentDirName;
                if (mCurrentAlbum == null) {
                    currentDirName = mActivity.getResources().getString(R.string.publish_no_photo);
                } else {
                    currentDirID = mCurrentAlbum.getDirId() + "";
                    currentDirName = mCurrentAlbum.getName();
                }

                // 获取某个相册中的图片集合
                mPhotoAlbumList = LoadImageManager.getPicsForAlbum(mActivity, currentDirID, -1);

                //初始化整个相册列表
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity == null || mActivity.isFinishing()) {
                            return;
                        }

                        PhotoAlbum takePhotoItem = new PhotoAlbum();
                        mPhotoAlbumList.add(0, takePhotoItem);
                        mActivity.mAdapter.setData(mPhotoAlbumList);

                        mAlbumAdapter = new PhotoListAdapter(mActivity, photoList);
                        mPhotoListView.setAdapter(mAlbumAdapter);
                        mPhotoListView.setOnItemClickListener(listviewItemClickListener);
                        mTitleTextView.setText(currentDirName);

                        if (mCurrentAlbum == null) { // 无任何相册时
                            mTitleTextView.setClickable(false);
                            mTitleTextView.setCompoundDrawables(null, null, null, null);
                        } else {
                            mTitleTextView.setClickable(true);
                            Drawable imgDrawable = mActivity.getResources().getDrawable(R.mipmap.publish_photo_ico_down);
                            imgDrawable.setBounds(0, 0, imgDrawable.getMinimumWidth(), imgDrawable.getMinimumHeight());
                            mTitleTextView.setCompoundDrawables(null, null, imgDrawable, null);
                        }
                        mActivity.removeProgress();
                    }
                });
            }
        }).start();
    }

    private void startShowAnimation() {
        if (isOpenedList) {
            return;
        }
        mPhotoListLayout.setVisibility(View.VISIBLE);
        mPhotoListView.setVisibility(View.VISIBLE);

        isAnimationing = true;

        Animation mAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.publish_photo_list_in_animation);
        LayoutAnimationController controller = new LayoutAnimationController(mAnimation);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mPhotoListView.startAnimation(mAnimation);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isOpenedList = true;
                isAnimationing = false;

                Drawable imgDrawable = mActivity.getResources().getDrawable(R.mipmap.publish_photo_ico_up);
                imgDrawable.setBounds(0, 0, imgDrawable.getMinimumWidth(), imgDrawable.getMinimumHeight());
                mTitleTextView.setCompoundDrawables(null, null, imgDrawable, null);
            }
        });

        mAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.publish_photo_bg_in_animation);
        mPhotoListBg.startAnimation(mAnimation);
    }

    private void startHideAnimation() {
        if (!isOpenedList) {
            return;
        }

        isAnimationing = true;
        Animation mAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.publish_photo_list_out_animation);
        LayoutAnimationController controller = new LayoutAnimationController(mAnimation);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mPhotoListView.startAnimation(mAnimation);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isOpenedList = false;
                isAnimationing = false;
                mPhotoListLayout.setVisibility(View.GONE);
                mPhotoListView.setVisibility(View.GONE);


                Drawable imgDrawable = mActivity.getResources().getDrawable(R.mipmap.publish_photo_ico_down);
                imgDrawable.setBounds(0, 0, imgDrawable.getMinimumWidth(), imgDrawable.getMinimumHeight());
                mTitleTextView.setCompoundDrawables(null, null, imgDrawable, null);
            }
        });

        mAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.publish_photo_bg_out_animation);
        mPhotoListBg.startAnimation(mAnimation);
    }

    public void clickTitleView() {
        if (isAnimationing) {
            return;
        }

        if (isOpenedList) {
            startHideAnimation();
        } else {
            startShowAnimation();
        }
    }


    public void returnMultipleSelect() {
        Intent intent = new Intent();
//        intent.putExtra(IPublishPhotoService.DataKey.MULTIPLE_PHOTO_LIST, mActivity.mAdapter.mSelectedList);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    public boolean isBackPressed() {
        if (isAnimationing) {
            return true;
        }

        // 如果下拉列表显示这，先隐藏
        if (isOpenedList) {
            startHideAnimation();
            return true;
        } else {
            return false;
        }
    }

    private void saveImageToSdcards(final String path, final Bitmap bitmap) {
//        final MLSImage.OnPictureSavedListener mListener
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
//            mListener.onPictureSaved(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static class GridItemDecoration extends RecyclerView.ItemDecoration {
        private Context mContext;

        public GridItemDecoration(Context context) {
            mContext = context;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int padding = MyUtils.dip2px(1);
            outRect.right = padding;
            outRect.top = padding;
            outRect.bottom = 0;
            outRect.left = 0;
            int childPos = parent.getChildAdapterPosition(view);

            if ((childPos + 1) % 3 == 0) {
                outRect.right = 0;
            }
        }
    }

}
