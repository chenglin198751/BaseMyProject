package com.wcl.test.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chenglin on 2017-5-24.
 */

public class ReplaceViewUtils {
    private ReplaceViewUtils() {
    }

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     *
     * @param oldView     被替换的旧View
     * @param newLayoutId 要替换进去的新ViewLayoutId
     */
    public static void replaceView(View oldView, final int newLayoutId) {
        if (oldView != null && oldView.getParent() != null) {
            View newView = inflateViewNoAdd(oldView, newLayoutId);
            replaceView(oldView, newView);
        }
    }

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     *
     * @param oldView 被替换的旧View
     * @param newView 要替换进去的新View
     */
    public static void replaceView(View oldView, View newView) {
        if (newView == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) oldView.getParent();
        final int index = parent.indexOfChild(oldView);
        parent.removeViewInLayout(oldView);

        final ViewGroup.LayoutParams layoutParams = oldView.getLayoutParams();
        if (layoutParams != null) {
            parent.addView(newView, index, layoutParams);
        } else {
            parent.addView(newView, index);
        }

        if (oldView.getId() != View.NO_ID) {
            newView.setId(oldView.getId());
        }
    }

    private static View inflateViewNoAdd(View oldView, int layoutResource) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        LayoutInflater factory = LayoutInflater.from(oldView.getContext());
        return factory.inflate(layoutResource, parent, false);
    }
}
