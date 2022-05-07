package com.wcl.test.listener;

/**
 * Created by chenglin on 2017-5-5.
 */

public interface OnFinishedListener<B, T> {
    void onFinished(B b, T t);
}
