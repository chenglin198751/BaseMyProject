package com.wcl.test.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by chenglin on 2017-3-21.
 * 自定义的 InputFilter，用来限制输入长度：一个英文字符（半角）占 1 位，一个汉字（全角）占 2 位。
 */

public class EnglishCharFilter implements InputFilter {
    private final int maxLen;

    /**
     * 输入英文的最大长度。比如你想限制 40 个汉字、80 个英文字符，传入的值就是 80。
     * 使用方式：mEdit.setFilters(new InputFilter[]{filter});
     */
    public EnglishCharFilter(int len) {
        maxLen = len;
    }

    /**
     * 计算文本的占用长度。
     *
     * @param english true 返回半角宽度（英文=1、全角=2，与 filter 一致）；
     *                false 返回字数（全角=1 字、半角=0.5 字，四舍五入）
     */
    public static int calculateLength(CharSequence c, boolean english) {
        int width = 0;
        for (int i = 0; i < c.length(); i++) {
            width += charWidth(c.charAt(i));
        }
        if (english) {
            return width;
        }
        return (int) Math.round(width / 2.0);
    }

    @Override
    public CharSequence filter(CharSequence src, int start, int end, Spanned dest, int dstart, int dend) {
        int count = 0;

        // 先统计 dest 中已有的内容，确认是否已超限
        int dindex = 0;
        while (dindex < dest.length()) {
            char c = dest.charAt(dindex);
            int w = charWidth(c);
            if (count + w > maxLen) {
                // 已有内容超限，截断到当前字符之前
                return dest.subSequence(0, dindex);
            }
            count += w;
            dindex++;
        }

        // 再统计本次要插入的 src
        int sindex = 0;
        while (sindex < src.length()) {
            char c = src.charAt(sindex);
            int w = charWidth(c);
            if (count + w > maxLen) {
                // 插入内容超限，截断到当前字符之前
                return src.subSequence(0, sindex);
            }
            count += w;
            sindex++;
        }

        return null;
    }

    /**
     * 计算单个字符占用的半角宽度：半角字符（英文、数字、常见符号）占 1，全角字符（汉字等）占 2。
     * 增补字符（emoji 等）由高代理统一计 2，低代理返回 0，避免重复计数与从代理对中间截断。
     */
    private static int charWidth(char c) {
        if (Character.isLowSurrogate(c)) {
            return 0;
        }
        return c <= 0xff ? 1 : 2;
    }
}
