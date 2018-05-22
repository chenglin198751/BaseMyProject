package utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import base.MyApp;

public class LibAction {
    public final static String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";

    public static void sendMyBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(LibAction.ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(MyApp.getApp()).sendBroadcast(intent);
    }
}
