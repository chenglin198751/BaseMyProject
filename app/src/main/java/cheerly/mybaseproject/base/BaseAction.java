package cheerly.mybaseproject.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BaseAction {
    public final static String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";

    public static void sendBroadcast(String action, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        Intent intent = new Intent(BaseAction.ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(BaseApp.getApp()).sendBroadcast(intent);
    }
}
