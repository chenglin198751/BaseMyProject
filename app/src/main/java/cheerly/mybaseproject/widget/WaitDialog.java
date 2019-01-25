package cheerly.mybaseproject.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import cheerly.mybaseproject.R;


public class WaitDialog extends Dialog {
    private View mView;

    public WaitDialog(Context context) {
        this(context, R.style.dialogNullBg);
        mView = View.inflate(context, R.layout.progress_layout, null);
    }

    protected WaitDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
    }

    @Override
    public View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public void show() {
        super.show();

    }


}
