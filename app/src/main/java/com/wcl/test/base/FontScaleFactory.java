package com.wcl.test.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * LayoutInflater.Factory2 拦截器
 * - 优先委托给原有 inflater.getFactory2()
 * - 如果没有，则委托给传入的 AppCompatDelegate 实例（通过 getDelegate() 获取）
 * - 最后对 TextView 做 +1sp 的放大
 */
public class FontScaleFactory implements LayoutInflater.Factory2 {
    private final LayoutInflater mInflater;
    private final LayoutInflater.Factory2 mDelegateFactory;
    private final AppCompatDelegate mAppCompatDelegate;
    private final float mDeltaPx;

    public FontScaleFactory(@NonNull LayoutInflater inflater,
                            @Nullable LayoutInflater.Factory2 delegateFactory,
                            @Nullable AppCompatDelegate appCompatDelegate,
                            @Nullable float deltaPx) {
        this.mInflater = inflater;
        this.mDelegateFactory = delegateFactory;
        this.mAppCompatDelegate = appCompatDelegate;
        this.mDeltaPx = deltaPx;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;

        // 1) 优先使用原有的 Factory2（如果存在），以保留库的默认行为（如 AppCompat）
        if (mDelegateFactory != null) {
            try {
                view = mDelegateFactory.onCreateView(parent, name, context, attrs);
            } catch (Throwable ignored) {
            }
        }

        // 2) 如果没有得到 view，尝试用 AppCompatDelegate 的实例方法（它会返回 tint-aware widgets）
        if (view == null && mAppCompatDelegate != null) {
            try {
                view = mAppCompatDelegate.createView(parent, name, context, attrs);
            } catch (Throwable ignored) {
            }
        }

        // 3) 作为兜底，尝试用 LayoutInflater 自己创建（部分情况下可用）
        if (view == null) {
            try {
                view = mInflater.createView(name, null, attrs);
            } catch (Throwable ignored) {
            }
        }

        if (view == null) {
            return null;
        }

        if (view instanceof TextView tv) {
            float sizePx = tv.getTextSize();
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx + mDeltaPx);
        }
        return view;
    }
}
