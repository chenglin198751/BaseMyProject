package com.wcl.test.utils;

import android.content.Context;

public class getResUtils {
    public static int getString(Context context, String str) {
        return context.getResources().getIdentifier(str, "string", context.getPackageName());
    }

    public static int getDrawable(Context context, String str) {
        return context.getResources().getIdentifier(str, "drawable", context.getPackageName());
    }

    public static int getId(Context context, String str) {
        return context.getResources().getIdentifier(str, "id", context.getPackageName());
    }

    public static int getStyle(Context context, String str) {
        return context.getResources().getIdentifier(str, "style", context.getPackageName());
    }

    public static int getLayout(Context context, String str) {
        return context.getResources().getIdentifier(str, "layout", context.getPackageName());
    }

    public static int getDimens(Context context, String str) {
        return context.getResources().getIdentifier(str, "dimen", context.getPackageName());
    }

    public static int getColor(Context context, String str) {
        return context.getResources().getIdentifier(str, "color", context.getPackageName());
    }

    public static int getAnim(Context context, String str) {
        return context.getResources().getIdentifier(str, "anim", context.getPackageName());
    }
}