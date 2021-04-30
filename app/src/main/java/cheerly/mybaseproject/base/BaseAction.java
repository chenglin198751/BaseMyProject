package cheerly.mybaseproject.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BaseAction {

    /**
     * 通用的发送广播，可以在任意位置发送
     */
    public static void sendBroadcast(String action, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        Intent intent = new Intent(BaseAction.ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(BaseApp.getApp()).sendBroadcast(intent);
    }

    public interface Keys {
        String ACTIVITY_NAME = "activity_name";
    }

    public final static String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
    public final static String ACTION_KEEP_SINGLE_ACTIVITY = "ACTION_KEEP_SINGLE_ACTIVITY";
    public final static String ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY = "ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY";

}
