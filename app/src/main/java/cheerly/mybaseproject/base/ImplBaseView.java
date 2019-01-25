package cheerly.mybaseproject.base;

import android.view.View;

import cheerly.mybaseproject.widget.WaitDialog;

public interface ImplBaseView {
    void showNoNetView(View.OnClickListener listener);

    void hideNoNetView();

    void showEmptyView(String text, View.OnClickListener listener);

    void hideEmptyView();

    void showProgress(String text);

    void hideProgress();

    WaitDialog showWaitDialog(String text);

    void dismissWaitDialog();
}
