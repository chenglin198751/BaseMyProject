package com.wcl.test.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.IdRes;
import androidx.core.util.Consumer;

import com.wcl.test.EnvToggle;
import com.wcl.test.R;
import com.wcl.test.base.EventBus;
import com.wcl.test.storage.ToggleSettings;


/**
 * 在拨号键盘输入 *#*#2022360#*#* 可以打开debug模式
 */
public class DialPhoneBroadcastReceiver extends BroadcastReceiver {
    public static final String SECRET_CODE = "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SECRET_CODE.equals(intent.getAction())) {
            EventBus.post(SECRET_CODE, null);
        }
    }

    public static void showDebugView(Activity activity) {
        View view = View.inflate(activity, R.layout.sdk_debug_layout, null);
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        viewGroup.addView(view);

        initToggle(view, R.id.log_toggle, EnvToggle.isLog(), ToggleSettings::setLogEnable);
        initToggle(view, R.id.debug_toggle, EnvToggle.isDebug(), ToggleSettings::setDebugEnable);

        view.findViewById(R.id.close_).setOnClickListener(v -> viewGroup.removeView(view));
    }

    private static void initToggle(View root, @IdRes int id, boolean current, Consumer<Boolean> setter) {
        CheckBox cb = root.findViewById(id);
        cb.setChecked(current);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> setter.accept(isChecked));
    }
}
