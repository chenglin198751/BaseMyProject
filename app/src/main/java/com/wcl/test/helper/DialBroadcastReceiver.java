package com.wcl.test.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * 在拨号键盘输入 *#*#2022360#*#* 可以打开debug模式
 */
public class DialBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {

        }
    }
}
