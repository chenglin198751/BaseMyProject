package cheerly.mybaseproject.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by chenglin on 2017-3-21.
 * 自定义的InputFilter ，用来限制英文中文输入字符限制。两个汉字代表一个英文。
 */

public class EnglishCharFilter implements InputFilter {
    int maxLen = 0;

    /**
     * 输入英文的最大长度 。比如你想要限制40个汉字，80个英文字符，传入的值就是80
     * 使用方式：mEdit.setFilters(new InputFilter[]{filter});
     */
    public EnglishCharFilter(int len) {
        maxLen = len;
    }

    public static int calculateLength(CharSequence c, boolean english) {
        double len = 0;
        for (int i = 0; i < c.length(); i++) {
            char cc = c.charAt(i);
            if ((cc & 0xffff) <= 0xff) {
                len += 0.5;
            } else {
                len++;
            }
        }
        if (english) {
            len = len * 2;
        }
        return (int) Math.round(len);
    }

    @Override
    public CharSequence filter(CharSequence src, int start, int end, Spanned dest, int dstart, int dend) {
        int dindex = 0;
        int count = 0;

        while (count <= maxLen && dindex < dest.length()) {
            char c = dest.charAt(dindex++);
            if ((c & 0xffff) <= 0xff) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }

        if (count > maxLen) {
            return dest.subSequence(0, dindex - 1);
        }

        int sindex = 0;
        while (count <= maxLen && sindex < src.length()) {
            char c = src.charAt(sindex++);
            if ((c & 0xffff) <= 0xff) {
                count = count + 1;
            } else {
                count = count + 2;
            }
        }

        if (count > maxLen) {
            sindex--;
            return src.subSequence(0, sindex);
        }
        return null;
    }

}
