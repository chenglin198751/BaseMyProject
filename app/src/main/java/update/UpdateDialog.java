package update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import httpwork.HttpCallback;
import httpwork.HttpUrl;
import preferences.PreferencesData;
import utils.Constants;
import utils.MyUtils;
import widget.MyToast;

/**
 * Created by chenglin on 2017-12-4.
 */

public class UpdateDialog extends Dialog {
    public static final long TIMES = 24 * 60 * 60 * 1000;//非强制更新的对话框24H只弹一次

    private BaseActivity mActivity;
    private View mDialogView;
    private TextView mLeftBtn, mRightBtn;
    private View bottom_line;
    private TextView tvMessage, tvTitle;
    private VersionUpdateModel mVersionModel;
    private UpdateDownLoadTask mDownLoadTask;

    public UpdateDialog(Context context) {
        this(context, R.style.dialog);
    }

    public UpdateDialog(Context context, int themeResId) {
        super(context, themeResId);
        mActivity = (BaseActivity) context;
        mDialogView = View.inflate(getContext(), R.layout.update_dialog_layout, null);

        mLeftBtn = mDialogView.findViewById(R.id.left_btn);
        mRightBtn = mDialogView.findViewById(R.id.right_btn);
        bottom_line = mDialogView.findViewById(R.id.bottom_line);
        tvMessage = mDialogView.findViewById(R.id.tv_message);
        tvTitle = mDialogView.findViewById(R.id.tv_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);

        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyUtils.hasNet()) {
                    MyToast.show(getContext().getString(R.string.net_error));
                    return;
                }

                if (mRightBtn.getText().equals(MyUtils.getString(R.string.update_version_update))) {
                    if (mVersionModel != null && !TextUtils.isEmpty(mVersionModel.url)) {
                        if (mDownLoadTask == null) {
                            mDownLoadTask = new UpdateDownLoadTask(UpdateDialog.this);
                        }
                        mDownLoadTask.start(mVersionModel.url);

                        if (HcxVersionModel.UPDATE_NORMAL == mVersionModel.updateType) {
                            dismiss();
                        } else if (HcxVersionModel.UPDATE_FORCE == mVersionModel.updateType) {
                            mRightBtn.setText(R.string.update_version_downloading);
                        }
                        MyToast.show(MyUtils.getString(R.string.update_version_downloading));
                    } else {
                        MyToast.show("无效的下载路径");
                    }
                } else if (mRightBtn.getText().equals(MyUtils.getString(R.string.update_version_downloading))) {
                    MyToast.show(MyUtils.getString(R.string.update_version_downloading));
                } else if (mRightBtn.getText().equals(MyUtils.getString(R.string.update_version_install))) {
                    if (mVersionModel != null) {
                        boolean isExist = UpdateDownLoadTask.apkExist(mActivity, mVersionModel.versionName);
                        if (isExist) {
                            MyUtils.installApk(mActivity, UpdateDownLoadTask.getApkPath());
                        } else {
                            MyToast.show("安装失败，请立即更新");
                            mRightBtn.setText(R.string.update_version_update);
                            mRightBtn.performLongClick();
                        }
                    }
                }
            }
        });
    }

    public void downloadSuccess() {
        if (isShowing()) {
            mRightBtn.setText(R.string.update_version_install);
        }
    }

    public void suddenBreadNet() {
        if (isShowing()) {
            mRightBtn.setText(R.string.update_version_update);
        }
    }

    /**
     * 检查更新
     */
    public void checkUpdate() {
        mActivity.post(HttpUrl.check_update, null, new HttpCallback() {
            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(String result) {
                HcxUpdateModel model = Constants.gson.fromJson(result, HcxUpdateModel.class);
                if (model == null || model.data == null) {
                    return;
                }
                HcxVersionModel infoModel = model.data;

//                //ToDo
//                infoModel.url = "http://w.tinydonuts.cn/download/Android/EatEquity/1.0.0/huichixia_1.0.0_test_9.apk";
//                infoModel.forceUpgrade = HcxVersionModel.UPDATE_NORMAL;
//                infoModel.content = "非常好用的应用，快来下载呗非常好用的应用，快来下载呗非常好用的应用，快来下载呗非常好用的应用，快来下载呗非常好用的应用，快来下载呗";
//                infoModel.title = "版本3.0.0";
//                infoModel.version = "3.0.0";
//                //ToDo

                //用MVVM模式，中转一下
                mVersionModel = new VersionUpdateModel();
                mVersionModel.url = infoModel.url;
                mVersionModel.updateType = infoModel.forceUpgrade;
                mVersionModel.content = infoModel.content;
                mVersionModel.title = infoModel.title;
                mVersionModel.versionName = infoModel.version;
                setData(mVersionModel);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        setContentView(mDialogView);
    }

    private void setData(VersionUpdateModel infoModel) {
        if (infoModel != null && infoModel.updateType >= 0) {
            long currentTimeMillis = System.currentTimeMillis();

            //非强制更新的对话框24H只弹一次
            if (HcxVersionModel.UPDATE_NORMAL == infoModel.updateType) {
                long lastTimes = PreferencesData.getUpdateDialogTimes();
                if (lastTimes > 0 && currentTimeMillis - lastTimes <= TIMES) {
                    return;
                }
            }

            if (!TextUtils.isEmpty(infoModel.title)) {
                tvTitle.setText(infoModel.title);
            } else {
                if (!TextUtils.isEmpty(infoModel.versionName)) {
                    tvTitle.setText("新版本" + infoModel.versionName);
                } else {
                    tvTitle.setText("有新版本");
                }
            }

            if (!TextUtils.isEmpty(infoModel.content)) {
                tvMessage.setText(infoModel.content);
            } else {
                tvMessage.setText(R.string.update_version_notice);
            }
            mRightBtn.setText(R.string.update_version_update);

            if (HcxVersionModel.UPDATE_NORMAL == infoModel.updateType) {
                mLeftBtn.setVisibility(View.VISIBLE);
                mRightBtn.setVisibility(View.VISIBLE);
                bottom_line.setVisibility(View.VISIBLE);
                setCancelable(true);
                setOnDismissListener(null);
                PreferencesData.setUpdateDialogTimes(currentTimeMillis);
            } else if (HcxVersionModel.UPDATE_FORCE == infoModel.updateType) {
                mLeftBtn.setVisibility(View.GONE);
                mRightBtn.setVisibility(View.VISIBLE);
                bottom_line.setVisibility(View.GONE);
                setCancelable(false);
            }

            boolean isExist = UpdateDownLoadTask.apkExist(mActivity, infoModel.versionName);
            if (isExist) {
                mRightBtn.setText(R.string.update_version_install);
            } else {
                File apkFile = new File(UpdateDownLoadTask.getApkPath());
                if (apkFile.exists()) {
                    apkFile.delete();
                }
            }

            if (mActivity != null && !mActivity.isFinishing()) {
                show();
            }
        }
    }

}
