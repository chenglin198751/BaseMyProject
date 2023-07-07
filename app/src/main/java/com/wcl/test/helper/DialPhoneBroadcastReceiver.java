package com.wcl.test.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.wcl.test.EnvToggle;
import com.wcl.test.R;
import com.wcl.test.base.BaseAction;
import com.wcl.test.storage.ToggleSettings;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.widget.ToastUtils;


/**
 * 在拨号键盘输入 *#*#2022360#*#* 可以打开debug模式
 */
public class DialPhoneBroadcastReceiver extends BroadcastReceiver {
    public static final String SECRET_CODE = "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SECRET_CODE)) {
            BaseAction.sendBroadcast(SECRET_CODE, null);
        }
    }

    public static void showDebugView(Activity activity) {
        View view = View.inflate(activity, R.layout.sdk_debug_layout, null);
        final ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        viewGroup.addView(view);
        CheckBox logToggle = view.findViewById(R.id.log_toggle);
        CheckBox debugToggle = view.findViewById(R.id.debug_toggle);
        logToggle.setChecked(EnvToggle.isLog());
        debugToggle.setChecked(EnvToggle.isDebug());

        logToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ToggleSettings.setLogEnable(isChecked);
                if (AppBaseUtils.isDebuggable()){
                    ToastUtils.show("操作无效，AndroidStudio运行安装的的apk，日志永远是开启");
                }
            }
        });

        debugToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ToggleSettings.setDebugEnable(isChecked);
            }
        });

        view.findViewById(R.id.close_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getParent() != null) {
                    viewGroup.removeView(view);
                }
            }
        });

    }
}
