package photo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import utils.Constants;
import widget.MyToast;

/**
 * weichenglin create in 16/4/11
 */
public class SelectPhotosActivity extends BaseActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    public static final int GRID_COLUMN = 3;
    public RecyclerView mRecyclerView;
    public SelectPhotosAdapter mAdapter;
    public SelectPhotosHelper mHelper;

    public int mCount = -1;
    public boolean isSingleType = true;//单选还是多选

    /**
     * 单选照片
     */
    public static void startForSingle(Context context) {
        Intent intent = new Intent(context, SelectPhotosActivity.class);
        context.startActivity(intent);
    }

    /**
     * 多选照片
     */
    public static void startForMultiple(Context context, int count) {
        Intent intent = new Intent(context, SelectPhotosActivity.class);
        intent.putExtra("count", count);
        context.startActivity(intent);
    }


    private void requestReadExternalPermission() {
        //Android 6.0 以下不需要主动申请权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            init();
        } else {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                init();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//获取权限成功
                init();
            } else {//用户拒绝授予权限
                MyToast.show("请开启访问权限");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.publish_select_photos);
        getTitleHelper().hideTitleBar();
        requestReadExternalPermission();

        if (getIntent().hasExtra("count")) {
            mCount = getIntent().getIntExtra("count", -1);
            isSingleType = false;
        } else {
            isSingleType = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        initView();
        initAlbum();

        mHelper = new SelectPhotosHelper(this);
        mHelper.initImageData();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.title).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);

        mAdapter = new SelectPhotosAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initAlbum() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GRID_COLUMN);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addItemDecoration(new SelectPhotosHelper.GridItemDecoration(this));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel_btn) {
            finish();
        } else if (i == R.id.title) {
            mHelper.clickTitleView();
        } else if (i == R.id.next_btn) {
            if (mAdapter.getSelectedPhotoList() != null && mAdapter.getSelectedPhotoList().size() > 0) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.KEY_PHOTO_LIST, mAdapter.getSelectedPhotoList());
                sendMyBroadcast(Constants.ACTION_GET_PHOTO_LIST, bundle);
                finish();
            } else {
                MyToast.show(R.string.publish_selected_single_photo);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectPhotosHelper.FINISH_CODE && resultCode == RESULT_OK) {
            finish();
        }

        if (mAdapter != null) {
            mAdapter.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onBackPressed() {
        if (mHelper == null) {
            super.onBackPressed();
        } else {
            if (!mHelper.isBackPressed()) {
                super.onBackPressed();
            }
        }
    }

}
