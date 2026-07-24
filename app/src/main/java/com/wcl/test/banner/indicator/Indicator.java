package com.wcl.test.banner.indicator;

import android.view.View;

import androidx.annotation.NonNull;

import com.wcl.test.banner.config.IndicatorConfig;
import com.wcl.test.banner.listener.OnPageChangeListener;

public interface Indicator extends OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
