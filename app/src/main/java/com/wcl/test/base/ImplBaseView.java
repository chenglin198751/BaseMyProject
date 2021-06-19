package com.wcl.test.base;

import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.widget.WaitDialog;

public interface ImplBaseView {
    void showNoNetView(View.OnClickListener listener);

    void hideNoNetView();

    void showEmptyView(String text, View.OnClickListener listener);

    void hideEmptyView();

    void showProgress(String text);

    void hideProgress();

    WaitDialog showWaitDialog();

    void dismissWaitDialog();

    void setNestedParentLayout(ViewGroup parent);
}
