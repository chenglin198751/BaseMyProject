package widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import cheerly.mybaseproject.R;


public class WaitDialog extends Dialog {

    public WaitDialog(Context context) {
        this(context, R.style.dialog);
    }

    protected WaitDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_layout);
    }

    @Override
    public void show() {
        super.show();

    }


}
