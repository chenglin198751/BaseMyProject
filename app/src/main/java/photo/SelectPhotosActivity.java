package photo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import base.BaseActivity;
import cheerly.mybaseproject.R;

/**
 * weichenglin create in 16/4/11
 */
public class SelectPhotosActivity extends BaseActivity implements View.OnClickListener {
    public static final int GRID_COLUMN = 3;
    public RecyclerView mRecyclerView;
    public SelectPhotosAdapter mAdapter;
    public SelectPhotosHelper mHelper;

    public int mMaxCount = -1;


    private void requestReadExternalPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // 0 是自己定义的请求coude
                ActivityCompat.requestPermissions(getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        } else {
//            Log.d(TAG, "READ permission is granted...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    // request successfully, handle you transactions

                } else {

                    // permission denied
                    // request failed
                }

                return;
            }
            default:
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestReadExternalPermission();
        setContentLayout(R.layout.publish_select_photos);
        getTitleHelper().hideTitle();

        initView();
        initAlbum();

        mHelper = new SelectPhotosHelper(this);
        mHelper.initImageData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if (!mHelper.isBackPressed()) {
            super.onBackPressed();
        }
    }

}
