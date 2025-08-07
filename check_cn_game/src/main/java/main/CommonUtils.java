package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    /**
     * 判断字符串是否包含中文字符
     */
    public static boolean containsChinese(String str) {
        String pattern = "[\u4e00-\u9fff]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.find();
    }
}
