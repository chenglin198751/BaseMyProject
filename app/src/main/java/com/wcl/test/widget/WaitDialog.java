package com.wcl.test.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.wcl.test.R;
import com.wcl.test.utils.AppUtils;


public class WaitDialog extends Dialog {

    public WaitDialog(Context context) {
        this(context, R.style.dialogNullBg);
    }

    protected WaitDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppUtils.setDialogEdgeToEdge(this);
        setContentView(R.layout.wait_dialog_layout);
    }
}
