package com.wcl.test.base;

import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.widget.WaitDialog;

public interface IBaseView {
    void showNoNetView(View.OnClickListener listener);

    void hideNoNetView();

    void showEmptyView(String text, View.OnClickListener listener);

    void hideEmptyView();

    void showLoading(String text);

    void showLoading();

    void hideLoading();

    WaitDialog showWaitDialog();

    void dismissWaitDialog();

    void setStateViewGravity(int position);

    void setNestedParentView(ViewGroup parent);
}
