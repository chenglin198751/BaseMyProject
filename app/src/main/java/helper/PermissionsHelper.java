package helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 *
 */
public class PermissionsHelper {
    private Activity mActivity;
    private final String[] PERMISSION = new String[1];
    private onPermissionListener mListener;
    private static final int PER_GRANTED_ALL = 103;

    public PermissionsHelper(Activity activity, onPermissionListener listener) {
        mActivity = activity;
        mListener = listener;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PER_GRANTED_ALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mListener.onPermissionsAllowed(true);
            } else {
                mListener.onPermissionsAllowed(false);
            }
        }
    }

    public void checkPermission(String permission) {
        PERMISSION[0] = permission;

        //Android 6.0 以下不需要主动申请权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mListener.onPermissionsAllowed(true);
            return;
        }

        boolean hasPermission = getPermissionState();
        if (!hasPermission) {
            ActivityCompat.requestPermissions(mActivity, PERMISSION, PER_GRANTED_ALL);
        } else {
            mListener.onPermissionsAllowed(true);
        }
    }


    /**
     * 是否已经全部获取了读取手机状态权限
     * 返回的是已经获取的权限状态
     */
    private boolean getPermissionState() {
        int permissionPhone = ActivityCompat.checkSelfPermission(mActivity, PERMISSION[0]);

        if (permissionPhone == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public interface onPermissionListener {
        /**
         * 权限是否允许：isAllowed = true 是允许，false是拒绝
         */
        void onPermissionsAllowed(final boolean isAllowed);

    }
}
